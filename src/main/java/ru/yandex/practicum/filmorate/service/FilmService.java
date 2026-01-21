package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public Film createFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getMpa() != null) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mpa WHERE id = ?", Integer.class, film.getMpa().getId());
            if (count == null || count == 0) {
                throw new NotFoundException("Рейтинг MPA с указанным id не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genres WHERE id = ?", Integer.class, genre.getId());
                if (count == null || count == 0) {
                    throw new NotFoundException("Жанр с указанным id не найден");
                }
            }
        }

        log.info("Добавление нового фильма: {}", film.getName());
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID фильма не может быть null");
        }

        Film existingFilm = filmStorage.getFilmById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с указанным id не найден");
        }

        Film updated = filmStorage.updateFilm(film);
        if (updated == null) {
            throw new NotFoundException("Фильм с указанным id не найден");
        }
        return updated;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    public void addLike(Integer filmId, Integer userId) {
        getFilmById(filmId);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        getFilmById(filmId);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

}