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



## 实现搜索方法

首先要准备好数据库表，然后给出es映射即可