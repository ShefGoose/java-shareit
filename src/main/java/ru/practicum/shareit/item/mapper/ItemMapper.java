package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

public class ItemMapper {
    public static ItemDto toItemDto(Item item, Collection<CommentDto> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                comments
        );
    }

    public static ItemOwnerDto toItemOwnerDto(Item item,
                                              LocalDateTime endBooking,
                                              LocalDateTime startNextBooking,
                                              Collection<CommentDto> comments) {
        return new ItemOwnerDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                endBooking,
                startNextBooking,
                comments
        );
    }

    public static Item toItem(ItemDto itemDto, Long userId) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(new User(userId))
                .request(null)
                .build();
}
}