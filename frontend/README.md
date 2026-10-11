# AI 智游前端

Vue3 + Vite + Vue Router + Axios + Element Plus。页面数据全部来自后端接口，不在前端写死。

## 启动

使用 Node.js 22.12+（当前验证环境为 22.22.2）。先按项目根目录 README 配置数据库并启动 Spring Boot 后端，再在本目录运行：

```bash
npm ci
npm run dev
```

打开终端显示的本地地址（通常是 `http://localhost:5173`）。演示账号 `testuser`，密码 `123456`。

## 页面

| 路径 | 页面 | 是否需要登录 |
|---|---|---|
| `/explore` | 城市与景点：城市表格；景点按城市筛选、分页（点击城市行也可筛选） | 否 |
| `/login` | 登录 / 注册（同一页面切换） | 否 |
| `/plan` | AI 路线规划：填写目的地、天数、偏好，生成路线和小贴士，保存或复制 | 是 |
| `/profile` | 个人中心：个人信息；我的路线的收藏、只看收藏、复制、删除 | 是 |

## 代码结构

```text
src/
├── main.js           创建应用，注册路由和 Element Plus（中文）
├── App.vue           顶部导航 + <router-view> 显示当前页面
├── router.js         页面路由；进入需要登录的页面前检查令牌
├── auth.js           登录状态（token、user），保存在 localStorage
├── request.js        Axios 封装：自动携带令牌、统一提示错误、401 跳转登录
├── copy.js           整理路线文本并复制到剪贴板
└── views/            四个页面：ExploreView、LoginView、PlanView、ProfileView
```

## 请求流程

```text
页面调用 request.get('/city/list')
→ request.js 加上 /api 前缀，已登录时在请求头加 Authorization: Bearer <token>
→ Vite 代理去掉 /api，转发到 http://localhost:8080/city/list
→ 后端返回 {code, msg, data}
→ request.js：code 为 200 时把 data 交给页面；否则弹出 msg 提示
```

后端返回 401（令牌缺失、过期或无效）时，`request.js` 清除登录状态并跳转登录页，登录后回到原页面。退出登录只清除浏览器中的令牌。

AI 生成较慢，生成请求单独设置 90 秒超时（后端调用大模型的超时为 60 秒）；生成期间按钮显示加载状态，避免重复提交。

修改代理配置后需要重启 Vite。代理只在开发服务中生效。

## 构建

```bash
npm run build
```

构建时出现 “Some chunks are larger than 500 kB” 是因为完整引入了 Element Plus，属于提示，不影响使用。

`package-lock.json` 需要提交，`node_modules/` 和 `dist/` 不提交。前端与后端使用总项目的同一个 Git 仓库。
