package com.codewizards.meshify.framework.expections;

public class ConnectionException extends Exception {
    public ConnectionException(String str, Exception e) {
        super(str, e);
    }

    public ConnectionException(String str) {
        super(str);
    }

    public ConnectionException(Exception e) {
        super(e);
    }
}
