package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;

    private String name;

    @NotBlank
    @Pattern(regexp = "^\\S+$")
    private String login;

    @NotBlank
    @Email
    private String email;

    @PastOrPresent
    private LocalDate birthday;

    private Set<Integer> friends = new HashSet<>();
}