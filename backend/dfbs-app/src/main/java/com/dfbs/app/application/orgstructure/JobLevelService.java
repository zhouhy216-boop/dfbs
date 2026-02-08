package com.dfbs.app.application.orgstructure;

import com.dfbs.app.modules.orgstructure.JobLevelEntity;
import com.dfbs.app.modules.orgstructure.JobLevelRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobLevelService {

    private final JobLevelRepo repo;

    public JobLevelService(JobLevelRepo repo) {
        this.repo = repo;
    }

    public List<JobLevelEntity> listOrdered() {
        return repo.findAllByOrderByOrderIndexAsc();
    }
}
