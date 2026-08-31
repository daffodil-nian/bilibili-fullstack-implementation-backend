# 仿照BILIBILI的系统

## 项目功能
项目还在完善中...

## 开发计划

### 2026-08-30 开发进度
- **登录 / 注册**：基本完成
- **修改用户信息**：基本完成
- **专栏发布**：入库、标签关联、已发布同步 ES
- **专栏详情**：权限校验、作者/标签/是否已赞、Redis 点击量
- **专栏互动表**：投币 / 收藏 / 转发（表 + 实体 + Mapper）；主表补充投币数、收藏数
- **评论相关 VO**：一级 / 二级评论 VO 已定；评论业务方法待写
- **Redis 笔记**：`doc/06-Redis操作笔记.md`（点击计数）

### 2026-08-31 计划（对接前端）
目标：先让前端能串起「登录 → 发专栏 → 看详情」，接口路径与返回体对齐。

**上午（必须）**
1. 补 `IArticleService`：把已有的 `publish`、`detail` 声明进接口  
2. 写 `ArticleController`（建议 `/api/articles`）  
   - `POST /api/articles` 发布（body：`ArticlePublishDto`，uid 从请求头）  
   - `GET /api/articles/{id}` 详情  
3. 与前端对齐：`Result` 结构、请求头 `uid`、跨域 CORS（若分开端口）  
4. 联调登录 `/api/auth/login`、注册、改资料 `/api/user/**`（已有则只对字段）

**下午（优先）**
5. 实现 `list` + `GET /api/articles` 分页列表（首页卡片）  
6. 实现 `toggleThumb` + `POST /api/articles/{id}/thumb`（详情页点赞）  
7. 把真实路径补进 `doc/毕设/04-接口设计.md`，方便前端对照  

**有余力再做**
8. 评论：`pageComments` / `addComment` + 两个接口  
9. 投币 / 收藏接口（表已齐）  
10. 搜索联调：`POST /api/search/all`

**对接注意**
- 拦截器要 `uid` 请求头；详情/发布需登录（搜索可白名单）  
- 详情返回 `ArticleDetailVO`；列表用瘦字段 `ArticleListVO`  
- 先约定错误码与 `Result` 成功/失败字段，避免前后端各猜一套  

### 用户功能

登录、注册、修改用户信息（基本完成）
