# 牛客社区项目还原

这是基于 `niuke.md` 还原出来的一套完整社区项目，包含后端、前端和 AI 服务三部分。

## 项目结构

- `backend`
  - Spring Boot 3
  - MVC 三层结构：`controller / service / mapper`
  - JDK 21
- `frontend`
  - Vue 3 + Vite + Element Plus
  - Node.js 18.20.4
- `python-ai-service`
  - FastAPI + LangChain
  - Python 3.12.2
- `docker-compose.middleware.yml`
  - 中间件一键启动文件

## 已实现模块

- 注册、登录、登出
- Redis 登录态
- 首页帖子列表
- 发帖、评论、回复
- 点赞、关注
- 通知、未读统计、全部已读
- 私信、WebSocket 基础消息收发
- 个人主页、资料设置、头像上传
- 搜索、热榜、UV 统计
- 敏感词过滤（AC 自动机版）
- 角色权限、管理员置顶/加精
- RabbitMQ 通知与帖子同步链路
- Elasticsearch 搜索接入
- AI 助手页与 Python LangChain 服务

## 当前状态

当前状态已经进入“运行联调阶段”：

- 后端：`mvn compile` 已通过
- 前端：`npm run build` 已通过
- Python AI 服务：依赖安装已通过

还没有完成的工作主要是：

- 填写真实环境参数
- 启动 MySQL / Redis / RabbitMQ / Elasticsearch
- 启动后端、前端、AI 服务并联调
- 根据真实运行结果继续修正个别运行时问题

## 环境要求

- JDK 21
- Maven 3.9.x
- Node.js 18.20.4
- Python 3.12.2
- MySQL 8.0.42
- Redis
- RabbitMQ
- Elasticsearch 8.x

## 中间件启动

如果你的中间件跑在虚拟机中的 Docker 里，可以使用项目根目录下的文件：

- [docker-compose.middleware.yml](C:\Users\17431\Desktop\niu\docker-compose.middleware.yml)

在虚拟机中执行：

```bash
docker compose -f docker-compose.middleware.yml up -d
```

该文件会启动：

- MySQL 8.0.42
- Redis 7.2
- RabbitMQ 3-management
- Elasticsearch 8.15.3

## 配置文件

后端主配置文件：

- [application.yml](C:\Users\17431\Desktop\niu\backend\src\main\resources\application.yml)

当前已经改成“虚拟机 IP 占位版”，你需要替换这些值：

- `YOUR_VM_IP`
- `your_email@example.com`
- `your_mail_auth_code`
- `replace-with-your-jwt-secret-key-at-least-32-bytes`

重点配置项：

```yaml
spring:
  datasource:
    url: jdbc:mysql://YOUR_VM_IP:3306/niu_community?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: root

  data:
    redis:
      host: YOUR_VM_IP
      port: 6379

  rabbitmq:
    host: YOUR_VM_IP
    port: 5672
    username: guest
    password: guest

  elasticsearch:
    uris: http://YOUR_VM_IP:9200

  mail:
    username: your_email@example.com
    password: your_mail_auth_code

jwt:
  secret: replace-with-your-jwt-secret-key-at-least-32-bytes

app:
  ai-service-url: http://localhost:8000
```

AI 服务配置文件：

- [`.env.example`](C:\Users\17431\Desktop\niu\python-ai-service\.env.example)

复制为 `.env` 后填写：

```env
DEBUG=False
JAVA_API_BASE_URL=http://localhost:8080/api
DEEPSEEK_API_KEY=你的真实API_KEY
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL=deepseek-chat
TEMPERATURE=0.7
MAX_TOKENS=2000
MAX_HISTORY=10
```

## 数据库初始化

初始化 SQL 文件：

- [schema.sql](C:\Users\17431\Desktop\niu\backend\src\main\resources\schema.sql)

在 MySQL 中创建好 `niu_community` 数据库后，执行该 SQL 文件。

## 启动顺序

### 1. 启动中间件

先确保这些服务已经可访问：

- MySQL: `YOUR_VM_IP:3306`
- Redis: `YOUR_VM_IP:6379`
- RabbitMQ: `YOUR_VM_IP:5672`
- Elasticsearch: `YOUR_VM_IP:9200`

### 2. 启动后端

```powershell
cd C:\Users\17431\Desktop\niu\backend
mvn spring-boot:run
```

### 3. 启动前端

```powershell
cd C:\Users\17431\Desktop\niu\frontend
npm install
npm run dev
```

### 4. 启动 AI 服务

```powershell
cd C:\Users\17431\Desktop\niu\python-ai-service
python -m venv .venv
.\.venv\Scripts\python -m pip install -r requirements.txt
.\.venv\Scripts\python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## 当前关键文件

后端核心：

- [pom.xml](C:\Users\17431\Desktop\niu\backend\pom.xml)
- [application.yml](C:\Users\17431\Desktop\niu\backend\src\main\resources\application.yml)
- [schema.sql](C:\Users\17431\Desktop\niu\backend\src\main\resources\schema.sql)
- [PostEsService.java](C:\Users\17431\Desktop\niu\backend\src\main\java\com\niu\community\es\service\PostEsService.java)
- [RabbitMqConfig.java](C:\Users\17431\Desktop\niu\backend\src\main\java\com\niu\community\mq\config\RabbitMqConfig.java)

前端核心：

- [package.json](C:\Users\17431\Desktop\niu\frontend\package.json)
- [vite.config.js](C:\Users\17431\Desktop\niu\frontend\vite.config.js)
- [HomeView.vue](C:\Users\17431\Desktop\niu\frontend\src\views\HomeView.vue)
- [PostDetailView.vue](C:\Users\17431\Desktop\niu\frontend\src\views\PostDetailView.vue)
- [AiView.vue](C:\Users\17431\Desktop\niu\frontend\src\views\AiView.vue)

AI 服务核心：

- [requirements.txt](C:\Users\17431\Desktop\niu\python-ai-service\requirements.txt)
- [main.py](C:\Users\17431\Desktop\niu\python-ai-service\app\main.py)
- [forum_agent.py](C:\Users\17431\Desktop\niu\python-ai-service\app\agent\forum_agent.py)
- [forum_tools.py](C:\Users\17431\Desktop\niu\python-ai-service\app\tools\forum_tools.py)

## 已验证结果

我已经实际验证过：

- 后端可以执行 `mvn compile`
- 前端可以执行 `npm run build`
- Python AI 服务依赖可以安装成功

## 注意事项

- 当前配置里大量参数仍然是占位符，未填真实值前不要直接用于生产
- 如果 Docker 跑在虚拟机里，后端配置必须使用虚拟机 IP，不能继续用 `localhost`
- Elasticsearch、RabbitMQ、Redis 这些功能已经接入代码，但必须服务真实可用才能运行完整功能
- AI 服务必须填写真实 `DEEPSEEK_API_KEY`

## 建议的联调顺序

1. 先跑通 MySQL + 后端基础接口
2. 再确认 Redis 登录态正常
3. 再接 RabbitMQ / Elasticsearch
4. 最后联调 AI 服务
