# ES官方提供Java API调用

## 配置参数

官方文档：https://www.elastic.co/docs/reference/elasticsearch/clients/java/getting-started

```java
// URL and API key
String serverUrl = "https://localhost:9200";
String apiKey = "VnVhQ2ZHY0JDZGJrU...";

ElasticsearchClient esClient = ElasticsearchClient.of(b -> b
    .host(serverUrl)
    .apiKey(apiKey)
);

// Use the client...

// Close the client, also closing the underlying transport object and network connections.
esClient.close();


```

根据上面的用法创建一个elasticsearch的客户端，结束的时候可以调用close方法

## 搜索数据

```java
SearchResponse<Product> search = esClient.search(s -> s
    .index("products")
    .query(q -> q
        .term(t -> t
            .field("name")
            .value(v -> v.stringValue("bicycle"))
        )),
    Product.class);

for (Hit<Product> hit: search.hits().hits()) {
    processProduct(hit.source());
}
```

通过上面的用法可以搜索索引值为products找到name的名字为bicycle的产品信息

# Spring Data Elasticsearch

官方文档路径：https://spring.io/projects/spring-data-elasticsearch

## 客户端配置

```java
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class MyClientConfig extends ElasticsearchConfiguration {

	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()           (1)
			.connectedTo("localhost:9200")
			.build();
	}
}
```



```java
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
ElasticsearchOperations operations;      (1)

@Autowired
ElasticsearchClient elasticsearchClient; (2)

@Autowired
Rest5Client rest5Client;                 (3)

@Autowired
JsonpMapper jsonpMapper;                 (4)
```

我们用ElasticsearchClient



## 对象映射

### 先搞懂两个核心最常用类型

1. **`FieldType.Text`**：分词文本

- 存入 ES 的时候会分词拆词。
- ✅用于：昵称 nickname、视频标题 title、简介 signature，**需要全文搜索、关键词检索**。
- ❌不能用来精确匹配、排序。

1. **`FieldType.Keyword`**：完整原始字符串，**不分词**

- 原样一整个字符串存，不会拆字。
- ✅用于：头像 url、标签、状态值，做精确过滤、排序、聚合。
- ❌不能做模糊关键词搜索。

> 举例子： nickname = "罗小黑 Official"
>
> - Text：会拆成「罗、小、黑、official」，搜 “小黑” 能搜到这条。
> - Keyword：完整字符串`"罗小黑Official"`，必须完整一模一样才能匹配。

其他常用：

- `Integer`：粉丝数、视频数量、B 站等级，数字
- `Date`：发布时间
- `Boolean`：是否公开

### 重点讲 `FieldType.Auto`（默认行为，不写 type 就这个）

> `FieldType.Auto`：**Java 代码不定义 mapping，交给 ES 动态推断**。

### 会发生什么：

你 DTO 上不写`@Field(type=xxx)`

```
@Field
private String nickname;
```

或者干脆不写`@Field`注解。

当你第一条数据保存进 ES：

- Java String → ES 自动猜成 `text + keyword` 双字段
- Java Integer → ES 自动映射 integer
- Java boolean → boolean

✅好处：省事，不用写一堆注解。 ❌坑点（**项目千万不要依赖 Auto，很容易翻车**）



## 核心概念

```java
public interface CrudRepository<T, ID> extends Repository<T, ID> {

  <S extends T> S save(S entity);

  Optional<T> findById(ID primaryKey);

  Iterable<T> findAll();

  long count();

  void delete(T entity);

  boolean existsById(ID primaryKey);

  // … more functionality omitted.
}

```

该jdk为我们提供上述接口。

```java
interface PagingAndSortingRepository<T, ID> extends Repository<T, ID> {

  Iterable<T> findAll(Sort sort);

  Page<T> findAll(Pageable pageable);
}
```

这个接口专门提供排序+分页查询能力

用法如下

```java
PagingAndSortingRepository<User, Long> repository = // … get access to a bean
Page<User> users = repository.findAll(PageRequest.of(1, 20));
```



```java
interface UserRepository extends CrudRepository<User, Long> {

  long countByLastname(String lastname);
}
```

这是派生计数查询Derived Count Query，返回的是命中文档的总数。

```java
interface UserRepository extends CrudRepository<User, Long> {

  long deleteByLastname(String lastname);

  List<User> removeByLastname(String lastname);
}
```

