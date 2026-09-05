package com.jemigraph.jemigraph_backend.exceptions;

import com.jemigraph.jemigraph_backend.DTO.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors.toString()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUserExists(UserAlreadyExistsException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(NoPhotographersAvailableException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNoPhotographers(NoPhotographersAvailableException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Radar Empty: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGeneralException(Exception ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected server error occurred: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(PendingVerificationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handlePendingVerification(PendingVerificationException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Account approval required",
                ex.getMessage() 
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(NotPhotographerException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNotAphotographer(PendingVerificationException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Only Photographers can Go live",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SuperAdminException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleSuperadmin(SuperAdminException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "You don't have role to add super admin on the system",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleLockedAccountr(LockedException ex) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.LOCKED.value(), // 423 Locked
                "Account suspended due to debt",
                "Your account has been locked because your debt has exceeded 7 days. Please clear your balance to continue."
        );
        return new ResponseEntity<>(response, HttpStatus.LOCKED);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicatePackageNameException extends RuntimeException {
        public DuplicatePackageNameException(String message) {
            super(message);
        }
    }
}