package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemUserDto;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemUserMapper {
    public static ItemUserDto toItemUserDto(Item item) {
        return new ItemUserDto(item.getName(), item.getDescription());
    }
}