lastname是姓，是实体User的一个属性，上述接口功能为删掉所以lastname等于参数的文档以及把删掉lastname为参数的User对象以列表形式返回。

### 实体检测策略

我把原文的内容复制过来了

| `@Id`-Property inspection (the default) | By default, Spring Data inspects the identifier property of the given entity. If the identifier property is `null` or `0` in case of primitive types, then the entity is assumed to be new. Otherwise, it is assumed to not be new. |
| --------------------------------------- | ------------------------------------------------------------ |
| `@Version`-Property inspection          | If a property annotated with `@Version` is present and `null`, or in case of a version property of primitive type `0` the entity is considered new. If the version property is present but has a different value, the entity is considered to not be new. If no version property is present Spring Data falls back to inspection of the identifier property. |
| Implementing `Persistable`              | If an entity implements `Persistable`, Spring Data delegates the new detection to the `isNew(…)` method of the entity. See the [Javadoc](https://docs.spring.io/spring-data/commons/reference/4.1/api/java/org/springframework/data/domain/Persistable.html) for details.*Note: Properties of `Persistable` will get detected and persisted if you use `AccessType.PROPERTY`. To avoid that, use `@Transient`.* |

简单来说@Id规则如下：如果是引用类型如Long,String，ID为NULL就被判定为新实体，就新增，如果有值就更新，同理，如果是基本类型如long，值为0就判定为新实体否则更新。

@Version

| 项目            | @Id 策略 (无 @Version 时使用)           | @Version 策略 (实体有 @Version 就优先使用)        |
| --------------- | --------------------------------------- | ------------------------------------------------- |
| 判断依据        | ID 字段的值                             | version 字段的值                                  |
| 新实体条件      | 引用类型 ID 为 null；基本类型 ID 等于 0 | 引用类型 version 为 null；基本类型 version 等于 0 |
| 优先级          | 低，没有 @Version 才轮到它              | 高，只要存在 @Version 就优先执行                  |
| ID/version 有值 | ID 不为 null / 不为 0 →旧实体           | version 有值 →旧实体                              |





### 创建查询

```java
interface PersonRepository extends Repository<Person, Long> {
  // 1
  List<Person> findByEmailAddressAndLastname(EmailAddress emailAddress, String lastname);

  // 2 distinct 去重，两种写法等效
  List<Person> findDistinctPeopleByLastnameOrFirstname(String lastname, String firstname);
  List<Person> findPeopleDistinctByLastnameOrFirstname(String lastname, String firstname);

  // 3 单个字段忽略大小写
  List<Person> findByLastnameIgnoreCase(String lastname);

  // 4 全部条件字段忽略大小写
  List<Person> findByLastnameAndFirstnameAllIgnoreCase(String lastname, String firstname);

  // 5 查询同时静态排序
  List<Person> findByLastnameOrderByFirstnameAsc(String lastname);
  List<Person> findByLastnameOrderByFirstnameDesc(String lastname);
}
```





```javascript
interface BookRepository extends Repository<Book, String> {
  List<Book> findByNameAndPrice(String name, Integer price);
}
```

上述方法等效写法如下:

```json
{
    "query": {
        "bool" : {
            "must" : [
                { "query_string" : { "query" : "?", "fields" : [ "name" ] } },
                { "query_string" : { "query" : "?", "fields" : [ "price" ] } }
            ]
        }
    }
}
```

## 定义仓库接口





```java
interface MyRepository extends JpaRepository<User, Long> { }
这是实体专用的仓库接口！
    
    
    
它本身不能直接注入使用！它只是模板，用来被别的接口继承。
如果不加`@NoRepositoryBean`，Spring 会尝试实例化这个泛型接口，直接启动报错。
@NoRepositoryBean
interface MyBaseRepository<T, ID> extends JpaRepository<T, ID> { … }

interface UserRepository extends MyBaseRepository<User, Long> { … }




```

| 接口             | 注解                | Spring 是否生成 Bean | 用途                                       |
| ---------------- | ------------------- | -------------------- | ------------------------------------------ |
| MyRepository     | 无                  | ✅生成                | 直接针对 User 的仓库，不可复用             |
| MyBaseRepository | `@NoRepositoryBean` | ❌不生成，仅模板      | 抽取所有实体共用的通用方法，作为父接口     |
| UserRepository   | 无                  | ✅生成                | 业务实际使用，继承基础模板，针对 User 实体 |

## ES仓库



```java
@Document(indexName="books")
class Book {
    @Id
    private String id;
@Field(type = FieldType.Text)
private String name;

@Field(type = FieldType.Text)
private String summary;

@Field(type = FieldType.Integer)
private Integer price;

// getter/setter ...
}
```
注意，在elasticsearch中，id就是字符串类型的







```java
interface BookRepository extends Repository<Book, String> {
@Highlight(fields = {
    @HighlightField(name = "name"),
    @HighlightField(name = "summary")
})
SearchHits<Book> findByNameOrSummary(String text, String summary);
}
```
The `@Highlight` annotation on a repository method defines for which fields of the returned entity highlighting should be included.To search for some text in a `Book` 's name or summary and have the found data highlighted, the following repository method can be used:

感觉没啥用





```java
interface BookRepository extends Repository<Book, String> {
@SourceFilters(includes = "name")
SearchHits<Book> findByName(String text);
}
```
`@SourceFilters` 就是做 **_source 源过滤（Source Filtering）**，目的就是：**不要返回文档全部字段，只返回指定的一部分字段，减少网络传输的数据量**。



### 领域事件

实际上，领域对象在英语中是一个完成后的状态，表示事件已经发生了。



```java
class AnAggregateRoot {
@DomainEvents
Collection<Object> domainEvents() {
    // … return events you want to get published here
}

@AfterDomainEventPublication
void callbackMethod() {
   // … potentially clean up domain events list
}
}
```
举个简单例子

#### 领域事件POJO

```java
/**
 * 领域事件：用户已经注册完成（过去式，已经发生）
 */
public class UserRegisteredEvent {
    private final Long userId;
    private final String username;

    public UserRegisteredEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
```



#### 聚合根User使用@DomainEvents

```java
import jakarta.persistence.*;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    // transient：内存临时存放待发布领域事件，不会持久化到数据库
    @Transient
    private List<Object> domainEvents = new ArrayList<>();

    // ---------------- 领域业务方法：注册
    public void register(String username, String password) {
        // 领域内业务逻辑：设置自身状态
        this.username = username;
        this.password = password;

        // ✅业务执行完成，产生【已经发生】的领域事件，加入内存列表
        domainEvents.add(new UserRegisteredEvent(this.id, username));
    }

    // ---------------- Spring Data JPA 读取事件
    @DomainEvents
    public Collection<Object> domainEvents() {
        // 返回待发布的事件集合给框架
        return domainEvents;
    }

    // ---------------- 事件全部发布完毕之后回调
    @AfterDomainEventPublication
    public void clearEvents() {
        // 清空，避免下次save重复发布旧事件！
        domainEvents.clear();
    }

    // getter setter
    public Long getId() { return id; }
    public String getUsername() { return username; }
}
```

简单来说领域事件就是一张记录发生过的事件的小纸条

### CDI集合

上下文与依赖注入

```java
class ElasticsearchTemplateProducer {

  @Produces
  @ApplicationScoped
  public ElasticsearchOperations createElasticsearchTemplate() {
    // ...
  }
}

class ProductService {

  private ProductRepository repository;
  public Page<Product> findAvailableBookByName(String name, Pageable pageable) {
    return repository.findByAvailableTrueAndNameStartingWith(name, pageable);
  }
  @Inject
  public void setRepository(ProductRepository repository) {
    this.repository = repository;
  }
}
```

这里Produces是想告诉CDI，别人需要这个类型的对象就调用这个方法用它返回结果

@Application告诉CDI，这个对象要放入应用房间的上下文中。

@Inject是依赖注入

## Dao层命名方法



| Keyword                                       | Sample                                     | Elasticsearch Query String                                   |
| :-------------------------------------------- | :----------------------------------------- | :----------------------------------------------------------- |
| `And`                                         | `findByNameAndPrice`                       | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "?", "fields" : [ "name" ] } }, { "query_string" : { "query" : "?", "fields" : [ "price" ] } } ] } }}` |
| `Or`                                          | `findByNameOrPrice`                        | `{ "query" : { "bool" : { "should" : [ { "query_string" : { "query" : "?", "fields" : [ "name" ] } }, { "query_string" : { "query" : "?", "fields" : [ "price" ] } } ] } }}` |
| `Is`                                          | `findByName`                               | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "?", "fields" : [ "name" ] } } ] } }}` |
| `Not`                                         | `findByNameNot`                            | `{ "query" : { "bool" : { "must_not" : [ { "query_string" : { "query" : "?", "fields" : [ "name" ] } } ] } }}` |
| `Between`                                     | `findByPriceBetween`                       | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : ?, "to" : ?, "include_lower" : true, "include_upper" : true } } } ] } }}` |
| `LessThan`                                    | `findByPriceLessThan`                      | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : null, "to" : ?, "include_lower" : true, "include_upper" : false } } } ] } }}` |
| `LessThanEqual`                               | `findByPriceLessThanEqual`                 | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : null, "to" : ?, "include_lower" : true, "include_upper" : true } } } ] } }}` |
| `GreaterThan`                                 | `findByPriceGreaterThan`                   | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : ?, "to" : null, "include_lower" : false, "include_upper" : true } } } ] } }}` |
| `GreaterThanEqual`                            | `findByPriceGreaterThanEqual`              | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : ?, "to" : null, "include_lower" : true, "include_upper" : true } } } ] } }}` |
| `Before`                                      | `findByPriceBefore`                        | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : null, "to" : ?, "include_lower" : true, "include_upper" : true } } } ] } }}` |
| `After`                                       | `findByPriceAfter`                         | `{ "query" : { "bool" : { "must" : [ {"range" : {"price" : {"from" : ?, "to" : null, "include_lower" : true, "include_upper" : true } } } ] } }}` |
| `Like`                                        | `findByNameLike`                           | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "?*", "fields" : [ "name" ] }, "analyze_wildcard": true } ] } }}` |
| `StartingWith`                                | `findByNameStartingWith`                   | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "?*", "fields" : [ "name" ] }, "analyze_wildcard": true } ] } }}` |
| `EndingWith`                                  | `findByNameEndingWith`                     | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "*?", "fields" : [ "name" ] }, "analyze_wildcard": true } ] } }}` |
| `Contains/Containing`                         | `findByNameContaining`                     | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "*?*", "fields" : [ "name" ] }, "analyze_wildcard": true } ] } }}` |
| `In` (when annotated as FieldType.Keyword)    | `findByNameIn(Collection<String>names)`    | `{ "query" : { "bool" : { "must" : [ {"bool" : {"must" : [ {"terms" : {"name" : ["?","?"]}} ] } } ] } }}` |
| `In`                                          | `findByNameIn(Collection<String>names)`    | `{ "query": {"bool": {"must": [{"query_string":{"query": "\"?\" \"?\"", "fields": ["name"]}}]}}}` |
| `NotIn` (when annotated as FieldType.Keyword) | `findByNameNotIn(Collection<String>names)` | `{ "query" : { "bool" : { "must" : [ {"bool" : {"must_not" : [ {"terms" : {"name" : ["?","?"]}} ] } } ] } }}` |
| `NotIn`                                       | `findByNameNotIn(Collection<String>names)` | `{"query": {"bool": {"must": [{"query_string": {"query": "NOT(\"?\" \"?\")", "fields": ["name"]}}]}}}` |
| `True`                                        | `findByAvailableTrue`                      | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "true", "fields" : [ "available" ] } } ] } }}` |
| `False`                                       | `findByAvailableFalse`                     | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "false", "fields" : [ "available" ] } } ] } }}` |
| `OrderBy`                                     | `findByAvailableTrueOrderByNameDesc`       | `{ "query" : { "bool" : { "must" : [ { "query_string" : { "query" : "true", "fields" : [ "available" ] } } ] } }, "sort":[{"name":{"order":"desc"}}] }` |
| `Exists`                                      | `findByNameExists`                         | `{"query":{"bool":{"must":[{"exists":{"field":"name"}}]}}}`  |
| `IsNull`                                      | `findByNameIsNull`                         | `{"query":{"bool":{"must_not":[{"exists":{"field":"name"}}]}}}` |
| `IsNotNull`                                   | `findByNameIsNotNull`                      | `{"query":{"bool":{"must":[{"exists":{"field":"name"}}]}}}`  |
| `IsEmpty`                                     | `findByNameIsEmpty`                        | `{"query":{"bool":{"must":[{"bool":{"must":[{"exists":{"field":"name"}}],"must_not":[{"wildcard":{"name":{"wildcard":"*"}}}]}}]}}}` |
| `IsNotEmpty`                                  | `findByNameIsNotEmpty`                     | `{"query":{"bool":{"must":[{"wildcard":{"name":{"wildcard":"*"}}}]}}}` |