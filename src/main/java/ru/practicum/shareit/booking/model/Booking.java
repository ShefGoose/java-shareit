package ru.practicum.shareit.booking.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.advice.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;

@Component
@Data
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private BookingStatus status;



}
