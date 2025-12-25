package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.info("Добавление пользователя {} в друзья пользователю {}", friendId, userId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} уже в друзьях у {}", friendId, userId);
            throw new IllegalArgumentException("Пользователь уже в друзьях");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);

    }

    public User getUserById(Integer id) {
        log.debug("Поиск пользователя с id={}", id);
        User user = userStorage.getUserById(id);
        if (user == null) {
            log.error("Пользователь с id={} не найден", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    public List<User> getFriends(Integer userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Удаление пользователя {} из друзей пользователя {}", friendId, userId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} не в друзьях у {}", friendId, userId);
            return;

        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return userStorage.getAllUsers();
    }

    public User createUser(User user) {
        log.info("Создание пользователя: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не задано, установлено значение логина: {}", user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        getUserById(user.getId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);

        User user = getUserById(userId);
        User other = getUserById(otherId);

        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());

        return commonIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}
