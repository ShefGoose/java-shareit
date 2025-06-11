package ru.practicum.shareit.advice.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityType, Long id) {
        super(String.format("%s с ID_%s не найден", entityType, id));
    }
}
