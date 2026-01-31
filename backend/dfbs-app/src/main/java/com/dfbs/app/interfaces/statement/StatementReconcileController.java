package com.dfbs.app.interfaces.statement;

import com.dfbs.app.application.statement.StatementReconcileService;
import com.dfbs.app.interfaces.payment.BindPaymentsRequest;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statements")
public class StatementReconcileController {

    private final StatementReconcileService statementReconcileService;

    public StatementReconcileController(StatementReconcileService statementReconcileService) {
        this.statementReconcileService = statementReconcileService;
    }

    @PostMapping("/{id}/bind-payments")
    public AccountStatementEntity bindPayments(@PathVariable Long id, @RequestBody BindPaymentsRequest body) {
        List<Long> paymentIds = body != null && body.paymentIds() != null ? body.paymentIds() : List.of();
        return statementReconcileService.bindPayments(id, paymentIds);
    }
}
