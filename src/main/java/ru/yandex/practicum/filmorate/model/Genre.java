package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Genre {
    private Integer id;

    @NotBlank(message = "Название жанра не может быть пустым")
    private String name;
}
