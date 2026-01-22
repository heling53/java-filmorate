package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 0;

    @Override
    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(++idCounter);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Integer id) {
        return users.get(id);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        }
    }

    @Override
    public List<User> getFriends(Integer userId) {
        User user = users.get(userId);
        if (user == null) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        User user1 = users.get(userId1);
        User user2 = users.get(userId2);
        if (user1 == null || user2 == null) return new ArrayList<>();

        Set<Integer> commonIds = new HashSet<>(user1.getFriends());
        commonIds.retainAll(user2.getFriends());

        return commonIds.stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Integer id) {
        return users.containsKey(id);
    }
}