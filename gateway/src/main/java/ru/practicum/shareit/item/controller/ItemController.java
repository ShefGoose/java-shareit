package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.Marker;


@RestController
@Validated
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @Validated(Marker.OnUpdate.class)
    public ResponseEntity<Object> updateItem(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId) {
        return itemClient.updateItem(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable Long itemId,
                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(name = "text") String searchText,
                                              @RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                  Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10")
                                                  Integer size) {
        return itemClient.searchItems(searchText, userId, from, size);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                              Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10")
                                                  Integer size) {
        return itemClient.getItems(userId, from, size);
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> createItemComment(@PathVariable Long itemId,
                             @Valid @RequestBody CommentDto commentDto,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (userId == null) throw new IllegalArgumentException("Field userId is null");
        return itemClient.createComment(commentDto, itemId, userId);
    }
}
