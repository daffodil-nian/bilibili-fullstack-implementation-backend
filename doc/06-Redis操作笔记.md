# Redis 操作笔记

结合本项目「专栏详情点击量」场景整理。对应：`ArticleServiceImpl.detail` / `incrArticleClick` + `RedisKey.ARTICLE_CLICK` + `RedisUtils`。

---

## 一、知识点（概念放一起）

### 1. 简单介绍

Redis 常放在应用和 MySQL 之间，做**高速读写**。本项目里登录 Token 已在用；专栏点击属于另一类高频场景——**读详情很频繁，每次都直接 `UPDATE articles.click_count` 会打爆数据库**，所以把「加一」先放到 Redis。

### 2. 和本项目相关的知识点归类

| 知识点 | 本项目怎么用 |
|---|---|
| **String 类型** | 一个 key 存一篇专栏的点击数字符串 |
| **原子计数 `INCR`** | 每次打开详情（权限通过后），对该 key +1 |
| **`SETNX` / `setIfAbsent`** | key 不存在时用 DB 的 `click_count` 种底，避免从 0 重算 |
| **写缓冲 / 热点计数** | 热写走 Redis，定时或异步再刷回 MySQL |
| **Key 命名** | `article:click:{articleId}` |
| **缓存与 DB 一致性** | Redis 是实时展示；DB 是最终落库 |
| **分布式锁（进阶，热路径一般不用）** | 仅「首次种底 / 多步回写」才考虑；点击加一本身不用锁 |

一句话：核心是 **String 原子计数 + 写缓冲 + 与 DB 最终一致**；锁不是每次点击都要。

### 3. String 与常用命令

Redis 的 String 不只是「字符串」，也可以存整数；`INCR` 会按整数理解并原子加一。

| 命令 / API | 含义 |
|---|---|
| `GET` / `get` | 读当前值 |
| `SET` / `set` | 直接覆盖（不管 key 在不在） |
| `SETNX` / `setIfAbsent` | key **不存在**才写入（适合种初始值） |
| `INCR` / `increment` | 原子 +1，返回加完后的值 |

`stringRedisTemplate.opsForValue().setIfAbsent(key, value)` 是 **Spring Data Redis 内置方法**，底层类似 `SET key value NX`。`RedisUtils` 只是包一层做日志和降级。

### 4. 为什么热路径不用分布式锁

`INCR` **单条命令在 Redis 里是原子的**：并发 100 个请求加点击，不会出现「都读到 10 再都写成 11」这种丢次数。

锁适合：先读后写、多步必须串行、复杂初始化。  
**每次点一下就加锁**会变慢。本项目约定：**点击只用 `INCR`；锁留给进阶场景。**

### 5. 专栏点击量流程（写缓冲）

```text
用户打开专栏详情
        ↓
权限校验通过（公开读者，或作者看自己的）
        ↓
incrArticleClick：无 key 则 setIfAbsent(DB基数) → INCR
        ↓
返回 ArticleDetailVO.clickCount = Redis 当前值
        ↓
（另路）定时任务把 Redis 值刷回 articles.click_count
```

「能不能看」由前面的 `Assert` 决定；「算不算点击」是产品选择——权限通过后调用 `incrArticleClick` 即可（作者自己看也可以算）。

### 6. 首次访问为什么要种底：`incrArticleClick` 在干什么

若 Redis 里还没有 key，直接从 0 `INCR`，会把 MySQL 里已有的历史点击量「忘掉」。

步骤：

1. `GET`：key 是否存在  
2. 不存在 → `setIfAbsent` 写入当前 DB 的 `click_count`（已有 key 不会被覆盖）  
3. 再 `INCR`：所有请求（含第一个）都原子 +1  
4. Redis 挂了 / `increment` 返回 `null` → 降级返回传入的 `dbClickCount`，不挡详情  

展示值 ≈ **历史库值 + 本次之后的增量**。  
若要「种底 + 自增」绝对无竞态，可用 **Lua** 合成一次执行（进阶，可选）。

