package com.dfbs.app.application.triprequest;

import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.triprequest.TripRequestEntity;
import com.dfbs.app.modules.triprequest.TripRequestRepo;
import com.dfbs.app.modules.triprequest.TripRequestStatus;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import com.dfbs.app.application.expense.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class TripRequestTest {

    @Autowired
    private TripRequestService tripRequestService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private TripRequestRepo tripRequestRepo;

    @Autowired
    private ExpenseRepo expenseRepo;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private com.dfbs.app.config.CurrentUserProvider currentUserProvider;

    private Long user1Id;  // creator
    private Long user2Id;  // leader/finance

    @BeforeEach
    void ensureUsers() {
        UserEntity u1 = new UserEntity();
        u1.setUsername("trip-u1");
        u1.setCanRequestPermission(false);
        u1.setAuthorities("[]");
        u1.setAllowNormalNotification(true);
        u1.setCanManageStatements(false);
        u1 = userRepo.save(u1);
        user1Id = u1.getId();

        UserEntity u2 = new UserEntity();
        u2.setUsername("trip-u2");
        u2.setCanRequestPermission(false);
        u2.setAuthorities("[]");
        u2.setAllowNormalNotification(true);
        u2.setCanManageStatements(false);
        u2 = userRepo.save(u2);
        user2Id = u2.getId();

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
    }

    private TripRequestService.CreateTripRequestCommand baseCommand() {
        var cmd = new TripRequestService.CreateTripRequestCommand();
        cmd.setCity("Beijing");
        cmd.setStartDate(LocalDate.now().plusDays(1));
        cmd.setEndDate(LocalDate.now().plusDays(3));
        cmd.setPurpose("Business meeting");
        cmd.setEstTransportCost(BigDecimal.ZERO);
        cmd.setEstAccommodationCost(BigDecimal.ZERO);
        cmd.setCurrency("CNY");
        return cmd;
    }

    @Test
    void create_independentTripWithoutReason_fails() {
        var cmd = baseCommand();
        cmd.setWorkOrderId(null);
        cmd.setIndependentReason(null);

        assertThatThrownBy(() -> tripRequestService.create(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("independent_reason is required");
    }

    @Test
    void workflow_submitLeaderApproveFinanceApprove_success() {
        var cmd = baseCommand();
        cmd.setWorkOrderId(1L);  // has work order, no independent reason needed
        TripRequestEntity t = tripRequestService.create(cmd);
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.DRAFT);

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        tripRequestService.submit(t.getId());
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.SUBMITTED);

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user2Id));
        tripRequestService.leaderApprove(t.getId());
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.LEADER_APPROVED);

        tripRequestService.financeApprove(t.getId());
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.FINANCE_APPROVED);
    }

    @Test
    void withdraw_submittedOk_afterLeaderApproveFails() {
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        var cmd = baseCommand();
        cmd.setWorkOrderId(1L);
        TripRequestEntity t = tripRequestService.create(cmd);
        tripRequestService.submit(t.getId());

        tripRequestService.withdraw(t.getId());
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.DRAFT);

        tripRequestService.submit(t.getId());
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user2Id));
        tripRequestService.leaderApprove(t.getId());

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        final Long tripId = t.getId();
        assertThatThrownBy(() -> tripRequestService.withdraw(tripId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SUBMITTED can be withdrawn");
    }

    @Test
    void cancelFlow_financeApproved_requestCancel_thenLeaderApprove_cancelled() {
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        var cmd = baseCommand();
        cmd.setWorkOrderId(1L);
        TripRequestEntity t = tripRequestService.create(cmd);
        tripRequestService.submit(t.getId());

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user2Id));
        tripRequestService.leaderApprove(t.getId());
        tripRequestService.financeApprove(t.getId());
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.FINANCE_APPROVED);

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        tripRequestService.requestCancel(t.getId(), "Plan changed");
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.CANCEL_REQUESTED);
        assertThat(t.getCancellationReason()).isEqualTo("Plan changed");

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user2Id));
        tripRequestService.leaderCancelApprove(t.getId());
        t = tripRequestRepo.findById(t.getId()).orElseThrow();
        assertThat(t.getStatus()).isEqualTo(TripRequestStatus.CANCELLED);
    }

    @Test
    void expenseLink_transportWithoutTripId_fails_withValidTripId_success() {
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));

        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(LocalDate.now());
        cmd.setAmount(new BigDecimal("100.00"));
        cmd.setCurrency(Currency.CNY);
        cmd.setExpenseType(ExpenseType.TRANSPORT);
        cmd.setTripRequestId(null);

        assertThatThrownBy(() -> expenseService.create(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("差旅费用必须关联出差申请");

        var tripCmd = baseCommand();
        tripCmd.setWorkOrderId(1L);
        TripRequestEntity trip = tripRequestService.create(tripCmd);

        cmd.setTripRequestId(trip.getId());
        ExpenseEntity e = expenseService.create(cmd);
        assertThat(e.getTripRequestId()).isEqualTo(trip.getId());
        assertThat(e.getExpenseType()).isEqualTo(ExpenseType.TRANSPORT);
    }
}
