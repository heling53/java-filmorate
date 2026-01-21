package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Mpa> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM mpa ORDER BY id",
                (rs, rn) -> {
                    Mpa m = new Mpa();
                    m.setId(rs.getInt("id"));
                    m.setName(rs.getString("name"));
                    return m;
                }
        );
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable Integer id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rn) -> {
            Mpa m = new Mpa();
            m.setId(rs.getInt("id"));
            m.setName(rs.getString("name"));
            return m;
        }, id);

        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id=" + id + " не найден");
        }
        return mpaList.get(0);
    }
}