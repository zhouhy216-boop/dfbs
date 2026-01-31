package com.dfbs.app.application.carrier;

import com.dfbs.app.modules.carrier.CarrierEntity;
import com.dfbs.app.modules.carrier.CarrierRepo;
import com.dfbs.app.modules.carrier.CarrierRuleEntity;
import com.dfbs.app.modules.carrier.CarrierRuleRepo;
import com.dfbs.app.modules.shipment.ShipmentType;
import com.dfbs.app.application.shipment.ShipmentTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CarrierTest {

    @Autowired
    private CarrierService carrierService;

    @Autowired
    private ShipmentTypeService shipmentTypeService;

    @Autowired
    private CarrierRepo carrierRepo;

    @Autowired
    private CarrierRuleRepo carrierRuleRepo;

    /** Test 1 (Carrier Rec): Rule "Beijing" -> Carrier A, "Shanghai" -> Carrier B. "Beijing Chaoyang" -> A, "Shenzhen" -> null. */
    @Test
    void test1_carrierRecommend_byAddress() {
        CarrierEntity carrierA = new CarrierEntity();
        carrierA.setName("Carrier A");
        carrierA.setIsActive(true);
        carrierA = carrierRepo.save(carrierA);

        CarrierEntity carrierB = new CarrierEntity();
        carrierB.setName("Carrier B");
        carrierB.setIsActive(true);
        carrierB = carrierRepo.save(carrierB);

        CarrierRuleEntity ruleA = new CarrierRuleEntity();
        ruleA.setCarrier(carrierA);
        ruleA.setMatchKeyword("Beijing");
        ruleA.setPriority(10);
        carrierRuleRepo.save(ruleA);

        CarrierRuleEntity ruleB = new CarrierRuleEntity();
        ruleB.setCarrier(carrierB);
        ruleB.setMatchKeyword("Shanghai");
        ruleB.setPriority(10);
        carrierRuleRepo.save(ruleB);

        CarrierEntity recommended = carrierService.recommendCarrier("Beijing Chaoyang");
        assertThat(recommended).isNotNull();
        assertThat(recommended.getName()).isEqualTo("Carrier A");

        assertThat(carrierService.recommendCarrier("Shenzhen")).isNull();
    }

    /** Test 2 (Type Inference): Text with "借用" -> SALES_DELEGATE. Random text -> null. */
    @Test
    void test2_typeInference_keywords() {
        assertThat(shipmentTypeService.inferType("借用设备给客户")).isEqualTo(ShipmentType.SALES_DELEGATE);
        assertThat(shipmentTypeService.inferType("归还测试机")).isEqualTo(ShipmentType.SALES_DELEGATE);
        assertThat(shipmentTypeService.inferType("客户委托 发货")).isEqualTo(ShipmentType.CUSTOMER_DELEGATE);
        assertThat(shipmentTypeService.inferType("random text without keywords")).isNull();
    }

    /** Test 3 (Billable Logic): CUSTOMER_DELEGATE -> true, NORMAL -> false. */
    @Test
    void test3_billableLogic() {
        assertThat(shipmentTypeService.isBillable(ShipmentType.CUSTOMER_DELEGATE)).isTrue();
        assertThat(shipmentTypeService.isBillable(ShipmentType.NORMAL)).isFalse();
        assertThat(shipmentTypeService.isBillable(ShipmentType.SALES_DELEGATE)).isFalse();
        assertThat(shipmentTypeService.isBillable(ShipmentType.PRODUCTION_DELEGATE)).isFalse();
    }
}
