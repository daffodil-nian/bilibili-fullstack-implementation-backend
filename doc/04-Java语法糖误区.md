# Java 语法糖 / 常见误区（含今天 ES 搜索相关）

> 本文把「写法看起来像语法糖、其实容易想错」的点和今天做 B 站用户搜索时踩过的坑写在一起，尽量口语化。

---

## 1. 列表、Stream、`toList()` —— 不是魔法，是换种写法的 for

### 1.1 `ArrayList` / `List` 是啥

- `List`：接口（约定「有序、可按下标取」）
- `ArrayList`：一种具体实现，**元素可以重复**
- 你业务里经常写 `List<UserEsDoc>`，运行时多半是 `ArrayList`

### 1.2 这段在干啥

```java
List<UserEsDoc> list = hits.getSearchHits().stream()
        .map(SearchHit::getContent)
        .toList();
```

用普通 for 理解就是：

```java
List<UserEsDoc> list = new ArrayList<>();
for (SearchHit<UserEsDoc> hit : hits.getSearchHits()) {
    list.add(hit.getContent());  // map：每个 hit 变成里面的文档
}
// toList()：收集成不可变 List（Java 16+）
```

| 写法 | 人话 |
|------|------|
| `stream()` | 把集合变成「可以一节节加工」的流水线 |
| `map(...)` | 每个元素变一次形（这里取出 ES 命中里的 `UserEsDoc`） |
| `SearchHit::getContent` | 方法引用，等于 `hit -> hit.getContent()` |
| `toList()` | 收成 `List`（注意：一般是**不可变**的，不能再 `add`） |

**误区：** 以为 `stream` 是新集合类型。不是，它是处理方式；最后还是要落到 `List` / `forEach` 等。

---

## 2. `switch` 箭头语法（`->`）—— 每个分支要有「完整动作」

今天编译报错过：空的 `case VIDEO -> { }` 在有的写法下不好使。

```java
// 推荐：每个分支要么 return，要么明确写完
switch (searchTypeEnum) {
    case USER -> {
        return doSearchUser(...);
    }
    case VIDEO -> {
        return null; // 暂时没做也要有语句
    }
    default -> {
        return null;
    }
}
```

**误区：** 以为箭头 `switch` 可以像以前 `case X:` 一样空着再「掉到最后的 return」。更稳妥：每个 `->` 分支自己 `return` / 赋值。

另外：若 `searchTypeEnum` 是 **null**，一进 `switch` 就会 NPE。枚举的 `of()` 一定要把 cache 填满。

---

## 3. 导包陷阱 —— IDE 自动导入也会导错

今天两次踩坑，看起来像「语法不会」，其实是 **导错类**：

| 你要用的 | 正确包 | 错误常见导入 |
|----------|--------|----------------|
| 排序 | `org.springframework.data.domain.Sort` | `SpringDataWebProperties.Sort` |
| 分页参数 | `org.springframework.data.domain.Pageable` | `SpringDataWebProperties.Pageable` |
| 分页请求 | `org.springframework.data.domain.PageRequest` | 自己写的 `common.PageRequest`（名字撞车） |

**人话：**  
`SpringDataWebProperties` 里的东西是 **配置属性**，不是拿来写查询的。  
分页/排序一律认准 **`org.springframework.data.domain`**。

自己项目里的：

```java
org.arrinna...common.PageRequest  // 前端入参：currentNum、pageSize
org.springframework.data.domain.PageRequest  // 调 ES/JPA 用
```

两个都叫 PageRequest，**不要混**。转的时候：

```java
Pageable pageable = org.springframework.data.domain.PageRequest.of(
        (int) currentNum - 1,  // Spring 从 0 页开始
        (int) pageSize,
        sort
);
```

---

## 4. `@Override` 加在哪

- **加在：** 实现接口的公开方法上，例如 `searchUser(SearchEsReq req)`
- **不要加在：** `private doSearchUser(...)` 这种内部私有方法（接口里没有它）

前提：`ISearchService` 里要先声明 `searchUser`，Impl 才能合法 `@Override`。

---

## 5. Lombok `@Data` —— 语法糖，但 ES 文档很需要它

```java
@Data
public class UserEsDoc { ... }
```

约等于自动生成 getter/setter/`toString` 等。

**误区：** 以为不写 getter，Spring Data / JSON / `doc.getNickname()` 也能用。  
没有 getter，映射和你自己的 `map` 转换经常挂。实体、`UserEsDoc` 建议加 `@Data`（或手写 getter）。

---

## 6. 今天最大的架构误区（比语法更重要）

### 6.1 别把「搜索请求」当成 ES 文档

| 角色 | 该是谁 | 误区 |
|------|--------|------|
| 入参 | `SearchEsReq` / `SearchDto`（keyword、searchType、sortCode、分页） | 拿去 `extends ElasticsearchRepository<SearchDto, …>` |
| ES 文档 | `UserEsDoc`（`@Document`，真写进索引的字段） | 和入参混成一个类 |
| 返回 | `SearchVO` / `UserVO` | 和 Doc 强行同一个 |

`ElasticsearchRepository<T, ID>` 的 `T` = **索引里的文档类型**，不是查询条件。

### 6.2 `UserEsDao` 不用你手写 Impl

```java
public interface UserEsDao extends ElasticsearchRepository<UserEsDoc, Long> {
}
```

