package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testlogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUser_ValidUser_ReturnsOk() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void createUser_EmptyName_SetsLoginAsName() throws Exception {
        validUser.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void createUser_NullName_SetsLoginAsName() throws Exception {
        validUser.setName(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void createUser_InvalidEmailNoAt_ReturnsBadRequest() throws Exception {
        validUser.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта должна содержать символ @"));
    }

    @Test
    void createUser_EmptyEmail_ReturnsBadRequest() throws Exception {
        validUser.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта не может быть пустой"));
    }

    @Test
    void createUser_NullEmail_ReturnsBadRequest() throws Exception {
        validUser.setEmail(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта не может быть пустой"));
    }

    @Test
    void createUser_LoginWithSpaces_ReturnsBadRequest() throws Exception {
        validUser.setLogin("login with spaces");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не может содержать пробелы"));
    }

    @Test
    void createUser_EmptyLogin_ReturnsBadRequest() throws Exception {
        validUser.setLogin("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не может быть пустым"));
    }

    @Test
    void createUser_NullLogin_ReturnsBadRequest() throws Exception {
        validUser.setLogin(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не может быть пустым"));
    }

    @Test
    void createUser_FutureBirthday_ReturnsBadRequest() throws Exception {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void updateUser_NotFound_ReturnsNotFound() throws Exception {
        // Сначала создаем пользователя
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        createdUser.setId(999); // Меняем id на несуществующий

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с указанным id не найден"));
    }

    @Test
    void getAllUsers_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}