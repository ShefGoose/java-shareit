package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
public class ItemOwnerDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime lastBooking;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime nextBooking;

    private Collection<CommentDto> comments;
}
