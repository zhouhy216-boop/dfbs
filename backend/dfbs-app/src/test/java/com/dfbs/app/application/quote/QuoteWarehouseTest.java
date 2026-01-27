package com.dfbs.app.application.quote;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.settings.WarehouseConfigService;
import com.dfbs.app.modules.notification.NotificationEntity;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuoteWarehouseTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private WarehouseConfigService warehouseConfigService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FeeTypeRepo feeTypeRepo;

    @Autowired
    private com.dfbs.app.application.masterdata.PartBomService partBomService;

    @Test
    void scenario1_setup_configureMockUserAsWarehouse() {
        // Configure a mock user as Warehouse
        List<Long> userIds = List.of(500L, 501L);
        warehouseConfigService.updateWarehouseUserIds(userIds);

        // Verify configuration
        List<Long> configured = warehouseConfigService.getWarehouseUserIds();
        assertThat(configured).containsExactlyInAnyOrder(500L, 501L);
    }

    @Test
    void scenario2_ccTrigger_createQuoteWithHQItem_verifyNotificationSent() {
        // Setup: Configure warehouse users
        warehouseConfigService.updateWarehouseUserIds(List.of(600L));

        // Create Quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item with HEADQUARTERS warehouse
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.PARTS);
        itemCmd.setQuantity(2);
        itemCmd.setUnitPrice(BigDecimal.valueOf(100.00));
        itemCmd.setDescription("HQ Item");
        itemCmd.setUnit("个");
        itemCmd.setWarehouse(QuoteItemWarehouse.HEADQUARTERS);
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        // Refresh quote to get updated flags
        quote = quoteService.findById(quote.getId()).orElseThrow();

        // Verify isWarehouseCcSent=true
        assertThat(quote.getIsWarehouseCcSent()).isTrue();

        // Verify Notification sent
        List<NotificationEntity> notifications = notificationService.listMyNotifications(600L);
        assertThat(notifications).isNotEmpty();
        NotificationEntity notification = notifications.get(0);
        assertThat(notification.getTitle()).contains("新报价单需发货");
        assertThat(notification.getTitle()).contains(quote.getQuoteNo());
        assertThat(notification.getContent()).contains(quote.getQuoteNo());
    }

    @Test
    void scenario3_noTrigger_createQuoteWithNonHQItem_noNotification() {
        // Setup: Configure warehouse users
        warehouseConfigService.updateWarehouseUserIds(List.of(700L));

        // Create Quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item with LOCAL warehouse (not HEADQUARTERS)
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(200.00));
        itemCmd.setDescription("Local Item");
        itemCmd.setUnit("个");
        itemCmd.setWarehouse(QuoteItemWarehouse.LOCAL);
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        // Refresh quote
        quote = quoteService.findById(quote.getId()).orElseThrow();

        // Verify isWarehouseCcSent=false
        assertThat(quote.getIsWarehouseCcSent()).isFalse();

        // Verify NO Notification sent
        List<NotificationEntity> notifications = notificationService.listMyNotifications(700L);
        assertThat(notifications).isEmpty();
    }

    @Test
    void scenario4_dedup_updateQuoteFromScenario2_noNewNotification() {
        // Setup: Configure warehouse users
        warehouseConfigService.updateWarehouseUserIds(List.of(800L));

        // Create Quote with HQ item (triggers CC)
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.PARTS);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(150.00));
        itemCmd.setDescription("HQ Item");
        itemCmd.setUnit("个");
        itemCmd.setWarehouse(QuoteItemWarehouse.HEADQUARTERS);
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        // Refresh quote to get updated flags
        quote = quoteService.findById(quote.getId()).orElseThrow();
        
        // Get initial notification count
        List<NotificationEntity> initialNotifications = notificationService.listMyNotifications(800L);
        int initialCount = initialNotifications.size();
        assertThat(initialCount).isGreaterThan(0);
        assertThat(quote.getIsWarehouseCcSent()).isTrue();

        // Update quote (e.g., change currency)
        var updateCmd = new QuoteService.UpdateQuoteCommand();
        updateCmd.setCurrency(com.dfbs.app.modules.quote.enums.Currency.USD);
        quoteService.updateHeader(quote.getId(), updateCmd);

        // Verify NO new notification sent (de-duplication)
        List<NotificationEntity> afterUpdateNotifications = notificationService.listMyNotifications(800L);
        assertThat(afterUpdateNotifications.size()).isEqualTo(initialCount);
    }

    @Test
    void scenario5_shipTrigger_confirmQuote_verifyShipNotificationSent() {
        // Setup: Configure warehouse users
        warehouseConfigService.updateWarehouseUserIds(List.of(900L));

        // Create Quote with HQ item
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Create a part for testing (required for PARTS expense type)
        var part = partBomService.createPart("Test Part HQ", "Spec", "个");
        
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.PARTS);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(300.00));
        itemCmd.setDescription("HQ Item");
        itemCmd.setUnit("个");
        itemCmd.setWarehouse(QuoteItemWarehouse.HEADQUARTERS);
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemCmd.setPartId(part.getId());  // Required for PARTS
        itemService.addItem(quote.getId(), itemCmd);

        // Confirm Quote
        QuoteEntity confirmed = quoteService.confirm(quote.getId());
        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");

        // Refresh to get updated flags
        confirmed = quoteService.findById(confirmed.getId()).orElseThrow();

        // Verify isWarehouseShipSent=true
        assertThat(confirmed.getIsWarehouseShipSent()).isTrue();

        // Verify Ship Notification sent
        List<NotificationEntity> notifications = notificationService.listMyNotifications(900L);
        assertThat(notifications.size()).isGreaterThanOrEqualTo(2);  // At least CC + Ship
        
        // Find the Ship notification
        NotificationEntity shipNotification = notifications.stream()
                .filter(n -> n.getTitle().contains("报价单已确认，请安排发货"))
                .findFirst()
                .orElseThrow();
        
        assertThat(shipNotification.getTitle()).contains("报价单已确认，请安排发货");
        assertThat(shipNotification.getTitle()).contains(quote.getQuoteNo());
        assertThat(shipNotification.getContent()).contains("已确认");
        assertThat(shipNotification.getContent()).contains("安排发货");
    }

    @Test
    void scenario6_lateTrigger_createQuoteNoHQ_thenUpdateToAddHQ_verifyCCTriggered() {
        // Setup: Configure warehouse users
        warehouseConfigService.updateWarehouseUserIds(List.of(1000L));

        // Create Quote without HQ item
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item with LOCAL warehouse
        var itemCmd1 = new QuoteItemService.CreateItemCommand();
        itemCmd1.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd1.setQuantity(1);
        itemCmd1.setUnitPrice(BigDecimal.valueOf(100.00));
        itemCmd1.setDescription("Local Item");
        itemCmd1.setUnit("次");
        itemCmd1.setWarehouse(QuoteItemWarehouse.LOCAL);
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd1.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd1);

        // Verify no notification sent initially
        quote = quoteService.findById(quote.getId()).orElseThrow();
        assertThat(quote.getIsWarehouseCcSent()).isFalse();
        List<NotificationEntity> initialNotifications = notificationService.listMyNotifications(1000L);
        int initialCount = initialNotifications.size();

        // Update: Add HQ item
        var itemCmd2 = new QuoteItemService.CreateItemCommand();
        itemCmd2.setExpenseType(QuoteExpenseType.PARTS);
        itemCmd2.setQuantity(1);
        itemCmd2.setUnitPrice(BigDecimal.valueOf(200.00));
        itemCmd2.setDescription("HQ Item Added");
        itemCmd2.setUnit("个");
        itemCmd2.setWarehouse(QuoteItemWarehouse.HEADQUARTERS);
        if (!feeTypes.isEmpty()) {
            itemCmd2.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd2);

        // Trigger update (by updating header to trigger the check)
        var updateCmd = new QuoteService.UpdateQuoteCommand();
        updateCmd.setCurrency(com.dfbs.app.modules.quote.enums.Currency.CNY);
        quoteService.updateHeader(quote.getId(), updateCmd);

        // Verify CC Notification is triggered now
        quote = quoteService.findById(quote.getId()).orElseThrow();
        assertThat(quote.getIsWarehouseCcSent()).isTrue();

        List<NotificationEntity> afterUpdateNotifications = notificationService.listMyNotifications(1000L);
        assertThat(afterUpdateNotifications.size()).isEqualTo(initialCount + 1);
        
        NotificationEntity ccNotification = afterUpdateNotifications.get(0);
        assertThat(ccNotification.getTitle()).contains("新报价单需发货");
        assertThat(ccNotification.getTitle()).contains(quote.getQuoteNo());
    }
}
