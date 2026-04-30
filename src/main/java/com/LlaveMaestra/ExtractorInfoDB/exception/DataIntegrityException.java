package com.LlaveMaestra.ExtractorInfoDB.exception;

public class DataIntegrityException extends RuntimeException {

    private final String errorCode;

    public DataIntegrityException(String message) {
        super(message);
        this.errorCode = "DATA_INTEGRITY_ERROR";
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DATA_INTEGRITY_ERROR";
    }

    public DataIntegrityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DataIntegrityException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}