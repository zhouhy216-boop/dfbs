package com.dfbs.app.application.orgstructure.dto;

import java.util.List;

public record PositionBindingsUpdateRequest(Long orgNodeId, Long positionId, List<Long> personIds) {}
