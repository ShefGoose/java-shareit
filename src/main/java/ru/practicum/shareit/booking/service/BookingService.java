package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.advice.enums.BookingState;
import ru.practicum.shareit.advice.enums.BookingStatus;
import ru.practicum.shareit.advice.exception.AccessDeniedException;
import ru.practicum.shareit.advice.exception.EntityNotFoundException;
import ru.practicum.shareit.advice.exception.ItemUnavailableException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@Service
@AllArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingDto create(BookingCreateDto bookingCreateDto, Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));
        Item item = itemRepository.findById(bookingCreateDto.getItemId()).orElseThrow(() ->
                new EntityNotFoundException("Предмет", bookingCreateDto.getItemId()));
        if (!item.getAvailable()) {
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования",
                    item.getId()));
        }
        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingCreateDto, userId,
                item.getName())), userId);
    }

    public BookingDto update(Long userId, Long bookingId, boolean approve) {
        Booking bookingUpdate = bookingRepository.findByIdWithItemAndOwner(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирование", bookingId));

        if (!bookingUpdate.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить или отменить бронирование может только владелец вещи");
        }

        bookingUpdate.setStatus(approve ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingDto(bookingRepository.save(bookingUpdate), bookingUpdate.getBooker().getId());
    }

    public BookingDto find(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdWithItemAndOwner(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирование", bookingId));

        if (!(booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId))) {
            throw new AccessDeniedException("Просмотреть бронирование может " +
                    " только владелец вещи либо автор бронирования");
        }

        return BookingMapper.toBookingDto(booking, booking.getBooker().getId());
    }

    public Collection<BookingDto> findAllUserBookings(Long userId, BookingState state) {
        Collection<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByUserId(userId);
            case CURRENT -> bookings = bookingRepository.findAllByUserIdAndStatusAndStartBeforeAndEndAfter(userId,
                    BookingStatus.APPROVED,
                    now);
            case PAST -> bookings = bookingRepository.findAllByUserIdAndStatusAndEndBefore(userId,
                    BookingStatus.APPROVED,
                    now);
            case FUTURE -> bookings = bookingRepository.findAllByUserIdAndStatusAndStartAfter(userId,
                    BookingStatus.APPROVED,
                    now);
            case WAITING -> bookings = bookingRepository.findAllByUserIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository.findAllByUserIdAndStatus(userId, BookingStatus.REJECTED);
            default -> bookings = Collections.emptyList();
        }

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
    }

    public Collection<BookingDto> findAllOwnerBookings(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));

        Collection<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByOwnerId(userId);
            case CURRENT -> bookings = bookingRepository.findAllByOwnerIdAndStatusAndStartBeforeAndEndAfter(userId,
                    BookingStatus.APPROVED,
                    now);
            case PAST -> bookings = bookingRepository.findAllByOwnerIdAndStatusAndEndBefore(userId,
                    BookingStatus.APPROVED,
                    now);
            case FUTURE -> bookings = bookingRepository.findAllByOwnerIdAndStatusAndStartAfter(userId,
                    BookingStatus.APPROVED,
                    now);
            case WAITING -> bookings = bookingRepository.findAllByOwnerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository.findAllByOwnerIdAndStatus(userId, BookingStatus.REJECTED);
            default -> bookings = Collections.emptyList();
        }

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
    }
}
