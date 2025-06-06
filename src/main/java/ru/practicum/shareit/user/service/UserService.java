package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.advice.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto find(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        return UserMapper.toUserDto(user);
    }

    public Collection<UserDto> findAll() {
        Collection<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto create(UserDto userDto) {
        try {
            return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() != null && e.getRootCause().getMessage().contains("duplicate key")) {
                throw new DuplicateEmailException("Email уже используется");
            }
            throw e;
        }
    }

    public UserDto update(UserDto userDto, Long userId) {
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        if (userDto.getName() != null) updateUser.setName(userDto.getName());
        if (userDto.getEmail() != null) updateUser.setEmail(userDto.getEmail());

        try {
            return UserMapper.toUserDto(userRepository.save(updateUser));
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() != null && e.getRootCause().getMessage().contains("duplicate key")) {
                throw new DuplicateEmailException("Email уже используется");
            }
            throw e;
        }
    }

    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }
}
