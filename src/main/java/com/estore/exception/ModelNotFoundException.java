package com.estore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@link ModelNotFoundException}
 *
 * @author Dmytro Trotsenko on 6/3/23
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(String message) {
        super(message);
    }
    public ModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
