DROP TABLE IF EXISTS travel_route;
CREATE TABLE travel_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    destination VARCHAR(100) NOT NULL,
    days INT NOT NULL,
    preference VARCHAR(200),
    route_content TEXT NOT NULL,
    tips_content TEXT,
    is_collect TINYINT DEFAULT 0,
    create_time DATETIME
);
