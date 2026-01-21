package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null || userStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь с указанным id не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        log.info("Обновление пользователя с id={}", user.getId());
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Integer id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        getUserById(userId);

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        List<User> userFriends = userStorage.getFriends(id);
        List<User> otherFriends = userStorage.getFriends(otherId);

        return userFriends.stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toList());
    }
}