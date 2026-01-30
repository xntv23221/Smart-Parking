# 智慧停车系统

## 作者信息
- 作者：xntv
- 联系方式：xntv23221@gmail.com
- GitHub主页：[https://github.com/xntv23221](https://github.com/xntv23221)

## 注意事项
- 本项目的用户端前端页面为移动端设计
- 用户端前端页面在开发环境下可使用浏览器的设备仿真功能进行调试与预览

## 项目部署

### 环境要求

#### 后端
- JDK 21+：项目使用Java 21开发
- Maven 3.6+：用于构建项目
- MySQL 8.4+：数据库服务
- Jetty 11.0.20：Web服务器

#### 前端
- Node.js 16+：运行环境
- npm 7+：包管理工具
- Vue 3.5.13：前端框架
- Vite 6.0.4：构建工具

### 环境变量配置
- 数据库连接信息：在 `backend/parking-bootstrap/src/main/resources/application.properties` 中配置
- Qwen AI API Key：在 `backend/parking-bootstrap/src/main/resources/application.properties` 中配置 `ai.qwen.apiKey`
- 高德地图 API Key（后端）：在 `backend/parking-api/src/main/java/com/smartparking/api/controller/client/AMapController.java` 中配置 `AMAP_KEY`
- 高德地图 API Key（前端）：在 `frontend/client/src/api/client.ts` 中配置

### 部署步骤
1. 克隆项目到本地
2. 执行数据库脚本：`backend/parking-dao/src/main/resources/db/smart_parking_mysql.sql`
3. 配置环境变量和API Key
4. 在项目根目录执行 `mvn clean package`
5. 运行 `java -jar backend/parking-bootstrap/target/parking-bootstrap-1.0.0-SNAPSHOT.jar`

### 访问地址
- **后端API服务**：`http://localhost:8080`（默认端口）
- **用户端前端**：`http://localhost:5174`（开发环境）
- **管理员端前端**：`http://localhost:5173`（开发环境）

> 注意：后端端口可以通过系统属性 `server.port` 或环境变量 `SERVER_PORT` 覆盖默认端口

### 登录信息

#### 用户端(手机/Web)
- 账号：user
- 密码：user123

#### 停车场管理员端(Manager)
- 账号：manager
- 密码：manager123

#### 系统管理员端(Admin)
- 账号：admin
- 密码：admin123
