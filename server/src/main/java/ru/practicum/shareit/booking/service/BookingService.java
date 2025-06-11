package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.advice.Pagination;
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

    public BookingDto create(Long userId, BookingCreateDto bookingCreateDto) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));
        Item item = itemRepository.findById(bookingCreateDto.getItemId()).orElseThrow(() ->
                new EntityNotFoundException("Предмет", bookingCreateDto.getItemId()));
        if (!item.getAvailable()) {
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования",
                    item.getId()));
        }

        LocalDateTime startBooking = bookingCreateDto.getStart();
        LocalDateTime endBooking = bookingCreateDto.getEnd();

        if (item.getOwner().getId().equals(userId) ||
                bookingRepository.hasOverlappingBooking(bookingCreateDto.getItemId(),
                BookingStatus.APPROVED,
                startBooking,
                endBooking)) {
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования",
                    item.getId()));
        }

        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingCreateDto, userId,
                item.getName())), userId);
    }

    public BookingDto update(Long userId, Long bookingId, boolean approve) {
        Booking bookingUpdate = bookingRepository.findBookingWithGraphById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирование", bookingId));

        if (!bookingUpdate.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить или отменить бронирование может только владелец вещи");
        }

        bookingUpdate.setStatus(approve ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingDto(bookingRepository.save(bookingUpdate), bookingUpdate.getBooker().getId());
    }

    public BookingDto find(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findBookingWithGraphById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирование", bookingId));

        if (!(booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId))) {
            throw new AccessDeniedException("Просмотреть бронирование может " +
                    " только владелец вещи либо автор бронирования");
        }

        return BookingMapper.toBookingDto(booking, booking.getBooker().getId());
    }

    public Collection<BookingDto> findAllUserBookings(Long userId, BookingState state,
                                                      Integer from, Integer size) {
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = Pagination.makePageRequest(from, size);
        Page<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByBooker_Id(userId, pageRequest);
            case CURRENT ->
                    bookings = bookingRepository.findAllByBooker_IdAndStatusAndStartBeforeAndEndTimeAfter(userId,
                            BookingStatus.APPROVED,
                            now, now, pageRequest);
            case PAST -> bookings = bookingRepository.findAllByBooker_IdAndStatusAndEndTimeBefore(userId,
                    BookingStatus.APPROVED,
                    now, pageRequest);
            case FUTURE -> bookings = bookingRepository.findAllByBooker_IdAndStatusAndStartAfter(userId,
                    BookingStatus.APPROVED,
                    now, pageRequest);
            case WAITING -> bookings = bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.WAITING,
                    pageRequest);
            case REJECTED -> bookings = bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.REJECTED,
                    pageRequest);
            default -> bookings = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
        }

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
    }

    public Collection<BookingDto> findAllOwnerBookings(Long userId, BookingState state,
                                                       Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));

        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = Pagination.makePageRequest(from, size);
        Page<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByItem_Owner_Id(userId, pageRequest);
            case CURRENT ->
                    bookings = bookingRepository.findAllByItem_Owner_IdAndStatusAndStartBeforeAndEndTimeAfter(userId,
                            BookingStatus.APPROVED,
                            now, now, pageRequest);
            case PAST -> bookings = bookingRepository.findAllByItem_Owner_IdAndStatusAndEndTimeBefore(userId,
                    BookingStatus.APPROVED,
                    now, pageRequest);
            case FUTURE -> bookings = bookingRepository.findAllByItem_Owner_IdAndStatusAndStartAfter(userId,
                    BookingStatus.APPROVED,
                    now, pageRequest);
            case WAITING -> bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING,
                    pageRequest);
            case REJECTED ->
                    bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED,
                            pageRequest);
            default -> bookings = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
        }

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
    }
}