### 7. `setIfAbsent` 返回值为什么写成 `Boolean.TRUE.equals(...)`

`setIfAbsent` 返回包装类 `Boolean`，可能为 `null`。  
`Boolean.TRUE.equals(x)`：只有真正是 `true` 才返回 `true`；`false`/`null` 都当失败，且**不会 NPE**。

### 8. 和「查库缓存」的区别

| | Cache Aside（常见读缓存） | 本项目点击量 |
|---|---|---|
| 方向 | 多是减轻**读**库 | 减轻**写**库 |
| 典型 | 先 Redis，没有再查 MySQL 再回填 | 先 Redis `INCR`，再异步写 MySQL |
| 称呼 | 缓存 | 更像**计数器 / 写缓冲** |

### 9. 与 MySQL 的一致性

- **展示**：详情优先用 Redis 返回值（失败则退回 DB）  
- **落库**：定时任务扫 `article:click:*`，`UPDATE articles SET click_count = ?`  
- **最终一致**：短时间 DB 可能略旧，阅读量场景通常可接受  
- **`detail` 不必 `@Transactional`**：无多表同事务需求；Redis `INCR` 也不归 Spring 事务管  

### 10. 和项目其他 Redis 用法对比

| 场景 | Key 思路 | 知识点 |
|---|---|---|
| 登录 Token（已有） | `user:token:uid_%d` | String 存会话、`EXPIRE` |
| 专栏点击（本笔记） | `article:click:%d` | `setIfAbsent` 种底 + `INCR` 写缓冲 |

### 11. 小结

1. 高频点击 → Redis `INCR`，不要每次打 MySQL。  
2. 属于 **String 原子计数 + 写缓冲**，不是必须上分布式锁。  
3. 第一次要把 DB 旧值种进 Redis（`setIfAbsent`），避免从 0 重算。  
4. DB 用定时/异步刷，接受短暂最终一致。  
5. `detail` 不用为点击量单独开事务。  

---

## 二、代码（实现放一起）

### 1. Key 常量（`RedisKey`）

```java
public static final String ARTICLE_CLICK = "article:click:%d";
```

### 2. 工具方法（`RedisUtils`）

```java
/** 原子 +1，对应 Redis INCR */
public static Long increment(String key) {
    try {
        return stringRedisTemplate.opsForValue().increment(key);
    } catch (Exception e) {
        log.error("redis increment error", e);
        return null;
    }
}

/**
 * 仅当 key 不存在时写入，对应 SETNX。
 * Boolean.TRUE.equals 避免返回 null 时拆箱 NPE。
 */
public static Boolean setIfAbsent(String key, String value) {
    try {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value));
    } catch (Exception e) {
        log.error("redis setIfAbsent error", e);
        return false;
    }
}
```

### 3. 业务方法（`ArticleServiceImpl`）

```java
/**
 * Redis 原子加点击：
 * 1) key 不存在 → setIfAbsent(DB 基数)
 * 2) 再 INCR
 * 3) Redis 失败则退回 dbClickCount
 */
private int incrArticleClick(Long articleId, int dbClickCount) {
    String key = RedisUtils.getKey(RedisKey.ARTICLE_CLICK, articleId);
    if (RedisUtils.get(key) == null) {
        RedisUtils.setIfAbsent(key, String.valueOf(dbClickCount));
    }
    Long after = RedisUtils.increment(key);
    return after == null ? dbClickCount : after.intValue();
}
```

`detail` 里权限通过后调用（示例）：

```java
int clickCount = article.getClickCount() == null ? 0 : article.getClickCount();
clickCount = incrArticleClick(articleId, clickCount);
// 组装 ArticleDetailVO 时用 clickCount
```

---

## 三、后续可写（未展开）

- 定时刷库任务示例  
- Lua：不存在则设底再 INCR  
- HyperLogLog 做 UV（和 PV 点击不是一回事）  
- Redisson 分布式锁适用边界再对比  
