package com.dfbs.app.application.expense;

import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.application.triprequest.TripRequestService;
import com.dfbs.app.modules.triprequest.TripRequestEntity;
import com.dfbs.app.modules.triprequest.TripRequestRepo;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderRepo;
import com.dfbs.app.modules.workorder.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class ExpenseStatsTest {

    @Autowired
    private ExpenseStatsService expenseStatsService;

    @Autowired
    private ExpenseExportService expenseExportService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private TripRequestService tripRequestService;

    @Autowired
    private ExpenseRepo expenseRepo;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private WorkOrderRepo workOrderRepo;

    @Autowired
    private TripRequestRepo tripRequestRepo;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private com.dfbs.app.config.CurrentUserProvider currentUserProvider;

    private Long user1Id;
    private Long financeUserId;

    @BeforeEach
    void ensureUsers() {
        UserEntity u1 = new UserEntity();
        u1.setUsername("stats-u1");
        u1.setCanRequestPermission(false);
        u1.setAuthorities("[]");
        u1.setAllowNormalNotification(true);
        u1.setCanManageStatements(false);
        u1 = userRepo.save(u1);
        user1Id = u1.getId();

        UserEntity finance = new UserEntity();
        finance.setUsername("stats-finance");
        finance.setCanRequestPermission(false);
        finance.setAuthorities("[]");
        finance.setAllowNormalNotification(true);
        finance.setCanManageStatements(true);
        finance = userRepo.save(finance);
        financeUserId = finance.getId();

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
    }

    @Test
    void aggregation_totalIncludesAAndB_converted_approvedIncludesB() {
        LocalDate today = LocalDate.now();
        var req = new ExpenseStatsRequest();
        req.setStartDate(today.minusDays(1));
        req.setEndDate(today.plusDays(1));
        req.setGroupBy(GroupBy.USER);

        var cmdA = new ExpenseService.CreateExpenseCommand();
        cmdA.setExpenseDate(today);
        cmdA.setAmount(new BigDecimal("100.00"));
        cmdA.setCurrency(Currency.CNY);
        ExpenseEntity eA = expenseService.create(cmdA);

        var cmdB = new ExpenseService.CreateExpenseCommand();
        cmdB.setExpenseDate(today);
        cmdB.setAmount(new BigDecimal("10.00"));
        cmdB.setCurrency(Currency.USD);
        ExpenseEntity eB = expenseService.create(cmdB);

        var claim = claimService.create(List.of(eB.getId()));
        claimService.submit(claim.getId());
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(financeUserId));
        claimService.approve(claim.getId());
        claimService.pay(claim.getId());

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(user1Id));
        List<ExpenseStatsItemDto> stats = expenseStatsService.getStats(req);

        assertThat(stats).isNotEmpty();
        ExpenseStatsItemDto userGroup = stats.stream().filter(s -> ("User:" + user1Id).equals(s.getGroupKey())).findFirst().orElseThrow();
        assertThat(userGroup.getTotalRmb()).isEqualByComparingTo(new BigDecimal("172.00")); // 100 CNY + 10*7.2 USD
        assertThat(userGroup.getTotalAmount().get(Currency.CNY)).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(userGroup.getTotalAmount().get(Currency.USD)).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(userGroup.getApprovedAmount().get(Currency.USD)).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void customerGrouping_viaWorkOrder_groupsUnderCust1() {
        QuoteEntity quote = new QuoteEntity();
        quote.setQuoteNo("QT-STATS-" + System.currentTimeMillis());
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setSourceType(QuoteSourceType.MANUAL);
        quote.setCustomerId(1L);
        quote.setCustomerName("Cust1");
        quote = quoteRepo.save(quote);

        WorkOrderEntity wo = new WorkOrderEntity();
        wo.setQuoteId(quote.getId());
        wo.setInitiatorId(user1Id);
        wo.setStatus(WorkOrderStatus.PENDING);
        wo = workOrderRepo.save(wo);

        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(LocalDate.now());
        cmd.setAmount(new BigDecimal("50.00"));
        cmd.setCurrency(Currency.CNY);
        cmd.setWorkOrderId(wo.getId());
        ExpenseEntity e = expenseService.create(cmd);

        var req = new ExpenseStatsRequest();
        req.setStartDate(LocalDate.now().minusDays(1));
        req.setEndDate(LocalDate.now().plusDays(1));
        req.setGroupBy(GroupBy.CUSTOMER);
        List<ExpenseStatsItemDto> stats = expenseStatsService.getStats(req);

        assertThat(stats).anyMatch(s -> "Cust1".equals(s.getGroupKey()));
        ExpenseStatsItemDto cust1 = stats.stream().filter(s -> "Cust1".equals(s.getGroupKey())).findFirst().orElseThrow();
        assertThat(cust1.getTotalRmb()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void tripBudget_tripGroup_showsBudget1000_real800() {
        var tripCmd = new TripRequestService.CreateTripRequestCommand();
        tripCmd.setCity("Beijing");
        tripCmd.setStartDate(LocalDate.now().plusDays(1));
        tripCmd.setEndDate(LocalDate.now().plusDays(3));
        tripCmd.setPurpose("Meeting");
        tripCmd.setWorkOrderId(1L);
        tripCmd.setEstTransportCost(new BigDecimal("600"));
        tripCmd.setEstAccommodationCost(new BigDecimal("400"));
        TripRequestEntity tripEntity = tripRequestService.create(tripCmd);

        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(LocalDate.now());
        cmd.setAmount(new BigDecimal("800.00"));
        cmd.setCurrency(Currency.CNY);
        cmd.setExpenseType(ExpenseType.TRANSPORT);
        cmd.setTripRequestId(tripEntity.getId());
        expenseService.create(cmd);

        var req = new ExpenseStatsRequest();
        req.setStartDate(LocalDate.now().minusDays(1));
        req.setEndDate(LocalDate.now().plusDays(5));
        req.setGroupBy(GroupBy.TRIP);
        List<ExpenseStatsItemDto> stats = expenseStatsService.getStats(req);

        ExpenseStatsItemDto tripGroup = stats.stream()
                .filter(s -> ("Trip:" + tripEntity.getId()).equals(s.getGroupKey()))
                .findFirst().orElseThrow();
        assertThat(tripGroup.getEstTransport()).isEqualByComparingTo(new BigDecimal("600"));
        assertThat(tripGroup.getEstAccommodation()).isEqualByComparingTo(new BigDecimal("400"));
        assertThat(tripGroup.getRealTransport()).isEqualByComparingTo(new BigDecimal("800"));
        assertThat(tripGroup.getTotalRmb()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void export_returnsExcelContentTypeAndNonEmptyBody() throws Exception {
        var req = new ExpenseStatsRequest();
        req.setStartDate(LocalDate.now().minusDays(30));
        req.setEndDate(LocalDate.now().plusDays(1));
        req.setGroupBy(GroupBy.USER);
        MockHttpServletResponse response = new MockHttpServletResponse();
        expenseExportService.exportStats(req, response);

        assertThat(response.getContentType()).contains("spreadsheetml");
        assertThat(response.getContentAsByteArray().length).isGreaterThan(100);
    }
}
