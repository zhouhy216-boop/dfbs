package com.dfbs.app.application.masterdata;

import com.dfbs.app.application.iccid.IccidMasterDataService;
import com.dfbs.app.application.iccid.dto.IccidListDto;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.iccid.IccidEntity;
import com.dfbs.app.modules.iccid.IccidRepo;
import com.dfbs.app.modules.masterdata.ContractEntity;
import com.dfbs.app.modules.masterdata.ContractRepo;
import com.dfbs.app.modules.masterdata.MachineEntity;
import com.dfbs.app.modules.masterdata.MachineRepo;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import com.dfbs.app.modules.product.ProductEntity;
import com.dfbs.app.modules.product.ProductRepo;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import com.dfbs.app.modules.shipment.ShipmentType;
import com.dfbs.app.modules.shipment.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MasterDataListTest {

    private static final String CONTRACT_NO = "C1";
    private static final String PRODUCT_CODE = "P1";
    private static final String CUSTOMER_CODE = "CUST-MDLIST";

    @Autowired
    private IccidMasterDataService iccidMasterDataService;

    @Autowired
    private MachineRepo machineRepo;

    @Autowired
    private IccidRepo iccidRepo;

    @Autowired
    private ShipmentRepo shipmentRepo;

    @Autowired
    private ShipmentMachineRepo shipmentMachineRepo;

    @Autowired
    private ContractRepo contractRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ProductRepo productRepo;

    @BeforeEach
    void ensureContractAndProductExist() {
        if (customerRepo.findByCustomerCode(CUSTOMER_CODE).isEmpty()) {
            customerRepo.save(CustomerEntity.create(CUSTOMER_CODE, "Test Customer"));
        }
        if (contractRepo.findByContractNo(CONTRACT_NO).isEmpty()) {
            CustomerEntity customer = customerRepo.findByCustomerCode(CUSTOMER_CODE).orElseThrow();
            ContractEntity contract = new ContractEntity();
            contract.setContractNo(CONTRACT_NO);
            contract.setCustomerId(customer.getId());
            contract.setAttachment("{}");
            contract.setStatus(MasterDataStatus.ENABLE);
            contractRepo.save(contract);
        }
        if (productRepo.findByProductCode(PRODUCT_CODE).isEmpty()) {
            ProductEntity product = new ProductEntity();
            product.setId(UUID.randomUUID());
            product.setProductCode(PRODUCT_CODE);
            product.setName("Test Product");
            product.setStatus("ACTIVE");
            product.setCreatedAt(OffsetDateTime.now());
            product.setUpdatedAt(OffsetDateTime.now());
            productRepo.save(product);
        }
    }

    /** Test 2 (ICCID List): Create ICCID bound to SN-ICCID. Search ICCID -> customerName matches; plan, platform, expiryDate persist. */
    @Test
    void iccidList_boundCustomerNameAndPlanPlatformExpiry() {
        MachineEntity m1 = new MachineEntity();
        m1.setMachineNo("SN-ICCID");
        m1.setSerialNo("SN-ICCID");
        m1.setStatus(MasterDataStatus.ENABLE);
        machineRepo.save(m1);

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setInitiatorId(1L);
        shipment.setType(ShipmentType.CUSTOMER_DELEGATE);
        shipment.setApprovalStatus(ApprovalStatus.APPROVED);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setReceiverName("ICCID Customer");
        shipment.setContractNo("CON-ICCID");
        shipment = shipmentRepo.save(shipment);

        ShipmentMachineEntity sm = new ShipmentMachineEntity();
        sm.setShipmentId(shipment.getId());
        sm.setMachineNo("SN-ICCID");
        sm.setModel("M1");
        shipmentMachineRepo.save(sm);

        IccidEntity iccid = new IccidEntity();
        iccid.setId(UUID.randomUUID());
        iccid.setIccidNo("ICCID-TEST-001");
        iccid.setMachineSn("SN-ICCID");
        LocalDate expiryDate = LocalDate.now().plusMonths(6);
        iccid.setPlan("100MB/Month");
        iccid.setPlatform("China Mobile");
        iccid.setExpiryDate(expiryDate);
        iccid.setStatus("ACTIVE");
        iccid.setCreatedAt(OffsetDateTime.now());
        iccid.setUpdatedAt(OffsetDateTime.now());
        iccidRepo.save(iccid);

        Page<IccidListDto> page = iccidMasterDataService.searchIccids("ICCID-TEST", null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        IccidListDto dto = page.getContent().get(0);
        assertThat(dto.iccid()).isEqualTo("ICCID-TEST-001");
        assertThat(dto.boundMachineNo()).isEqualTo("SN-ICCID");
        assertThat(dto.isBound()).isTrue();
        assertThat(dto.customerName()).isEqualTo("ICCID Customer");
        assertThat(dto.contractNo()).isEqualTo("CON-ICCID");
        assertThat(dto.plan()).isEqualTo("100MB/Month");
        assertThat(dto.platform()).isEqualTo("China Mobile");
        assertThat(dto.expiryDate()).isEqualTo(expiryDate);
    }
}
