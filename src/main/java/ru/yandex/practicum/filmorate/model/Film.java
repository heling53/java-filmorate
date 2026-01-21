package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    @PastOrPresent
    private LocalDate releaseDate;

    @NotNull
    @Positive
    private Integer duration;

    private Mpa mpa;

    private Set<Integer> likes = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
}