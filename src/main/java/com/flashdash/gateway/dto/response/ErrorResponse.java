package com.flashdash.gateway.dto.response;

public class ErrorResponse {

    private int status;
    private String error;
    private String cause;
    private String action;
    private String correlationId;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String cause, String action, String correlationId) {
        this.status = status;
        this.error = error;
        this.cause = cause;
        this.action = action;
        this.correlationId = correlationId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}