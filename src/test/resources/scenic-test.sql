DROP TABLE IF EXISTS scenic;
CREATE TABLE scenic (
    id BIGINT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    scenic_name VARCHAR(100) NOT NULL,
    scenic_desc TEXT
);
INSERT INTO scenic (id, city_id, scenic_name, scenic_desc) VALUES
(1, 1, '故宫', '北京景点1'),
(2, 1, '八达岭长城', '北京景点2'),
(3, 1, '颐和园', '北京景点3'),
(4, 2, '西湖', '杭州景点1'),
(5, 2, '灵隐寺', '杭州景点2');
