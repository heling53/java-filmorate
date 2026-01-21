# java-filmorate
Template repository for Filmorate project.
Система для поиска фильмов и оценки кинокартин.

## 🗄️ Database Schema

![Database Schema](docs/schema.png)

## 📋 Примеры SQL-запросов

### Пользователи
```sql
-- Создание пользователя
INSERT INTO users (email, login, name, birthday) 
VALUES ('user@mail.com', 'login', 'Name', '1990-01-01');

-- Получение всех пользователей
SELECT * FROM users ORDER BY id;

-- Получение друзей пользователя
SELECT u.* FROM users u
JOIN friendships f ON u.id = f.friend_id
WHERE f.user_id = ?;

-- Создание фильма
INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Film Name', 'Description', '2000-01-01', 120, 1);

-- Получение популярных фильмов
SELECT f.*, COUNT(fl.user_id) as likes_count
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 10;

-- Добавление в друзья
INSERT INTO friendships (user_id, friend_id, status) 
VALUES (1, 2, 'CONFIRMED');

-- Получение общих друзей
SELECT u.* FROM users u
JOIN friendships f1 ON u.id = f1.friend_id
JOIN friendships f2 ON u.id = f2.friend_id
WHERE f1.user_id = ? AND f2.user_id = ?;

-- Добавление лайка
INSERT INTO film_likes (film_id, user_id) VALUES (1, 1);

-- Удаление лайка
DELETE FROM film_likes WHERE film_id = ? AND user_id = ?;