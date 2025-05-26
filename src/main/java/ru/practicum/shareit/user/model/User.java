package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.validation.Marker;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Null(groups = Marker.OnCreate.class, message = "При создании id пользователя должен быть null")
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class, message = "Имя не может быть пустым")
    private String name;
    @Email(groups = {Marker.OnCreate.class,Marker.OnUpdate.class}, message = "Неверный формат Email")
    @NotBlank(groups = Marker.OnCreate.class, message = "Почта не может быть пустой")
    private String email;

    public User(Long id) {
        this.id = id;
    }
}
