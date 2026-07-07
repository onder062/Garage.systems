package com.onder.garage.exception;

/**
 * Base exception type for garage domain failures.
 */
public class GarageException extends RuntimeException {
    public GarageException(String message) {
        super(message);
    }
}
