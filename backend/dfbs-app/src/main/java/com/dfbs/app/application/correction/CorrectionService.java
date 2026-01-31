package com.dfbs.app.application.correction;

import com.dfbs.app.application.attachment.AttachmentPoint;
import com.dfbs.app.application.attachment.AttachmentRuleService;
import com.dfbs.app.application.attachment.AttachmentTargetType;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.correction.CorrectionEntity;
import com.dfbs.app.modules.correction.CorrectionRepo;
import com.dfbs.app.modules.correction.CorrectionStatus;
import com.dfbs.app.modules.correction.CorrectionTargetType;
import com.dfbs.app.modules.user.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CorrectionService {

    private static final String CORRECTION_NO_PREFIX = "COR-";

    private final CorrectionRepo correctionRepo;
    private final CorrectionExecutorFactory executorFactory;
    private final AttachmentRuleService attachmentRuleService;
    private final CurrentUserIdResolver userIdResolver;

    public CorrectionService(CorrectionRepo correctionRepo,
                              CorrectionExecutorFactory executorFactory,
                              AttachmentRuleService attachmentRuleService,
                              CurrentUserIdResolver userIdResolver) {
        this.correctionRepo = correctionRepo;
        this.executorFactory = executorFactory;
        this.attachmentRuleService = attachmentRuleService;
        this.userIdResolver = userIdResolver;
    }

    @Transactional
    public CorrectionEntity createDraft(CreateCorrectionDto dto) {
        if (dto.targetType() == null || dto.targetId() == null)
            throw new IllegalStateException("targetType and targetId are required");
        if (dto.reason() == null || dto.reason().isBlank())
            throw new IllegalStateException("reason is required");
        if (dto.occurredDate() == null)
            throw new IllegalStateException("occurredDate is required");

        String correctionNo = generateCorrectionNo();
        CorrectionEntity e = new CorrectionEntity();
        e.setCorrectionNo(correctionNo);
        e.setTargetType(dto.targetType());
        e.setTargetId(dto.targetId());
        e.setReason(dto.reason().trim());
        e.setStatus(CorrectionStatus.DRAFT);
        e.setChangesJson(dto.changesJson());
        e.setOccurredDate(dto.occurredDate());
        e.setCreatedBy(userIdResolver.getCurrentUserId());
        e.setCreatedAt(LocalDateTime.now());
        return correctionRepo.save(e);
    }

    @Transactional
    public CorrectionEntity submit(Long id, List<String> attachmentUrls) {
        CorrectionEntity c = correctionRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Correction not found: id=" + id));
        if (c.getStatus() != CorrectionStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT correction can be submitted");
        if (c.getReason() == null || c.getReason().isBlank())
            throw new IllegalStateException("reason is required");
        attachmentRuleService.validate(AttachmentTargetType.CORRECTION, AttachmentPoint.SUBMIT, attachmentUrls);
        c.setStatus(CorrectionStatus.SUBMITTED);
        c.setUpdatedAt(LocalDateTime.now());
        return correctionRepo.save(c);
    }

    @Transactional
    public CorrectionEntity approveAndExecute(Long id) {
        CorrectionEntity c = correctionRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Correction not found: id=" + id));
        if (c.getStatus() != CorrectionStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED correction can be approved");

        UserEntity operator = userIdResolver.getCurrentUserEntity();
        String auth = operator.getAuthorities();
        if (auth == null || !auth.contains("APPROVE_EXECUTE_CORRECTION")) {
            throw new SecurityException("Forbidden: APPROVE_EXECUTE_CORRECTION required");
        }

        CorrectionExecutor executor = executorFactory.get(c.getTargetType());
        executor.voidOld(c.getTargetId());
        Long newId = executor.createNew(c.getTargetId(), c.getChangesJson(), operator.getId());
        c.setNewRecordId(newId);
        c.setStatus(CorrectionStatus.EXECUTED);
        c.setApprovedBy(operator.getId());
        c.setApprovedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return correctionRepo.save(c);
    }

    @Transactional
    public CorrectionEntity reject(Long id) {
        CorrectionEntity c = correctionRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Correction not found: id=" + id));
        if (c.getStatus() != CorrectionStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED correction can be rejected");
        c.setStatus(CorrectionStatus.REJECTED);
        c.setUpdatedAt(LocalDateTime.now());
        return correctionRepo.save(c);
    }

    @Transactional(readOnly = true)
    public CorrectionEntity get(Long id) {
        return correctionRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Correction not found: id=" + id));
    }

    public boolean hasApproveExecutePermission() {
        UserEntity user = userIdResolver.getCurrentUserEntity();
        String auth = user.getAuthorities();
        return auth != null && auth.contains("APPROVE_EXECUTE_CORRECTION");
    }

    private String generateCorrectionNo() {
        String base = CORRECTION_NO_PREFIX + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String candidate = base + "-0001";
        int suffix = 1;
        while (correctionRepo.existsByCorrectionNo(candidate)) {
            candidate = base + "-" + String.format("%04d", ++suffix);
        }
        return candidate;
    }

    public record CreateCorrectionDto(CorrectionTargetType targetType, Long targetId, String reason, String changesJson, LocalDate occurredDate) {}
}
