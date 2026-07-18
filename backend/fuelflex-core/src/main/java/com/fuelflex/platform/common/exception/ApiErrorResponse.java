package com.fuelflex.platform.common.exception;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponse {

    private OffsetDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;
}