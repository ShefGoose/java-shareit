package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validation.Marker;

import java.util.Collection;

@RestController
@Validated
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ItemRequestDto create(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping
    public Collection<ItemRequestAllFieldsDto> getAllUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                  @RequestParam(required = false) Integer from,
                                                                  @RequestParam(required = false) Integer size) {
        return itemRequestService.findAllUserRequests(userId, from, size);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(required = false) Integer from,
                                             @RequestParam(required = false) Integer size) {
        return itemRequestService.findAll(userId, from, size);
    }

    @GetMapping("{requestId}")
    public ItemRequestAllFieldsDto get(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @PathVariable(name = "requestId") Long requestId) {
        return itemRequestService.find(requestId, userId);
    }
}
