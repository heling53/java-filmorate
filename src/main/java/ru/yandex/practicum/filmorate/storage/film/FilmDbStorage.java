package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getAllFilms() {
        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films AS f
                LEFT JOIN mpa AS m ON f.mpa_id = m.id
                ORDER BY f.id
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        for (Film film : films) {
            loadGenres(film);
        }
        return films;
    }

    @Override
    public Film getFilmById(Integer id) {
        String sql = """
                SELECT f.*, m.name AS mpa_name 
                FROM films f 
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            return null;
        }

        Film film = films.get(0);
        loadGenres(film);
        return film;
    }

    @Override
    public Film createFilm(Film film) {
        String sqlQuery = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                stmt.setInt(5, film.getMpa().getId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        saveGenres(film);
        return film;
    }


    @Override
    public Film updateFilm(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            if (!mpaExists(film.getMpa().getId())) {
                return null;
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreExists(genre.getId())) {
                    return null;
                }
            }
        }

        String sql = """
                UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?
                """;
        int updated = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        if (updated == 0) {
            return null;
        }

        jdbcTemplate.update("""
                DELETE FROM film_genres WHERE film_id = ?
                """, film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String sql = """
                MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)
                """;
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        String sql = """
                DELETE FROM film_likes WHERE film_id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sql = """
                SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count 
                FROM films f 
                LEFT JOIN mpa m ON f.mpa_id = m.id 
                LEFT JOIN film_likes l ON f.id = l.film_id 
                GROUP BY f.id, m.name 
                ORDER BY likes_count DESC 
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);

        for (Film film : films) {
            loadGenres(film);
        }

        return films;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name")
        );
        film.setMpa(mpa);

        return film;
    }

    private Mpa getMpa(Integer id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }, id);
        return mpaList.isEmpty() ? null : mpaList.get(0);
    }

    private void loadGenres(Film film) {
        String sql = """
                SELECT g.id, g.name FROM genres g 
                JOIN film_genres fg ON g.id = fg.genre_id 
                WHERE fg.film_id = ? ORDER BY g.id
                """;

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), film.getId());

        film.setGenres(new java.util.LinkedHashSet<>(genres));
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        java.util.Set<Genre> uniqueGenres = new java.util.LinkedHashSet<>(film.getGenres());

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : uniqueGenres) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }

        film.setGenres(uniqueGenres);
    }

    private boolean filmExists(Integer filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

    private boolean userExists(Integer userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    private boolean mpaExists(Integer mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        return count != null && count > 0;
    }

    private boolean genreExists(Integer genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreId);
        return count != null && count > 0;
    }

    private List<Genre> getGenresByFilmId(Integer filmId) {
        String sql = """
                SELECT g.* FROM genres g 
                                JOIN film_genres fg ON g.id = fg.genre_id 
                                WHERE fg.film_id = ? 
                                ORDER BY g.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ), filmId);
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> uniqueGenres = film.getGenres().stream()
                .distinct()
                .collect(Collectors.toList());
        film.setGenres(new LinkedHashSet<>(uniqueGenres));

        for (Genre genre : uniqueGenres) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
    }
}