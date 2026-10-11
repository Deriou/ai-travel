-- AI 智能旅游路线规划系统：数据库初始化脚本
-- 不删除数据库、表或已有数据，可以重复执行：
--   建表使用 IF NOT EXISTS；基础数据按名称判断，已存在的不会重复插入。
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS ai_travel
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_travel;

-- ========== 建表 ==========

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    username VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码散列，不存明文',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS city (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '城市编号',
    city_name VARCHAR(50) NOT NULL COMMENT '城市名称',
    description VARCHAR(500) DEFAULT '' COMMENT '城市简介',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='城市表';

CREATE TABLE IF NOT EXISTS scenic (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '景点编号',
    city_id BIGINT NOT NULL COMMENT '所属城市，对应 city.id',
    scenic_name VARCHAR(100) NOT NULL COMMENT '景点名称',
    scenic_desc TEXT COMMENT '景点简介',
    PRIMARY KEY (id),
    KEY idx_city_id (city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点表';

CREATE TABLE IF NOT EXISTS travel_route (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '路线编号',
    user_id BIGINT NOT NULL COMMENT '所属用户，对应 user.id',
    destination VARCHAR(100) NOT NULL COMMENT '目的地',
    days INT NOT NULL COMMENT '出行天数',
    preference VARCHAR(200) DEFAULT '' COMMENT '游玩偏好',
    route_content TEXT NOT NULL COMMENT 'AI 路线文本',
    tips_content TEXT COMMENT '出行小贴士，可为空',
    is_collect TINYINT DEFAULT 0 COMMENT '0 未收藏，1 已收藏',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路线记录表';

-- ========== 城市数据 ==========

INSERT INTO city (city_name, description)
SELECT '北京', '首都，历史文化名城，众多皇家古迹与现代地标'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE city_name = '北京');
INSERT INTO city (city_name, description)
SELECT '杭州', '江南水乡，西湖、灵隐寺等知名景点'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE city_name = '杭州');
INSERT INTO city (city_name, description)
SELECT '成都', '天府之国，美食与大熊猫为特色，休闲旅游城市'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE city_name = '成都');
INSERT INTO city (city_name, description)
SELECT '西安', '十三朝古都，兵马俑、古城墙等历史遗迹'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE city_name = '西安');

-- ========== 景点数据 ==========
-- 通过城市名称查出 city_id，不依赖城市编号恰好是 1、2、3、4。

INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '故宫', '明清皇家宫殿，世界文化遗产，建筑宏伟壮观' FROM city
WHERE city_name = '北京' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '故宫');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '八达岭长城', '万里长城的重要段落，感受雄伟长城风光' FROM city
WHERE city_name = '北京' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '八达岭长城');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '颐和园', '保存完整的皇家园林，昆明湖与万寿山相映成景' FROM city
WHERE city_name = '北京' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '颐和园');

INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '西湖', '杭州标志性景点，湖光山色，四季风景各异' FROM city
WHERE city_name = '杭州' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '西湖');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '灵隐寺', '千年古刹，禅意浓厚，知名祈福圣地' FROM city
WHERE city_name = '杭州' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '灵隐寺');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '西溪湿地', '城市中的湿地公园，适合慢行和乘船游览' FROM city
WHERE city_name = '杭州' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '西溪湿地');

INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '大熊猫繁育研究基地', '观赏国宝大熊猫，了解大熊猫保护知识' FROM city
WHERE city_name = '成都' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '大熊猫繁育研究基地');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '宽窄巷子', '老成都民居改造的街区，美食、休闲打卡地' FROM city
WHERE city_name = '成都' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '宽窄巷子');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '都江堰', '两千多年前修建的水利工程，至今仍在使用' FROM city
WHERE city_name = '成都' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '都江堰');

INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '秦始皇兵马俑', '世界八大奇迹之一，秦朝地下军阵' FROM city
WHERE city_name = '西安' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '秦始皇兵马俑');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '西安城墙', '中国现存保存最完整的古代城墙，可以骑行游览' FROM city
WHERE city_name = '西安' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '西安城墙');
INSERT INTO scenic (city_id, scenic_name, scenic_desc)
SELECT id, '大雁塔', '唐代佛塔，周边有大唐不夜城和音乐喷泉' FROM city
WHERE city_name = '西安' AND NOT EXISTS (SELECT 1 FROM scenic WHERE scenic_name = '大雁塔');

-- ========== 检查结果 ==========

SELECT COUNT(*) AS city_count FROM city;
SELECT COUNT(*) AS scenic_count FROM scenic;
