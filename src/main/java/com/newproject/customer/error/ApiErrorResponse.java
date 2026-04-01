package com.newproject.customer.error;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    String reference
) {
}
