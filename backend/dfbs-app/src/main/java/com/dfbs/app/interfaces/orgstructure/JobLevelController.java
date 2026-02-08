package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.JobLevelService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.JobLevelEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org-structure/job-levels")
public class JobLevelController {

    private final JobLevelService service;
    private final SuperAdminGuard superAdminGuard;

    public JobLevelController(JobLevelService service, SuperAdminGuard superAdminGuard) {
        this.service = service;
        this.superAdminGuard = superAdminGuard;
    }

    @GetMapping
    public List<JobLevelEntity> list() {
        superAdminGuard.requireSuperAdmin();
        return service.listOrdered();
    }
}
