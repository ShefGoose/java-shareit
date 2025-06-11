package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.Marker;

import java.util.Collection;

@RestController
@Validated
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @Validated(Marker.OnUpdate.class)
    public ItemDto update(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId) {
        return itemService.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemAllFieldsDto get(@PathVariable Long itemId,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.find(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam(name = "text") String searchText,
                                           @RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(required = false) Integer from,
                                           @RequestParam(required = false) Integer size) {
        return itemService.search(searchText, from, size);
    }

    @GetMapping
    public Collection<ItemAllFieldsDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size) {
        return itemService.findAll(userId, from, size);
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping("{itemId}/comment")
    public CommentDto create(@PathVariable Long itemId,
                             @Valid @RequestBody CommentDto commentDto,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.createComment(commentDto, itemId, userId);
    }
}
