package com.finance.dashboard.enums;

public enum Role {
    VIEWER,    // read-only: dashboard & records
    ANALYST,   // read + summary analytics
    ADMIN      // full CRUD on records and users
}
