# AI 智能旅游路线规划系统

河北农业大学小组实训项目。用户输入目的地、出行天数和游玩偏好，系统生成旅游路线，并提供保存、查询、收藏等功能。

当前已创建 Spring Boot 后端工程；城市查询、用户登录、AI 路线规划和 Vue 前端将逐步开发。

## 开发环境

- Java 17
- Spring Boot 3.5.16
- Maven：项目提供 Maven Wrapper，版本为 3.9.10
- MySQL：建议小组统一使用 8.0
- 前端计划使用 Vue3，当前尚未创建

## 项目结构

```text
ai-travel/
├── src/main/java/cn/edu/hebau/aitravel/   后端代码
├── src/main/resources/                  应用配置
├── src/test/                            后端测试
├── .mvn/、mvnw、mvnw.cmd                Maven Wrapper
├── pom.xml                              后端依赖和构建配置
└── README.md                            项目说明
```

后续在根目录增加 `frontend/` 存放 Vue 项目，前后端使用同一个 Git 仓库，分别启动和构建。

## 数据库配置

每位组员使用自己电脑上的 MySQL，数据库名称统一为 `ai_travel`，账号和密码按个人环境填写。

1. 确认本地 MySQL 已启动，并且存在 `ai_travel` 数据库。已有数据库无需重新创建。
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

当前仅包含 MySQL 驱动，尚未加入 MyBatis/JDBC Starter，因此启动成功不代表数据库连接已经验证。接入 MyBatis 后再验证连接和城市查询。建表与测试数据 SQL 将在城市模块开发时加入仓库，组员届时使用同一份脚本初始化表结构。

## 本地启动

### 使用 IDEA

1. 打开项目根目录，设置 Project SDK 为 JDK 17。
2. 重新加载 Maven 项目，等待依赖下载完成。
3. 按上述说明准备自己的数据库配置。
4. 运行 `cn.edu.hebau.aitravel.AiTravelApplication`。
5. 控制台出现 `Started AiTravelApplication`，且 Web 服务监听 8080 端口，表示应用启动成功。

当前尚未编写业务接口，访问 `http://localhost:8080` 返回 404 属于预期情况。

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

## 小组协作

1. 接受 GitHub 仓库的协作邀请，然后克隆项目。
2. 开始新任务前切换到 `main` 并执行 `git pull --ff-only`。
3. 从最新 `main` 创建功能分支，例如 `feature/city-list`。
4. 完成功能并验证后，提交代码、推送分支，再创建 Pull Request。
5. 由另一位组员检查后合并到 `main`。

每位成员使用自己的 Git 提交姓名和邮箱。个人配置、密码、API Key、IDE 设置、构建产物和前端依赖不提交；源码、Maven Wrapper、配置示例、数据库脚本和项目文档需要提交。
