package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void addLike(Integer filmId, Integer userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
            throw new IllegalArgumentException("Пользователь уже ставил лайк этому фильму");
        }
        film.getLikes().add(userId);
        log.info("Лайк добавлен: фильм {}, пользователь {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.info("Удаление лайка фильму {} от пользователя {}", filmId, userId);
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        if (!film.getLikes().contains(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new IllegalArgumentException("Пользователь не ставил лайк этому фильму");
        }
        film.getLikes().remove(userId);
        log.info("Лайк удален: фильм {}, пользователь {}", filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count != null) ? count : 10;
        log.info("Получение {} самых популярных фильмов", limit);
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(
                        (Film film) -> film.getLikes() != null ? film.getLikes().size() : 0
                ).reversed())
                .limit(limit)
                .collect(Collectors.toList());

    }

    public Film getFilmById(Integer id) {
        log.debug("Поиск фильма с id={}", id);
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            log.error("Фильм с id={} не найден", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    public List<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: {}", film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", film);

        if(filmStorage.getFilmById(film.getId()) == null) {
            log.error("Фильм с id={} не найден для обновления", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        return filmStorage.updateFilm(film);
    }

}
