package com.dfbs.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dfbs.company")
public class CompanyInfoProperties {

    private String name = "DFBS 示例公司";
    private String bankName = "示例银行";
    private String bankNo = "BANK001";
    private String taxNo = "91110000MA0000000X";
    private String accountNo = "1234567890123456";
    private String phone = "400-000-0000";
    private String address = "示例地址";
}
