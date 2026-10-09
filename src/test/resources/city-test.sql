DROP TABLE IF EXISTS city;
CREATE TABLE city (
    id BIGINT PRIMARY KEY,
    city_name VARCHAR(50) NOT NULL,
    description VARCHAR(500)
);
