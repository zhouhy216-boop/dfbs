package com.dfbs.app.application.expense;

import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseStatus;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class ExpenseClaimTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ExpenseRepo expenseRepo;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private com.dfbs.app.config.CurrentUserProvider currentUserProvider;

    private Long user1Id;
    private Long user2Id;

    @BeforeEach
    void ensureUsers() {
        UserEntity u1 = new UserEntity();
        u1.setCanRequestPermission(false);
        u1.setAuthorities("[]");
        u1.setAllowNormalNotification(true);
        u1.setCanManageStatements(false);
        u1 = userRepo.save(u1);
        user1Id = u1.getId();

        UserEntity u2 = new UserEntity();
        u2.setCanRequestPermission(false);
        u2.setAuthorities("[]");
        u2.setAllowNormalNotification(true);
        u2.setCanManageStatements(false);
        u2 = userRepo.save(u2);
        user2Id = u2.getId();

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
    }

    @Test
    void expense_createVoid_thenUpdateVoidFails() {
        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(LocalDate.now());
        cmd.setAmount(new BigDecimal("100.00"));
        cmd.setCurrency(Currency.CNY);
        ExpenseEntity e = expenseService.create(cmd);
        assertThat(e.getStatus()).isEqualTo(ExpenseStatus.DRAFT);

        expenseService.voidExpense(e.getId());
        ExpenseEntity voided = expenseRepo.findById(e.getId()).orElseThrow();
        assertThat(voided.getStatus()).isEqualTo(ExpenseStatus.VOID);

        var updateCmd = new ExpenseService.UpdateExpenseCommand();
        updateCmd.setAmount(new BigDecimal("200.00"));
        assertThatThrownBy(() -> expenseService.update(e.getId(), updateCmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT expense can be updated");
    }

    @Test
    void claim_createFromTwoExpenses_expensesClaimed() {
        var cmd1 = new ExpenseService.CreateExpenseCommand();
        cmd1.setExpenseDate(LocalDate.now());
        cmd1.setAmount(new BigDecimal("50.00"));
        cmd1.setCurrency(Currency.CNY);
        ExpenseEntity e1 = expenseService.create(cmd1);
        var cmd2 = new ExpenseService.CreateExpenseCommand();
        cmd2.setExpenseDate(LocalDate.now());
        cmd2.setAmount(new BigDecimal("80.00"));
        cmd2.setCurrency(Currency.CNY);
        ExpenseEntity e2 = expenseService.create(cmd2);

        var claim = claimService.create(List.of(e1.getId(), e2.getId()));
        assertThat(claim.getTotalAmount()).isEqualByComparingTo(new BigDecimal("130.00"));

        assertThat(expenseRepo.findById(e1.getId()).orElseThrow().getStatus()).isEqualTo(ExpenseStatus.CLAIMED);
        assertThat(expenseRepo.findById(e2.getId()).orElseThrow().getStatus()).isEqualTo(ExpenseStatus.CLAIMED);
    }

    @Test
    void claim_currencyMismatch_fails() {
        var cmdCny = new ExpenseService.CreateExpenseCommand();
        cmdCny.setExpenseDate(LocalDate.now());
        cmdCny.setAmount(new BigDecimal("100.00"));
        cmdCny.setCurrency(Currency.CNY);
        ExpenseEntity eCny = expenseService.create(cmdCny);

        var cmdUsd = new ExpenseService.CreateExpenseCommand();
        cmdUsd.setExpenseDate(LocalDate.now());
        cmdUsd.setAmount(new BigDecimal("10.00"));
        cmdUsd.setCurrency(Currency.USD);
        ExpenseEntity eUsd = expenseService.create(cmdUsd);

        assertThatThrownBy(() -> claimService.create(List.of(eCny.getId(), eUsd.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same currency");
    }

    @Test
    void claim_submitReturn_modifyResubmit_expenseClaimedAgain() {
        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(LocalDate.now());
        cmd.setAmount(new BigDecimal("99.00"));
        cmd.setCurrency(Currency.CNY);
        ExpenseEntity e = expenseService.create(cmd);
        var claim = claimService.create(List.of(e.getId()));

        claimService.submit(claim.getId());
        assertThat(claimService.search(null, PageRequest.of(0, 1)).getContent().get(0).getStatus())
                .isEqualTo(com.dfbs.app.modules.expense.ClaimStatus.SUBMITTED);

        // Return requires Finance/Admin: grant user1 canManageStatements for this test
        UserEntity u1 = userRepo.findById(user1Id).orElseThrow();
        u1.setCanManageStatements(true);
        userRepo.save(u1);
        claimService.returnClaim(claim.getId());
        assertThat(expenseRepo.findById(e.getId()).orElseThrow().getStatus()).isEqualTo(ExpenseStatus.DRAFT);

        var updateCmd = new ExpenseService.UpdateExpenseCommand();
        updateCmd.setAmount(new BigDecimal("100.00"));
        expenseService.update(e.getId(), updateCmd);

        claimService.submit(claim.getId());
        assertThat(expenseRepo.findById(e.getId()).orElseThrow().getStatus()).isEqualTo(ExpenseStatus.CLAIMED);
    }

    @Test
    void permission_userBCannotClaimUserAExpense() {
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(LocalDate.now());
        cmd.setAmount(new BigDecimal("50.00"));
        cmd.setCurrency(Currency.CNY);
        ExpenseEntity e = expenseService.create(cmd);

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user2Id));
        var page = expenseService.search(null, PageRequest.of(0, 10));
        assertThat(page.getContent()).isEmpty();

        assertThatThrownBy(() -> claimService.create(List.of(e.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not belong to current user");
    }
}
