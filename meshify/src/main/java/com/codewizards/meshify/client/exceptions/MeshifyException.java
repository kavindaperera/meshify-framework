package com.codewizards.meshify.client.exceptions;

public class MeshifyException extends RuntimeException {

    private int errorCode;

    public MeshifyException(Throwable cause) {
        super(cause);
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public MeshifyException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
