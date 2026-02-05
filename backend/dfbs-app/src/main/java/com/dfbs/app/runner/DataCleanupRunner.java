package com.dfbs.app.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runner that truncates business data on startup when profile "cleanup" is active (e.g. Data Governance testing).
 * Normal startup (no "cleanup" profile) does not run this.
 */
@Component
@Profile("cleanup")
public class DataCleanupRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataCleanupRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("TRUNCATE TABLE work_order RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE platform_account_applications RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE platform_org RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE md_customer RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE contracts RESTART IDENTITY CASCADE");
        System.out.println("=========== DATABASE CLEARED SUCCESSFULLY ===========");
    }
}
