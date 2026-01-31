package com.dfbs.app.modules.quote.void_;

public enum VoidRequestStage {
    INITIATOR,  // Waiting for Initiator approval
    FINANCE,    // Waiting for Finance approval
    LEADER      // Waiting for Leader approval
}
