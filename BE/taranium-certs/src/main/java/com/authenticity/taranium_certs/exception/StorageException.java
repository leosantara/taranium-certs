package com.authenticity.taranium_certs.exception;

/**
 * Custom exception untuk menangani masalah terkait penyimpanan file.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
