package org.openhab.binding.cololight.internal.exception;

public class CommunicationException extends Exception {
    static final long serialVersionUID = 1L;

    public CommunicationException(String message) {
        super(message);
    }
}
