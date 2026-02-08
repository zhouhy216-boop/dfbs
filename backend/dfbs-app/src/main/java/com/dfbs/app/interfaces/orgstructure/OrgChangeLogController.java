package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgChangeLogService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.OrgChangeLogEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/org-structure/change-logs")
public class OrgChangeLogController {

    private final OrgChangeLogService service;
    private final SuperAdminGuard superAdminGuard;

    public OrgChangeLogController(OrgChangeLogService service, SuperAdminGuard superAdminGuard) {
        this.service = service;
        this.superAdminGuard = superAdminGuard;
    }

    @GetMapping
    public Page<OrgChangeLogEntity> list(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        superAdminGuard.requireSuperAdmin();
        Instant fromInst = from == null ? null : from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInst = to == null ? null : to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return service.list(objectType, operatorId, fromInst, toInst, pageable);
    }
}
