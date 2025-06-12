package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.advice.Pagination;
import ru.practicum.shareit.advice.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mappper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));

        return ItemRequestMapper.toItemRequestDto(itemRequestRepository
                .save(ItemRequestMapper.toItemRequest(itemRequestDto, userId)));
    }

    public Collection<ItemRequestAllFieldsDto> findAllUserRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));

        PageRequest pageRequest = Pagination.makePageRequest(from, size);
        Page<ItemRequest> userRequests;

        userRequests = itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId,
                Objects.requireNonNullElseGet(pageRequest, () -> PageRequest.of(0, Integer.MAX_VALUE)));
        Collection<Long> requestsIds = userRequests.stream()
                .map(ItemRequest::getId)
                .toList();

        Collection<Item> itemRequests = itemRepository.findAllByRequest_IdIn(requestsIds);

        Map<Long, List<Item>> itemsByRequest = itemRequests.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return userRequests.stream()
                .map(itemRequest -> {
                    List<Item> relatedItems = itemsByRequest.getOrDefault(itemRequest.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestAllFieldsDto(itemRequest, relatedItems);
                })
                .toList();
    }

    public Collection<ItemRequestDto> findAll(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));

        PageRequest pageRequest = Pagination.makePageRequest(from, size);

        return itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(userId,
                        Objects.requireNonNullElseGet(pageRequest, () ->
                                PageRequest.of(0, Integer.MAX_VALUE))).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    public ItemRequestAllFieldsDto find(Long requestId, Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос на вещь", requestId));

        Collection<Item> itemsRequest = itemRepository.findAllByRequest_Id(requestId);

        return ItemRequestMapper.toItemRequestAllFieldsDto(itemRequest, itemsRequest);
    }
}
