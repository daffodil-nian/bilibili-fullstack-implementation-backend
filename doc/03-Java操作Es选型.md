# 查询语句



```java
Query query = NativeQuery.builder()
	.withAggregation("lastNames", Aggregation.of(a -> a
		.terms(ta -> ta.field("lastName").size(10))))
	.withQuery(q -> q
		.match(m -> m
			.field("firstName")
			.query(firstName)
		)
	)
	.withPageable(pageable)
	.build();

SearchHits<Person> searchHits = operations.search(query, Person.class);
```

可以参考这个来自定义查询语句



# 返回类型





返回值类型可以为SearchHits，传入文档的类型即可

```java
interface BookRepository extends ElasticsearchRepository<Book, String> {
    @SearchTemplateQuery(id = "book-by-title")
    SearchHits<Book> findByTitle(String title);
}
```

