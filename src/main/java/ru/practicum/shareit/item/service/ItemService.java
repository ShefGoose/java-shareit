package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.advice.enums.BookingStatus;
import ru.practicum.shareit.advice.exception.AccessDeniedException;
import ru.practicum.shareit.advice.exception.CommentCreationException;
import ru.practicum.shareit.advice.exception.EntityNotFoundException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
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

import static java.util.Comparator.comparing;

@Service
@AllArgsConstructor
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemAllFieldsDto find(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Предмет", itemId));
        Collection<Booking> bookings = bookingRepository.findAllByItem_Id(itemId);

        return createItemAllFieldsDtoWithBookings(item, bookings);
    }

    public Collection<ItemAllFieldsDto> findAll(Long userId) {
        Collection<Item> items = itemRepository.findByOwnerId(userId);
        Collection<Booking> bookings = bookingRepository.findAllByItem_Owner_Id(userId);

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    return createItemAllFieldsDtoWithBookings(item, itemBookings);
                })
                .collect(Collectors.toList());
    }

    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(itemDto, userId)));
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

        return ItemMapper.toItemDto(itemRepository.save(itemUpdate));
    }

    public Collection<ItemDto> search(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<Item> items = itemRepository.search(text);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));

        Booking booking = bookingRepository.findByBooker_IdAndItem_IdAndStatusAndEndTimeBefore(userId, itemId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now())
                .orElseThrow(() -> new CommentCreationException("Оставить комментарий может " +
                        "только пользователь, который брал вещь в аренду и только после окончания срока аренды"));

        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, author, itemId)));
    }

    private ItemAllFieldsDto createItemAllFieldsDtoWithBookings(Item item, Collection<Booking> itemBookings) {
        Collection<CommentDto> comments = commentRepository.findAllByItemId(item.getId())
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        BookingDto endBooking = null;
        BookingDto startNextBooking = null;

        if (!itemBookings.isEmpty()) {
            endBooking = itemBookings.stream()
                    .filter(booking -> booking.getEndTime().isBefore(LocalDateTime.now()))
                    .max(comparing(Booking::getEndTime))
                    .map(booking -> BookingMapper.toBookingDto(booking, booking.getBooker().getId()))
                    .orElse(null);


            startNextBooking = itemBookings.stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .min(comparing(Booking::getStart))
                    .map(booking -> BookingMapper.toBookingDto(booking, booking.getBooker().getId()))
                    .orElse(null);
        }

        return ItemMapper.toItemAllFieldsDto(item, endBooking, startNextBooking, comments);
    }
}
