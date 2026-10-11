# AI 智能旅游路线规划系统

河北农业大学小组实训项目。用户输入目的地、出行天数和游玩偏好，系统生成旅游路线，并提供保存、查询、收藏等功能。

当前已完成城市列表接口和统一 JSON 返回格式。用户登录、AI 路线规划将逐步开发。前端已接入城市列表接口。

## 开发环境

- Java 17
- Spring Boot 3.5.16
- MyBatis Spring Boot Starter 3.0.5
- Maven：项目提供 Maven Wrapper，版本为 3.9.10
- MySQL：建议小组统一使用 8.0
- 前端：Vue3、Vite、Axios、Element Plus；Node.js 22.12+

## 项目结构

```text
ai-travel/
├── src/main/java/cn/edu/hebau/aitravel/   后端代码
├── src/main/resources/                  应用配置
├── src/test/                            后端测试
├── sql/init.sql                         四张表与城市、景点数据初始化
├── http/city.http                       IDEA HTTP 请求示例
├── .mvn/、mvnw、mvnw.cmd                Maven Wrapper
├── pom.xml                              后端依赖和构建配置
└── README.md                            项目说明
```

根目录的 `frontend/` 存放 Vue 项目，前后端使用同一个 Git 仓库，分别启动和构建。

## 数据库配置

每位组员使用自己电脑上的 MySQL，数据库名称统一为 `ai_travel`，账号和密码按个人环境填写。

1. 确认本地 MySQL 已启动。在数据库工具（IDEA、Navicat 或 MySQL 客户端）中执行 `sql/init.sql`，创建 `user`、`city`、`scenic`、`travel_route` 四张表并准备城市和景点数据。脚本不会删除数据库、表或已有路线，重复执行不会再插入同名城市或景点。
2. 复制 `src/main/resources/application-local.properties.example`，将副本命名为同目录下的 `application-local.properties`。
3. 编辑副本，填写自己的连接地址、账号和密码：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_travel
spring.datasource.username=root
spring.datasource.password=填写自己的数据库密码
```

如果 MySQL 端口或账号不同，修改对应值即可。不要给密码额外加引号；密码中的反斜杠在 Properties 文件中需要写成两个反斜杠。

公共配置 `application.properties` 已通过以下设置自动导入本地文件，无需额外启用 profile：

```properties
spring.config.import=optional:classpath:application-local.properties
```

- `application.properties`：提交到 GitHub，存放公共配置。
- `application-local.properties.example`：提交到 GitHub，提供配置示例，不填写真实密码。
- `application-local.properties`：已被 `.gitignore` 忽略，只留在个人电脑上。

后续 AI API Key 也存放在本地配置中，不写进公共配置、源码或 README。

已接入 MyBatis，通过注解 SQL 查询 `city` 表。启动后访问 `/city/list` 验证实际数据库连接；只看到启动成功日志还不能确认数据库查询成功。SQL 脚本需手动执行，应用启动时不会自动初始化数据库。

## 本地启动

### 使用 IDEA

1. 打开项目根目录，设置 Project SDK 为 JDK 17。
2. 重新加载 Maven 项目，等待依赖下载完成。
3. 按上述说明准备自己的数据库配置。
4. 运行 `cn.edu.hebau.aitravel.AiTravelApplication`。
5. 控制台出现 `Started AiTravelApplication`，且 Web 服务监听 8080 端口，表示应用启动成功。

浏览器打开 `http://localhost:8080/city/list` 查看城市列表。根路径 `/` 尚未提供页面，返回 404 属于预期情况。

Maven 本地仓库用于存放依赖，每个人可以使用默认目录或自己的目录，不需要和其他成员一致，也不提交到 GitHub。通过 IDEA 设置的本地仓库路径不会自动应用到独立终端的 Maven 命令。

### 使用终端

在项目根目录运行，确保 `java -version` 显示 Java 17：

```bash
# macOS / Linux
./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

首次使用 Wrapper 需要联网下载 Maven 和依赖。按 Ctrl+C 停止服务。

## 城市列表接口

```http
GET http://localhost:8080/city/list
```

无需请求参数。成功时 HTTP 状态为 200，响应格式如下：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {"id": 1, "cityName": "北京", "description": "首都，历史文化名城，众多皇家古迹与现代地标"}
  ]
}
```

城市按 `id` 升序返回，实际数量取决于数据库。表中没有城市时，`data` 返回 `[]`。`code` 是 JSON 中的业务状态码。

