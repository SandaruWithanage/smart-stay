package lk.smartstay.shared;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> missing(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, Object>> conflict(Exception e) {
        // Do not leak database constraints, SQL or user records.
        return error(HttpStatus.CONFLICT, e instanceof ConflictException
            ? e.getMessage() : "Record conflicts with an existing record or reservation");
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
        MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> badInput(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "Invalid input or business-rule violation");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", code.value());
        body.put("error", code.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(code).body(body);
    }
}
