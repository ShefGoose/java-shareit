package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.advice.enums.BookingState;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.validation.Marker;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookingDto bookingDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingClient.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @PathVariable("bookingId") Long bookingId,
                             @RequestParam(name = "approved") boolean approved) {
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable("bookingId") Long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public  ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(name = "state", required = false,
                                                             defaultValue = "ALL") BookingState state,
                                               @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                   Integer from,
                                               @Positive @RequestParam(name = "size", defaultValue = "10")
                                                   Integer size) {
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(name = "state", required = false,
                                                              defaultValue = "ALL") BookingState state,
                                                   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                       Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10")
                                                       Integer size) {
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }
}
