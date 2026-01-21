CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR NOT NULL UNIQUE,
                                     login VARCHAR NOT NULL UNIQUE,
                                     name VARCHAR,
                                     birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa (
                                   id INTEGER PRIMARY KEY,
                                   name VARCHAR NOT NULL,
                                   description VARCHAR
);

CREATE TABLE IF NOT EXISTS genres (
                                      id INTEGER PRIMARY KEY,
                                      name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
                                     id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR NOT NULL,
                                     description VARCHAR,
                                     release_date DATE NOT NULL,
                                     duration INTEGER NOT NULL,
                                     mpa_id INTEGER,
                                     CONSTRAINT fk_film_mpa FOREIGN KEY (mpa_id) REFERENCES mpa(id)
    );

CREATE TABLE IF NOT EXISTS film_genres (
                                           film_id INTEGER NOT NULL,
                                           genre_id INTEGER NOT NULL,
                                           PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
    );

CREATE TABLE IF NOT EXISTS friendships (
                                           user_id INTEGER NOT NULL,
                                           friend_id INTEGER NOT NULL,
                                           status VARCHAR NOT NULL,
                                           PRIMARY KEY (user_id, friend_id)
    );

CREATE TABLE IF NOT EXISTS film_likes (
                                          film_id INTEGER NOT NULL,
                                          user_id INTEGER NOT NULL,
                                          PRIMARY KEY (film_id, user_id)
    );
