package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(User user) {
        log.info("Создание пользователя. Login: {}, Name: {}, Email: {}",
                user.getLogin(), user.getName(), user.getEmail());

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Установлено имя из логина: {}", user.getLogin());
        }

        try {
            User created = userStorage.createUser(user);
            log.info("Пользователь создан с ID: {}", created.getId());
            return created;
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: ", e);
            throw new RuntimeException("Ошибка при создании пользователя: " + e.getMessage());
        }
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            if (user.getLogin() == null) {
                throw new ValidationException("Логин не может быть null");
            }
            user.setName(user.getLogin());
        }

        User existingUser = userStorage.getUserById(user.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        log.info("Обновление пользователя с id={}", user.getId());
        User updated = userStorage.updateUser(user);
        if (updated == null) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return updated;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Integer id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    public void addFriend(Integer userId, Integer friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        getUserById(userId);

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        getUserById(userId1);
        getUserById(userId2);

        return userStorage.getCommonFriends(userId1, userId2);
    }
}