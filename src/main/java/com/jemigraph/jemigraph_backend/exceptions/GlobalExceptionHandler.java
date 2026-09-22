package com.jemigraph.jemigraph_backend.exceptions;

import com.jemigraph.jemigraph_backend.DTO.ApiErrorResponseDTO;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed: " + errors,
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication failed: " + ex.getMessage(),
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleUserExists(UserAlreadyExistsException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.CONFLICT.value(), ex.getMessage(), LocalDateTime.now(), null);
    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NoPhotographersAvailableException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleNoPhotographers(
      NoPhotographersAvailableException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.NOT_FOUND.value(),
            "Radar Empty: " + ex.getMessage(),
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(PendingVerificationException.class)
  public ResponseEntity<ApiErrorResponseDTO> handlePendingVerification(
      PendingVerificationException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.FORBIDDEN.value(),
            "Account approval required: " + ex.getMessage(),
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NotPhotographerException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleNotAphotographer(NotPhotographerException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.FORBIDDEN.value(),
            "Only Photographers can Go live: " + ex.getMessage(),
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(SuperAdminException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleSuperadmin(SuperAdminException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.FORBIDDEN.value(),
            "You don't have role to add super admin on the system: " + ex.getMessage(),
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(LockedException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleLockedAccountr(LockedException ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.LOCKED.value(),
            "Account suspended due to debt: Your account has been locked because your debt has exceeded 7 days. Please clear your balance to continue.",
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.LOCKED);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponseDTO> handleGeneralException(Exception ex) {
    ApiErrorResponseDTO response =
        new ApiErrorResponseDTO(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected server error occurred: " + ex.getMessage(),
            LocalDateTime.now(),
            null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DuplicatePackageLevelException.class)
  public ResponseEntity<ApiErrorResponseDTO> handleDuplicatePackageLevel(
          DuplicatePackageLevelException ex) {
    ApiErrorResponseDTO error = new ApiErrorResponseDTO(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            null
    );
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  public static class DuplicatePackageNameException extends RuntimeException {
    public DuplicatePackageNameException(String message) {
      super(message);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUsernameNotFound(
        UsernameNotFoundException ex) {
      ApiErrorResponseDTO error =
          new ApiErrorResponseDTO(
              HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now(), null);
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
  }
}
