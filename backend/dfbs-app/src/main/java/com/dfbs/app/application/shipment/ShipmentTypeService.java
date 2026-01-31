package com.dfbs.app.application.shipment;

import com.dfbs.app.modules.shipment.ShipmentType;
import org.springframework.stereotype.Service;

@Service
public class ShipmentTypeService {

    /**
     * Infer shipment type from raw pasted text. Heuristic: delegate-like structure + keywords.
     * Returns null if ambiguous (force manual selection).
     */
    public ShipmentType inferType(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;
        String t = rawText.trim();
        if (t.contains("借用") || t.contains("归还") || t.contains("测试机")) {
            return ShipmentType.SALES_DELEGATE;
        }
        if (t.contains("客户委托")) {
            return ShipmentType.CUSTOMER_DELEGATE;
        }
        return null;
    }

    /**
     * Only CUSTOMER_DELEGATE is billable to customer (income + advance).
     */
    public boolean isBillable(ShipmentType type) {
        return type == ShipmentType.CUSTOMER_DELEGATE;
    }
}
