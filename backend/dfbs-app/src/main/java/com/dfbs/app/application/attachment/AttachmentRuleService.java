package com.dfbs.app.application.attachment;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Central validation for mandatory attachment rules.
 * Throws IllegalArgumentException with a clear message if validation fails.
 */
@Service
public class AttachmentRuleService {

    private static final int MAX_URLS_PER_BATCH = 10;

    /**
     * Validate that the given URLs satisfy the rule for (target, point).
     * Common rule: max 10 URLs per batch.
     *
     * @param target Business flow
     * @param point  Attachment point
     * @param urls   List of attachment URLs (may be null or empty)
     * @throws IllegalArgumentException if validation fails
     */
    public void validate(AttachmentTargetType target, AttachmentPoint point, List<String> urls) {
        List<String> list = urls == null ? List.of() : urls.stream()
                .filter(u -> u != null && !u.isBlank())
                .collect(Collectors.toList());

        if (list.size() > MAX_URLS_PER_BATCH) {
            throw new IllegalArgumentException("Too many attachments: max " + MAX_URLS_PER_BATCH + " per batch.");
        }

        boolean required = isRequired(target, point);
        if (required && list.isEmpty()) {
            String ruleName = getRuleName(target, point);
            throw new IllegalArgumentException("Missing required attachment: " + ruleName + ".");
        }
    }

    private boolean isRequired(AttachmentTargetType target, AttachmentPoint point) {
        return switch (target) {
            case FREIGHT_BILL -> point == AttachmentPoint.CONFIRM;
            case HQ_TRANSFER -> point == AttachmentPoint.EXECUTE;
            case SHIPMENT_NORMAL -> point == AttachmentPoint.SHIP_PICK_TICKET || point == AttachmentPoint.COMPLETE_RECEIPT;
            case DAMAGE_RECORD -> point == AttachmentPoint.CREATE;
            case CORRECTION -> point == AttachmentPoint.SUBMIT;
        };
    }

    private String getRuleName(AttachmentTargetType target, AttachmentPoint point) {
        return switch (target) {
            case FREIGHT_BILL -> point == AttachmentPoint.CONFIRM ? "Bill Photo" : "";
            case HQ_TRANSFER -> point == AttachmentPoint.EXECUTE ? "Logistics Bill" : "";
            case SHIPMENT_NORMAL -> point == AttachmentPoint.SHIP_PICK_TICKET ? "Pick Ticket"
                    : point == AttachmentPoint.COMPLETE_RECEIPT ? "Receipt" : "";
            case DAMAGE_RECORD -> point == AttachmentPoint.CREATE ? "Damage Photo" : "";
            case CORRECTION -> point == AttachmentPoint.SUBMIT ? "Correction Attachment" : "";
        };
    }
}
