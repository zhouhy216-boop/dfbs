package com.dfbs.app.application.settings;

import com.dfbs.app.modules.settings.WarehouseConfigEntity;
import com.dfbs.app.modules.settings.WarehouseConfigRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WarehouseConfigService {

    private static final Long CONFIG_ID = 1L;  // Singleton config ID

    private final WarehouseConfigRepo repo;

    public WarehouseConfigService(WarehouseConfigRepo repo) {
        this.repo = repo;
    }

    /**
     * Get warehouse user IDs.
     * 
     * @return List of user IDs
     */
    @Transactional(readOnly = true)
    public List<Long> getWarehouseUserIds() {
        WarehouseConfigEntity config = repo.findById(CONFIG_ID).orElse(null);
        if (config == null || config.getUserIds() == null || config.getUserIds().trim().isEmpty()) {
            return new ArrayList<>();
        }
        return parseUserIds(config.getUserIds());
    }

    /**
     * Update warehouse user IDs (Admin method).
     * 
     * @param userIds List of user IDs
     * @return Updated config
     */
    @Transactional
    public WarehouseConfigEntity updateWarehouseUserIds(List<Long> userIds) {
        WarehouseConfigEntity config = repo.findById(CONFIG_ID).orElse(null);
        if (config == null) {
            config = new WarehouseConfigEntity();
            config.setId(CONFIG_ID);
        }
        
        // Convert list to JSON array format
        String userIdsStr = formatUserIds(userIds);
        config.setUserIds(userIdsStr);
        
        return repo.save(config);
    }

    /**
     * Parse user IDs from string (supports JSON array or comma-separated).
     * 
     * @param userIdsStr String containing user IDs
     * @return List of user IDs
     */
    private List<Long> parseUserIds(String userIdsStr) {
        List<Long> userIds = new ArrayList<>();
        String trimmed = userIdsStr.trim();
        
        // Try JSON array format first (e.g., "[1,2,3]")
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (!inner.isEmpty()) {
                String[] parts = inner.split(",");
                for (String part : parts) {
                    try {
                        userIds.add(Long.parseLong(part.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        } else {
            // Comma-separated format (e.g., "1,2,3")
            String[] parts = trimmed.split(",");
            for (String part : parts) {
                try {
                    userIds.add(Long.parseLong(part.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
        
        return userIds;
    }

    /**
     * Format user IDs list to JSON array string.
     * 
     * @param userIds List of user IDs
     * @return JSON array string
     */
    private String formatUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(userIds.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
