package ru.practicum.shareit.advice.exception;

public class CommentCreationException extends RuntimeException {
    public CommentCreationException(String message) {
        super(message);
    }
}
