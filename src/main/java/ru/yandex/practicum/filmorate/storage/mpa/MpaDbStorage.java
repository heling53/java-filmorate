package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public Mpa getById(Integer id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, this::mapRowToMpa, id);
        if (mpaList.isEmpty()) {
            throw new NotFoundException("MPA с id=" + id + " не найден");
        }
        return mpaList.get(0);
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("id"), rs.getString("name"));
    }
}