在 IDEA 中打开 `http/city.http`，点击请求旁的运行按钮发送请求；文件中的响应检查会验证 HTTP 状态、业务状态、消息和数组类型。

### 查询代码的调用顺序

```text
CityController → CityService → CityMapper → MySQL city 表
```

- `entity/City`：城市数据，包含 `id`、`cityName` 和 `description`。
- `mapper/CityMapper`：使用 `@Mapper`、`@Select` 查询城市。
- `service/CityService`：城市业务类，直接调用 Mapper。
- `controller/CityController`：接收 `/city/list` 请求。
- `util/Result<T>`：统一包装 `code`、`msg`、`data`。
- `util/BusinessException`：业务校验失败时抛出，例如参数非法、用户名重复。
- `util/GlobalExceptionHandler`：把异常统一转成 `Result`，HTTP 状态与 `code` 一致（400 参数或业务错误、404 路径不存在、500 服务异常），页面不显示异常堆栈。

公共配置已开启下划线到驼峰映射，数据库的 `city_name` 自动对应 Java 的 `cityName`。Service 目前使用一个具体类，查询 SQL 写在注解中。

## 景点分页接口

```http
GET http://localhost:8080/scenic/list?cityId=1&pageNum=1&pageSize=5
```

`cityId` 可不传，不传时查询全部景点；`pageNum` 从 1 开始，`pageSize` 范围 1～100，两者必填。按 `id` 升序返回：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [{"id": 1, "cityId": 1, "scenicName": "故宫", "scenicDesc": "明清皇家宫殿……"}],
    "total": 3,
    "pageNum": 1,
    "pageSize": 5
  }
}
```

`total` 是筛选后的总条数。没有匹配结果或页码超出范围时 `list` 为 `[]`。页码或每页条数非法时返回 HTTP 400、`code` 400。请求示例见 `http/scenic.http`。

分页用 SQL 的 `LIMIT 起始位置, 条数` 实现，起始位置 = `(pageNum - 1) × pageSize`；另执行一次 `COUNT(*)` 得到总条数。`ScenicMapper` 中的 `<if>` 表示传了 `cityId` 才拼接 `WHERE city_id = ?`。

## 验证与常见问题

运行测试：

```bash
# macOS / Linux
./mvnw test
```

```powershell
# Windows PowerShell
.\mvnw.cmd test
```

自动测试使用测试范围的 H2 内存数据库，覆盖中文数据、排序、字段映射、统一响应和空数组场景，不修改个人 MySQL 中的数据。H2 不用于实际运行；真实 MySQL 连接需通过 `/city/list` 验证。

- `Access denied`：检查本地配置中的数据库账号和密码。
- `Communications link failure`：检查 MySQL 是否启动，以及连接地址和端口。
- `Unknown database` 或提示 `city` 表不存在：执行 `sql/init.sql`，并确认连接的是 `ai_travel`。
- 8080 端口占用：停止之前运行的应用，再启动本项目。
## 前端启动与联调

1. 先启动后端 `AiTravelApplication`，默认端口为 8080。
2. 打开另一个终端，进入项目的 `frontend` 目录。
3. 首次拉取或依赖变更后执行 `npm ci`，再执行 `npm run dev`。
4. 打开终端给出的地址（通常是 `http://localhost:5173`），查看城市表格。

前端通过 Axios 请求 `/api/city/list`，Vite 代理去掉 `/api` 后转发到后端 `/city/list`。页面展示数据库实际数据；修改数据库后点击“刷新”重新查询。当前开发联调不需要另加后端跨域设置。

在 `frontend` 目录执行 `npm run build` 验证构建。提交 `package.json`、`package-lock.json` 和源码，不提交 `node_modules`、`dist`。修改 `vite.config.js` 后重启前端。Vite 代理仅适用于开发服务，构建后部署的转发设置后续补充。

## 小组协作

1. 接受 GitHub 仓库的协作邀请，然后克隆项目。
2. 开始新任务前切换到 `main` 并执行 `git pull --ff-only`。
3. 从最新 `main` 创建功能分支，例如 `feature/city-list`。
4. 完成功能并验证后，提交代码、推送分支，再创建 Pull Request。
5. 由另一位组员检查后合并到 `main`。

每位成员使用自己的 Git 提交姓名和邮箱。个人配置、密码、API Key、IDE 设置、构建产物和前端依赖不提交；源码、Maven Wrapper、配置示例、数据库脚本和项目文档需要提交。
