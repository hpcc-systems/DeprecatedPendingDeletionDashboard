package org.hpccsystems.dashboard.exception;

/**
 * Exception class to handle exceptions while Encrypt/Decrypting data
 *
 */
public class EncryptDecryptException extends Exception{
    
    private static final long serialVersionUID = 1L;

    public EncryptDecryptException(String message) {
        super(message);
    }

}
