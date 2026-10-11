# AI 智能旅游路线规划系统

河北农业大学小组实训项目。用户输入目的地、出行天数和游玩偏好，系统生成旅游路线，并提供保存、查询、收藏等功能。

当前已完成统一返回与异常处理、城市列表、景点分页、注册登录（Spring Security + JWT + BCrypt）、AI 生成路线与出行小贴士（DeepSeek）、路线保存与管理。前端已完成四个页面并与全部接口联调。

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
├── http/                                IDEA HTTP 请求示例（city、scenic、user、ai、route）
├── .mvn/、mvnw、mvnw.cmd                Maven Wrapper
├── pom.xml                              后端依赖和构建配置
└── README.md                            项目说明
```

根目录的 `frontend/` 存放 Vue 项目，前后端使用同一个 Git 仓库，分别启动和构建。

## 数据库配置

每位组员使用自己电脑上的 MySQL，数据库名称统一为 `ai_travel`，账号和密码按个人环境填写。

1. 确认本地 MySQL 已启动。在数据库工具（IDEA、Navicat 或 MySQL 客户端）中执行 `sql/init.sql`，创建 `user`、`city`、`scenic`、`travel_route` 四张表并准备城市和景点数据。脚本不会删除数据库、表或已有路线，重复执行不会再插入同名城市或景点。
2. 复制 `src/main/resources/application-local.properties.example`，将副本命名为同目录下的 `application-local.properties`。
3. 编辑副本，填写自己的连接地址、账号、密码，以及 JWT 签名密钥：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_travel
spring.datasource.username=root
spring.datasource.password=填写自己的数据库密码
jwt.secret=任意随机字符串，至少32个字符
ai.api-key=sk-你的DeepSeek密钥
```

