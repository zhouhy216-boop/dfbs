package com.dfbs.app.interfaces.statement;

import com.dfbs.app.application.statement.AccountStatementService;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import com.dfbs.app.modules.statement.StatementStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statements")
public class AccountStatementController {

    private final AccountStatementService statementService;

    public AccountStatementController(AccountStatementService statementService) {
        this.statementService = statementService;
    }

    private void requireCanManageStatements(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (!statementService.hasManagementPermission(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无对账单管理权限");
        }
    }

    /**
     * Generate a statement from selected quotes for a customer.
     * Body: { customerId, quoteIds }. creatorId as param for permission.
     */
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountStatementEntity generate(@RequestBody GenerateRequest body, @RequestParam Long creatorId) {
        requireCanManageStatements(creatorId);
        return statementService.generate(body.customerId(), body.quoteIds(), creatorId);
    }

    /**
     * Remove one quote from statement. Only when status is PENDING.
     */
    @DeleteMapping("/{id}/items/{quoteId}")
    public AccountStatementEntity removeItem(@PathVariable Long id, @PathVariable Long quoteId,
                                             @RequestParam Long userId) {
        requireCanManageStatements(userId);
        return statementService.removeItem(id, quoteId);
    }

    /**
     * Download statement as Excel.
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id, @RequestParam Long userId) {
        requireCanManageStatements(userId);
        byte[] bytes = statementService.exportExcel(id);
        AccountStatementEntity st = statementService.getById(id);
        String filename = "statement_" + st.getStatementNo() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * List statements with optional filter by customer and status.
     */
    @GetMapping("/list")
    public List<AccountStatementEntity> list(@RequestParam Long userId,
                                             @RequestParam(required = false) Long customerId,
                                             @RequestParam(required = false) String status) {
        requireCanManageStatements(userId);
        StatementStatus stStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                stStatus = StatementStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return statementService.list(customerId, stStatus);
    }

    @GetMapping("/{id}")
    public AccountStatementEntity getById(@PathVariable Long id, @RequestParam Long userId) {
        requireCanManageStatements(userId);
        return statementService.getById(id);
    }

    public record GenerateRequest(Long customerId, List<Long> quoteIds) {}
}
