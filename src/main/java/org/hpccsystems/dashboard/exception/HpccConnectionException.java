package org.hpccsystems.dashboard.exception;

public class HpccConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    public HpccConnectionException() {

    }

    public HpccConnectionException(String message) {
        super(message);
    }
}