`jwt.secret` 用于给登录令牌签名，可以用 `openssl rand -hex 32` 生成。缺少或少于 32 个字符时应用无法启动。`ai.api-key` 在 [DeepSeek 开放平台](https://platform.deepseek.com/api_keys) 创建；不填也能启动，但 AI 接口会返回“AI生成失败”。

脚本会准备演示账号 `testuser`，密码 `123456`（数据库中保存的是 BCrypt 散列）。

如果 MySQL 端口或账号不同，修改对应值即可。不要给密码额外加引号；密码中的反斜杠在 Properties 文件中需要写成两个反斜杠。

公共配置 `application.properties` 已通过以下设置自动导入本地文件，无需额外启用 profile：

```properties
spring.config.import=optional:classpath:application-local.properties
```

- `application.properties`：提交到 GitHub，存放公共配置。
- `application-local.properties.example`：提交到 GitHub，提供配置示例，不填写真实密码。
- `application-local.properties`：已被 `.gitignore` 忽略，只留在个人电脑上。

数据库密码、JWT 签名密钥和 AI API Key 都只存放在本地配置中，不写进公共配置、源码或 README。

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

## 注册登录与权限

| 接口 | 说明 | 权限 |
|---|---|---|
| `POST /user/register` | 请求体 `{"username","password"}`，成功 `msg` 为“注册成功”，不自动登录 | 公开 |
| `POST /user/login` | 成功返回 `data.token` 和 `data.user`（`id`、`username`、`createTime`） | 公开 |

用户名重复、输入为空、密码不是 6～20 个字符、账号或密码错误时，返回 HTTP 400、`code` 400。响应中不会出现密码或密码散列。请求示例见 `http/user.http`。

登录后，受保护接口在请求头携带 `Authorization: Bearer <token>`。城市、景点、注册、登录公开访问，其余接口都需要登录；没有令牌、令牌被篡改或已过期时返回 HTTP 401 和 `{"code":401,"msg":"未登录或登录已失效","data":null}`。

三者分工：

- **BCrypt**（`SecurityConfig.passwordEncoder`）：注册时 `encode` 把密码变成散列存库；登录时 `matches` 比对输入密码和散列。散列不可逆，数据库泄露也看不到明文。
- **JWT**（`util/JwtUtil`）：登录成功后生成令牌，里面只放用户编号和过期时间，并用 `jwt.secret` 签名；有效期由 `jwt.expire-hours` 配置（默认 24 小时）。令牌被改动后签名对不上，会被拒绝。
- **Spring Security**（`config/SecurityConfig`、`config/JwtAuthFilter`）：每个请求先经过 `JwtAuthFilter`，令牌有效就把用户编号登记为“已登录”；随后 `SecurityConfig` 判断接口是否需要登录，未登录则返回 401。

后续接口通过 `@AuthenticationPrincipal Long userId` 取得当前用户编号，不接受前端传入的 `userId`。采用无状态认证，不使用 Session；退出登录由前端删除令牌，已签发的令牌在到期前仍有效。

## AI 生成接口

| 接口 | 请求体 | 返回 `data` |
|---|---|---|
| `POST /ai/generateRoute` | `{"destination":"北京","days":3,"preference":"休闲、美食"}` | 按天分段的路线文本 |
| `POST /ai/generateTips` | `{"destination":"北京","days":3}` | 出行小贴士文本，每条一行 |

两个接口都需要登录。目的地必填，天数为 1～30 的整数，路线接口的偏好必填；不符合时返回 400，不会调用大模型。大模型超时、网络异常、Key 错误或返回为空时，返回 HTTP 500 和 `{"code":500,"msg":"AI生成失败，请稍后重试","data":null}`，详细原因只写入后端日志。生成结果不会自动保存。请求示例见 `http/ai.http`。

实现集中在 `service/AiService`：

1. 校验出行条件，把目的地、天数、偏好拼成一段中文提示词。
2. 用 Spring 自带的 `RestClient` 按 OpenAI 兼容格式请求 `POST {ai.base-url}/chat/completions`，请求头带 `Authorization: Bearer {ai.api-key}`。
3. 从返回 JSON 的 `choices[0].message.content` 取出文本，原样返回前端。

接口地址、模型名和超时在 `application.properties` 中（`ai.base-url`、`ai.model`、`ai.timeout-seconds`，默认 60 秒），API Key 在本地配置中。换用其他兼容 OpenAI 格式的大模型时，只需修改这几项配置。

## 个人路线接口

| 接口 | 说明 | 成功响应 |
|---|---|---|
| `POST /route/save` | 请求体 `destination`、`days`、`preference`、`routeContent` 必填，`tipsContent` 可选 | `msg` “保存成功”，`data` 为路线编号 |
| `GET /route/myList` | 无参数，按保存时间倒序，不分页 | `data` 为路线数组 |
| `DELETE /route/delete/{id}` | 无请求体 | `msg` “删除成功” |
| `PUT /route/collect/{id}` | 请求体 `{"isCollect":1}` 收藏，`0` 取消 | `msg` “设置成功” |

全部需要登录。每条路线包含 `id`、`destination`、`days`、`preference`、`routeContent`、`tipsContent`、`isCollect`、`createTime`，不返回 `userId`。请求示例见 `http/route.http`。

**如何保证只能操作自己的路线：**

1. 用户编号只从令牌取得：Controller 参数 `@AuthenticationPrincipal Long userId` 由 `JwtAuthFilter` 解析得到，请求体里即使带了 `userId` 也会被忽略（`TravelRoute.userId` 标注了 `@JsonIgnore`）。
2. 查询带 `WHERE user_id = ?`，只返回本人记录。
3. 删除和收藏的 SQL 同时限定 `WHERE id = ? AND user_id = ?`。路线不存在或属于别人时匹配不到记录，影响行数为 0，Service 据此返回 HTTP 400 和“路线不存在或不可操作”，数据不会被修改。

收藏按传入的值设置而不是取反，重复传 1 仍为收藏。

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
4. 打开终端给出的地址（通常是 `http://localhost:5173`），用演示账号 `testuser` / `123456` 登录。

前端包含城市与景点、登录注册、AI 路线规划、个人中心四个页面，页面说明和代码结构见 `frontend/README.md`。前端请求 `/api/...`，Vite 代理去掉 `/api` 后转发到后端，因此开发联调不需要另加后端跨域设置。

在 `frontend` 目录执行 `npm run build` 验证构建。提交 `package.json`、`package-lock.json` 和源码，不提交 `node_modules`、`dist`。修改 `vite.config.js` 后重启前端。Vite 代理仅适用于开发服务，构建后部署的转发设置后续补充。

## 小组协作

1. 接受 GitHub 仓库的协作邀请，然后克隆项目。
2. 开始新任务前切换到 `main` 并执行 `git pull --ff-only`。
3. 从最新 `main` 创建功能分支，例如 `feature/city-list`。
4. 完成功能并验证后，提交代码、推送分支，再创建 Pull Request。
5. 由另一位组员检查后合并到 `main`。

每位成员使用自己的 Git 提交姓名和邮箱。个人配置、密码、API Key、IDE 设置、构建产物和前端依赖不提交；源码、Maven Wrapper、配置示例、数据库脚本和项目文档需要提交。
