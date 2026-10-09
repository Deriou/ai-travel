-- 供组员初始化城市模块使用；已有数据库无需重建。
-- 不删除数据库、表或已有数据，不会在应用启动时自动执行。
CREATE DATABASE IF NOT EXISTS ai_travel
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_travel;

CREATE TABLE IF NOT EXISTS city (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    city_name VARCHAR(50) NOT NULL COMMENT '城市名称',
    description VARCHAR(500) DEFAULT '' COMMENT '城市简介',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='城市分类表';

-- 按名称判断是否已有测试数据，重复执行不会再次添加同名城市。
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
