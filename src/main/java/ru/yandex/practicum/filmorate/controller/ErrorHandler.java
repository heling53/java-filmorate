package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(ValidationException e) {
        log.warn("400 Bad Request (ValidationException): {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Optional<FieldError> notBlankError = e.getBindingResult().getFieldErrors().stream()
                .filter(error -> "NotBlank".equals(error.getCode()))
                .findFirst();

        String message = notBlankError
                .map(FieldError::getDefaultMessage)
                .orElseGet(() -> e.getBindingResult().getAllErrors().get(0).getDefaultMessage());

        log.warn("Ошибка валидации: {}", message);
        return Map.of("error", message);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        log.warn("404 Not Found: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler({EmptyResultDataAccessException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEmptyResultDataAccess(Exception e) {
        log.warn("404 Not Found (Data access): {}", e.getMessage());
        return Map.of("error", "Запрашиваемый объект не найден");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // Или BAD_REQUEST, зависит от логики
    public Map<String, String> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Конфликт данных в БД: {}", e.getMessage());
        return Map.of("error", "Нарушение целостности данных");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOther(Exception e) {
        log.error("500 Internal Server Error: ", e);
        return Map.of("error", "Внутренняя ошибка сервера");
    }
}