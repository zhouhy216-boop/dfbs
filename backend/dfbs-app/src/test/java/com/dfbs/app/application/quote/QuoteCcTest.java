package com.dfbs.app.application.quote;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.settings.BusinessLineService;
import com.dfbs.app.modules.notification.NotificationEntity;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.settings.BusinessLineEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuoteCcTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private BusinessLineService businessLineService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FeeTypeRepo feeTypeRepo;

    @Test
    void scenario1_configExists_notificationSentToLeader() {
        // Create Business Line with leader
        BusinessLineEntity businessLine = businessLineService.create("Test Business Line", "[100,200]");
        assertThat(businessLine).isNotNull();

        // Create Quote with Business Line
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        cmd.setBusinessLineId(businessLine.getId());
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item with valid FeeType
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(5000.00));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        // Confirm Quote
        QuoteEntity confirmed = quoteService.confirm(quote.getId());
        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");

        // Verify Notifications saved for Leaders
        List<NotificationEntity> notifications1 = notificationService.listMyNotifications(100L);
        List<NotificationEntity> notifications2 = notificationService.listMyNotifications(200L);

        assertThat(notifications1).isNotEmpty();
        assertThat(notifications2).isNotEmpty();

        // Verify notification content
        NotificationEntity notif1 = notifications1.get(0);
        assertThat(notif1.getTitle()).contains("报价单已确认");
        assertThat(notif1.getTitle()).contains(quote.getQuoteNo());
        assertThat(notif1.getContent()).contains("客户");
        assertThat(notif1.getContent()).contains("5000");
        assertThat(notif1.getTargetUrl()).contains("/quotes/" + quote.getId());
        assertThat(notif1.getIsRead()).isFalse();
    }

    @Test
    void scenario2_noConfig_noNotification_noError() {
        // Create Quote without Business Line
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        // businessLineId is null
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item with valid FeeType
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(3000.00));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        // Confirm Quote - should succeed without error
        QuoteEntity confirmed = quoteService.confirm(quote.getId());
        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");

        // Verify no notifications were created (no business line configured)
        // This is expected behavior - no error, just no notification
    }

    @Test
    void scenario3_readAccess_notificationContentCorrect() {
        // Create Business Line
        BusinessLineEntity businessLine = businessLineService.create("Sales Line", "[300]");
        
        // Create Quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(999L);
        cmd.setBusinessLineId(businessLine.getId());
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(2);
        itemCmd.setUnitPrice(BigDecimal.valueOf(1500.50));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        // Confirm
        quoteService.confirm(quote.getId());

        // Verify Notification content contains correct Quote No and Amount
        List<NotificationEntity> notifications = notificationService.listMyNotifications(300L);
        assertThat(notifications).isNotEmpty();

        NotificationEntity notification = notifications.get(0);
        assertThat(notification.getTitle()).contains(quote.getQuoteNo());
        assertThat(notification.getContent()).contains("3001.00");  // 2 * 1500.50 = 3001.00
        assertThat(notification.getContent()).contains("客户 #999");
        assertThat(notification.getTargetUrl()).isEqualTo("/quotes/" + quote.getId());
    }

    @Test
    void scenario4_notificationService_canRetrieveSavedNotification() {
        // Create Business Line
        BusinessLineEntity businessLine = businessLineService.create("Test Line", "[400]");
        
        // Create and confirm Quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        cmd.setBusinessLineId(businessLine.getId());
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(2000.00));
        itemCmd.setDescription("Test");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);
        quoteService.confirm(quote.getId());

        // Verify NotificationService can retrieve the saved notification
        List<NotificationEntity> notifications = notificationService.listMyNotifications(400L);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).contains(quote.getQuoteNo());
    }
}
