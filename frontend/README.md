# AI 智游前端

Vue3 + Vite + Axios + Element Plus。当前提供城市列表页面，数据来自后端 MySQL，不在前端写死。

## 启动

使用 Node.js 22.12+（当前验证环境为 22.22.2）。先按项目根目录 README 配置数据库并启动 Spring Boot 后端，再在本目录运行：

```bash
npm ci
npm run dev
```

打开终端显示的本地地址。页面加载时自动请求城市，点击“刷新”可以重新查询。请求失败会显示提示，无数据时显示“暂无城市数据”。

## 请求流程

```text
App.vue 请求 /api/city/list
→ Vite 代理到 http://localhost:8080/city/list
→ 后端查询 MySQL 的 city 表
→ result.data.data 赋给表格
```

修改代理配置后需要重启 Vite。代理只在开发服务中生效，生产部署的转发配置后续补充。

## 构建

```bash
npm run build
```

`package-lock.json` 需要提交，`node_modules/` 和 `dist/` 不提交。前端与后端使用总项目的同一个 Git 仓库。
