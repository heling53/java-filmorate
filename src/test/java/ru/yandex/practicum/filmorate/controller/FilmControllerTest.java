package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Название фильма");
        validFilm.setDescription("Описание фильма");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void createFilm_ValidFilm_ReturnsOk() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Название фильма"));
    }

    @Test
    void createFilm_EmptyName_ReturnsBadRequest() throws Exception {
        validFilm.setName("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Название не может быть пустым"));
    }

    @Test
    void createFilm_TooLongDescription_ReturnsBadRequest() throws Exception {
        validFilm.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Максимальная длина описания — 200 символов"));
    }

    @Test
    void createFilm_TooEarlyReleaseDate_ReturnsBadRequest() throws Exception {
        validFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза должна быть не раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilm_NegativeDuration_ReturnsBadRequest() throws Exception {
        validFilm.setDuration(-10);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void createFilm_NullDuration_ReturnsBadRequest() throws Exception {
        validFilm.setDuration(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Продолжительность фильма должна быть указана"));
    }

    @Test
    void createFilm_NullReleaseDate_ReturnsBadRequest() throws Exception {
        validFilm.setReleaseDate(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза должна быть указана"));
    }

    @Test
    void updateFilm_NotFound_ReturnsNotFound() throws Exception {
        // Сначала создаем фильм
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);
        createdFilm.setId(999); // Меняем id на несуществующий

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFilm)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с указанным id не найден"));
    }

    @Test
    void getAllFilms_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}