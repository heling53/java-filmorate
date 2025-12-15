package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;


@Data
public class User {
    private Integer id;
    private String name;
    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    private String email;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
