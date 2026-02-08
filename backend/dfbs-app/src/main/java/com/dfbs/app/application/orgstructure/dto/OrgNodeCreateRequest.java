package com.dfbs.app.application.orgstructure.dto;

public record OrgNodeCreateRequest(Long levelId, Long parentId, String name, String remark, Boolean isEnabled) {}
