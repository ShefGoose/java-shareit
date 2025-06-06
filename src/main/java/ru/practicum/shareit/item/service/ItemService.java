package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.advice.enums.BookingStatus;
import ru.practicum.shareit.advice.exception.AccessDeniedException;
import ru.practicum.shareit.advice.exception.CommentCreationException;
import ru.practicum.shareit.advice.exception.EntityNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemDto find(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Предмет", itemId));

        Collection<CommentDto> comments = commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        return ItemMapper.toItemDto(item, comments);
    }

    public Collection<ItemOwnerDto> findAll(Long userId) {
        Collection<Item> items = itemRepository.findByOwnerId(userId);
        Collection<Booking> bookings = bookingRepository.findAllByOwnerId(userId);

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    return createItemOwnerDtoWithBookings(item, itemBookings);
                })
                .collect(Collectors.toList());
    }

    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(itemDto, userId)),
                Collections.emptyList());
    }

    public ItemDto update(ItemDto itemUpdateDto, Long itemId, Long userId) {
        Item itemUpdate = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Предмет", itemId));

        if (!itemUpdate.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Редактировать вещь может только владелец вещи");
        }

        if (itemUpdateDto.getName() != null) itemUpdate.setName(itemUpdateDto.getName());
        if (itemUpdateDto.getDescription() != null) itemUpdate.setDescription(itemUpdateDto.getDescription());
        if (itemUpdateDto.getAvailable() != null) itemUpdate.setAvailable(itemUpdateDto.getAvailable());

        return ItemMapper.toItemDto(itemRepository.save(itemUpdate), Collections.emptyList());
    }

    public Collection<ItemDto> search(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<Item> items = itemRepository.search(text);

        return items.stream()
                .map(item -> ItemMapper.toItemDto(item, Collections.emptyList()))
                .toList();
    }

    @Transactional
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));

        Booking booking = bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now())
                .orElseThrow(() -> new CommentCreationException("Оставить комментарий может " +
                        "только пользователь, который брал вещь в аренду и только после окончания срока аренды"));

        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, author, itemId)));
    }

    private ItemOwnerDto createItemOwnerDtoWithBookings(Item item, List<Booking> itemBookings) {
        Collection<CommentDto> comments = commentRepository.findAllByItemId(item.getId())
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        LocalDateTime endBooking = null;
        LocalDateTime startNextBooking = null;

        if (!itemBookings.isEmpty()) {
            List<Booking> sortedBookings = itemBookings.stream()
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList();

            endBooking = sortedBookings.stream()
                    .map(Booking::getEndTime)
                    .filter(endTime -> endTime.isBefore(LocalDateTime.now()))
                    .max(LocalDateTime::compareTo)
                    .orElse(null);


            startNextBooking = sortedBookings.stream()
                    .map(Booking::getStart)
                    .filter(start -> start.isAfter(LocalDateTime.now()) ||
                            start.isEqual(LocalDateTime.now()))
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
        }

        return ItemMapper.toItemOwnerDto(item, endBooking, startNextBooking, comments);
    }
}
