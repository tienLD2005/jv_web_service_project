package com.tien.project.dto.response;

public class ErrorDetail {
    private String field;
    private String message;

    public ErrorDetail(String message) {
        this.message = message;
        this.field = null;
    }

    public ErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}