- Spring 启动后会生成代理，自带 `save` / `findById` / `deleteById`
- **不要**写 `class UserEsDaoImpl implements UserEsDao`
- **不要**让 `UserEsSearch implements UserEsDao`

| 类 | 干什么 |
|----|--------|
| `UserEsDao` | 同步：MySQL → ES 的 `save` / `saveAll` |
| `UserEsSearch` | 复杂搜索：拆字 + bool must（普通 `@Component`） |

复杂规则用 `ElasticsearchOperations` 手写查询；简单 CRUD 用 Repository。这和 yuso「Dao 同步 + Service/Template 搜索」是一类思路。

### 6.3 自定义 settings（ngram）不在 Dao 里「实现」

```text
bili-user-settings.json  → 定义 ngram_analyzer
UserEsDoc @Setting + nickname 的 analyzer  → 建索引时生效
UserEsDao.save                         → 写入时自动按分词器切字
```

Dao 只负责存取；**怎么切词是索引 mapping 的事**。

### 6.4 Text / Keyword / 分词器 别混

- **字段类型 `Text`：** 会分析，适合搜昵称  
- **字段类型 `Keyword`：** 整串精确，适合头像 URL、排序关键字  
- **分词器（analyzer/tokenizer）：** 挂在 Text 上，决定怎么切，例如：
  - `standard`、`ik_max_word`（中文插件）
  - **`ngram`**（今天用的）：可切成单字（`min_gram=max_gram=1`）

**误区：** 以为「选对一个分词器」就等于「乱序、多字都要命中」。  
分词器只管 **切不切得开**；「每个字都要出现、可乱序」要靠 **查询**：拆字 + `bool.must`。

你的规则对应关系：

| 需求 | 靠什么 |
|------|--------|
| 单字能搜到 | ngram `min_gram=1` 进索引 |
| 小宇 / 小宇内耗 / 内耗宇 | 查询拆字 + must |
| 我我我我算 1 个我 | 拆字时用 `Set` 去重 |

### 6.5 MySQL 不会自动进 ES

```text
MySQL = 主库（真相）
ES    = 搜索副本（要自己 save）
```

配了 `spring.elasticsearch.uris` 只表示 **连得上**，不是「表会自动同步」。

闭环建议（今天结论：**不必上领域事件**）：

1. **全量 Job**（`job/once`）：历史用户 `saveAll` 一次  
2. **写时同步**：注册 / 改昵称成功后直接 `userEsDao.save(doc)`  
3. **可选增量 Job**（`job/cycle`）：定时扫 `updateTime` 兜底  
4. **搜索**：只查 ES（`UserEsSearch`）

`job` 包 = 放启动任务 / 定时任务的文件夹，是 **应用里的自动任务**，不是外网机器人，也和是不是外网无关。yuso 也是本地 ES + Job/`save`。

### 6.6 `searchType` 和 `sortCode` 别传混

- `searchType` → `SearchTypeEnum`（用户 / 视频 / 专栏）  
- `sortCode` → `UserSortEnum`（按粉丝 / 等级）  

`doSearchUser` 第二个参数应是 **`sortCode`**，不是 `searchType`。

### 6.7 Controller 小误区

- `@GetMapping` + `@RequestBody`：很多客户端 GET 不带 body，不好对接 → 用 **POST + body** 或 **GET + query**  
- 需要登录拦截时记得 header 带 `uid`  
- 成功文案可放到 `DefaultConstant.SEARCH_SUCCESS_MSG = "搜索成功"`，`Result.Success(data, msg)`

### 6.8 枚举 `of` 的 cache 要填充

```java
static {
    for (SearchTypeEnum e : values()) {
        cache.put(e.getNo(), e);
    }
}
```

**误区：** 写了 `Map cache` 和 `of()`，却忘了 put，结果永远 `null`。

---

## 7. 分页相关语法糖理解

```java
Page<UserEsDoc> page = userEsSearch.searchNickname(keyword, pageable);
page.getContent();        // 当前页列表
page.getTotalElements();  // 总条数
```

`Page` 是 Spring Data 的分页结果包装，不是 hutool 的 `cn.hutool.db.Page`，**别导错**。

---

## 8. 今天结论速查

| 问题 | 短答 |
|------|------|
| Repository 要不要自己写实现类？ | 不要，Spring 生成代理 |
| `UserEsSearch` 要实现 Dao 吗？ | 不要，另起组件 |
| 分词器用啥？ | 按字搜：ngram min=max=1；规则靠拆字 must |
| ES 为啥没用户数据？ | 没同步；要 Job + save |
| 要不要领域事件？ | mock 阶段不必，直接 save 即可 |
| ES 能存多大？ | 用户这种小文档毫无压力；别存视频文件 |

---

## 9. 和语法糖的关系（收个尾）

今天很多报错看起来像「Java 不会」，拆开其实是：

1. **真语法糖：** Stream、`->` switch、`@Data`、方法引用 —— 要知道等价于啥  
2. **假语法问题：** 导错包、枚举 cache、架构角色弄混 —— 编译器/运行时也会炸  

先分清「这段语法在干什么」，再分清「ES / Dao / 同步各干什么」，搜索链路才闭环。

## 10.补充

taskkill /PID 12345 /F

netstat -ano | findstr :9200
