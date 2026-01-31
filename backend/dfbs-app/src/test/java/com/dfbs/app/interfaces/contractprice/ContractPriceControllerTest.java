package com.dfbs.app.interfaces.contractprice;

import com.dfbs.app.application.contractprice.ContractPriceService;
import com.dfbs.app.config.CurrentUserProvider;
import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class ContractPriceControllerTest {

    @Autowired
    private ContractPriceController contractPriceController;
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private Long customerId;

    @BeforeEach
    void setUp() {
        CustomerEntity customer = CustomerEntity.create("PB-CTRL-" + System.currentTimeMillis(), "ContractPrice Controller Test");
        customer = customerRepo.save(customer);
        customerId = customer.getId();
    }

    /** Non-admin user tries to create contract -> 403. */
    @Test
    void permission_nonAdmin_createContract_fails() {
        UserEntity plain = new UserEntity();
        plain.setCanRequestPermission(false);
        plain.setAuthorities("[\"ROLE_USER\"]");
        plain.setAllowNormalNotification(true);
        plain.setCanManageStatements(false);
        plain = userRepo.save(plain);
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(plain.getId()));

        var req = new ContractPriceController.CreateContractRequest(
                "Test", customerId, LocalDate.now(), null, 0,
                List.of(new ContractPriceController.ItemRequest(QuoteExpenseType.PLATFORM, new BigDecimal("10"), Currency.CNY)));
        assertThatThrownBy(() -> contractPriceController.create(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }
}
