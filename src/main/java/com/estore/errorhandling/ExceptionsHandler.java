package com.estore.errorhandling;

import com.estore.exception.ModelNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * {@link ExceptionsHandler} is the class for customizing a message for an invalid field
 *
 * @author Dmytro Trotsenko on 5/27/23
 */

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<List<String>> handleValidationException(WebExchangeBindException e) {

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

    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<Map<String, List<String>>> handleNotFoundException(ModelNotFoundException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, List<String>>> handleDuplicateKeyException(DuplicateKeyException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

}
