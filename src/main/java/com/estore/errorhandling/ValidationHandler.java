package com.estore.errorhandling;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.List;
import java.util.stream.IntStream;

/**
 * {@link ValidationHandler} is the class for customizing a message for an invalid field
 *
 * @author Dmytro Trotsenko on 5/27/23
 */

@ControllerAdvice
public class ValidationHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<List<String>> handleException(WebExchangeBindException e) {

        //Create custom error message
        var customErrMessage = IntStream.range(0, e.getBindingResult().getAllErrors().size())
                .mapToObj(i -> {
                    String message = e.getBindingResult().getAllErrors().get(i).getDefaultMessage();
                    String field = e.getFieldErrors().get(i).getField();
                    return String.format("Invalid '%s': %s", field, message);
                })
                .toList();

        return ResponseEntity.badRequest().body(customErrMessage);
    }

}
