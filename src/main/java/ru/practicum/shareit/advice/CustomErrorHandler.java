package ru.practicum.shareit.advice;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.shareit.advice.exception.AccessDeniedException;
import ru.practicum.shareit.advice.exception.EntityNotFoundException;
import ru.practicum.shareit.advice.response.ApiError;
import ru.practicum.shareit.advice.response.ValidationErrorResponse;
import ru.practicum.shareit.advice.response.Violation;
import ru.practicum.shareit.exception.DuplicateEmailException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = {"ru.practicum.shareit.user.controller",
        "ru.practicum.shareit.item.controller"})
public class CustomErrorHandler extends ResponseEntityExceptionHandler {
    //400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatusCode status,
                                                                  final WebRequest request) {
        final List<Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(violations);
        return handleExceptionInternal(ex, validationErrorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ApiError handleValidationException(final ValidationException e) {
        return new ApiError(HttpStatus.BAD_REQUEST, "Ошибка валидации", e.getLocalizedMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateEmailException(DuplicateEmailException e) {
        return new ApiError(HttpStatus.BAD_REQUEST,
                "Email уже есть в бд",
                e.getLocalizedMessage());
    }

    //404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ApiError handleEntityNotFoundException(final EntityNotFoundException e) {
        return new ApiError(HttpStatus.NOT_FOUND, "Обьект не найден", e.getLocalizedMessage());
    }

    //403
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiError handleAccessDeniedException(final AccessDeniedException e) {
        return new ApiError(HttpStatus.FORBIDDEN, "Отказано в доступе", e.getLocalizedMessage());
    }

    //500
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiError handleAll(final Exception ex, final WebRequest request) {
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), "Произошла ошибка");
    }
}
