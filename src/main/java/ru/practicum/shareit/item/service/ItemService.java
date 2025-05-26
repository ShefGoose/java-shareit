package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUserDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemUserMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemDto find(Long itemId) {
        Item item = itemRepository.read(itemId);
        if (item == null) {
            throw new NoSuchElementException("Предмет с ID_" + itemId + " не найден");
        }
        return ItemMapper.toItemDto(item);
    }

    public Collection<ItemUserDto> findAll(Long userId) {
        if (userRepository.read(userId) == null) {
            throw new NoSuchElementException("Пользователь с ID_" + userId + " не найден");
        }

        Collection<Item> items = itemRepository.readAll(userId);

        return items.stream()
                .map(ItemUserMapper::toItemUserDto)
                .collect(Collectors.toList());
    }

    public ItemDto create(ItemDto itemDto, Long userId) {
        if (userRepository.read(userId) == null) {
            throw new NoSuchElementException("Пользователь с ID_" + userId + " не найден");
        }
        return ItemMapper.toItemDto(itemRepository.create(itemDto, userId));
    }

    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        Item itemUpdate = itemRepository.read(itemId);
        if (itemUpdate == null) {
            throw new NoSuchElementException("Предмет с ID_" + itemId + " не найден");
        }

        if (userRepository.read(userId) == null) {
            throw new NoSuchElementException("Пользователь с ID_" + userId + " не найден");
        }

        if (!itemUpdate.getOwner().getId().equals(userId)) {
            throw new NoSuchElementException("Редактировать вещь может только владелец вещи");
        }

        return ItemMapper.toItemDto(itemRepository.update(itemDto, itemId, userId));
    }

    public Collection<ItemDto> search(String text) {
        Collection<Item> items = itemRepository.search(text);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
