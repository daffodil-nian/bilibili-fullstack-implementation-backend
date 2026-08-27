# ElasticSearch学习笔记



## 简单介绍

### 总体概述

有一些场景用mysql实现起来性能不好，这时候我们会采用elastic Search弹性搜索出想要的数据。

Elasticsearch 是一个分布式文档的储存中间件，https://learnku.com/docs/elasticsearch73/7.3/data-in-documents-and-indices/6446这篇文章是它的简单介绍。

分布式文档存储的中间件，可以理解成，它可以部署在多台机器上同时运行，以JSON格式的数据存储，并且逻辑上是介于程序和数据库之间的一层，作为辅助组件。

“当文档被储存时，它将建立索引并且近实时（1s）被搜索。 Elasticsearch 使用一种被称为倒排索引的数据结构，该结构支持快速全文搜索。在倒排索引里列出了所有文档中出现的每一个唯一单词并分别标识了每个单词在哪一个文档中。

这句话引申出elasticSearch的一个数据结构---“倒排索引”，而这个数据结构应用场景十分广泛，**市面上常见的搜索引擎底层都使用该数据结构**。  

索引可以被认为是文档的优化集合，每个文档索引都是字段的集合，这些字段是包含了数据的键值对。默认情况下，Elasticsearch 为每个字段中的所有数据建立倒排索引，并且每个索引字段都有专门的优化数据结构。例如：文本字段在倒排索引里，数值和地理字段被储存在 BKD 树中。正是因为通过使用按字段数据结构组合，才使得 Elasticsearch 拥有如此快速的搜索能力。

简单来说，elasticSearch默认为每个字段中的数据建立倒排索引，很方便找数据。

Elasticsearch 具备默认模式的能力，这意味着文档建立索引的时候无需明确指定每个字段的数据类型。当启用动态映射时，Elasticsearch 自动检测并将新字段添加到索引。该默认行为使索引和浏览数据变得容易，只要文档开始建立索引， Elasticsearch 就会检测布尔值，浮点数和整数值，日期和字符串，并将其映射到对应的数据类型中。

但是，最终你应该比程序更加了解自己的数据结构以及如何去使用它们。你可以定义动态映射的规则，并明确的定义 mapping 去更深度的控制字段的存储和索引方式。

这句话讲的是elasticSearch有一个动态识别字段数据类型的能力，可以称之为 **动态映射（dynamic mapping）**

前提是需要我们提前完成映射准备工作！！



在搜索时也会使用在索引期间应用于全文字段的分析链。当您查询全文字段时，对查询文本进行相同的分析，然后再在索引中查找术语。

整个过程大致如下：

1.用户查询文本

2.执行文本分析链（IK分词），把输入切割成 **一个个词条**

3.用上倒排索引找对应的文档ID

4.拿到文档ID后把数据一个个返回。



### 查询和分词

你可以快速使用 Elasticsearch 去存储文档并且能够去检索文档、元数据，是因为它的强大能力是基于 Apache Lucene 搜索引擎库所构建的全套搜索功能。

Elasticsearch 提供了一个简单、连贯的 REST API 用于管理集群、索引和搜索数据。出于在测试的目的，你可以直接通过命令行或者 Kibana 中的开发者控制台中很轻松提交请求。在自己的应用程序中，你可以将 Elasticsearch 客户端用于自己选择的语言：Java, JavaScript, Go, NET, PHP, Perl, Python or Ruby.

由此可得，elasticSearch可以支持多种语言。为了方便学习，之后会采用kibana开发者控制台来执行命令。

#### 搜索数据

Elasticsearch REST API 支持**结构化查询**、全文本查询和结合这两种查询的复杂查询。结构化查询类似于 SQL 中构造的查询类型。例如你可以在员工索引中搜索”gender” 和”age” 字段，然后按照”hire_date” 字段进行排序。全文查询会查找跟查询字符串匹配的所有文档，并返回按相关性排序的文档 - 它们与你的搜索词的匹配程度如何



除了搜索单个字段外，还可以执行**短语搜索**，**相似性搜索**和**前缀搜索**，并返回自动匹配建议。

是否有搜索地理空间或者其他数字类型数据的需求？Elasticsearch 在优化的数据结构中索引非文本数据，这些数据结构支持高性能地理和数字查询。



你可以使用 Elasticsearch 的综合 JSON 查询语言（Query DSL）访问所有这些搜索功能，还可以构造 SQL 样式的查询，以便在 Elasticsearch 内部以本机方式搜索和聚合数据，并且 JDBC 和 ODBC 驱动允许广泛的第三方应用程序通过 SQL 与 Elasticsearch 进行交互。

简单理解为：

短语搜索： 要求词语连续挨在一起。搜短语「骏马奔腾」，只会匹配文本里连续出现 “骏马奔腾” 的文档；如果文档只有分开的骏马、奔腾不会命中。  

相似搜索： 输入「俊马」（错别字），也能匹配到「骏马」相关文档。  （ 不少 AI 相关业务系统会借助 ES 的相似搜索，实现输入容错的检索效果。  ）

 前缀搜索 ： 输入「骏」，匹配开头是骏的词，如骏马、骏图。  

自动匹配建议：输入 “骏”，下拉提示：骏马图、骏马奔腾图。  

#### 分析数据

Elasticsearch 聚合 API 使你能够构建数据的复杂摘要，并深入了解关键指标，模式和趋势。通过聚合不仅可以找到俗话说的 “大海捞针”，还可以让您回答以下问题：

大海捞针能有多少根？

针的平均长度是多少？

按制造商分类的针的中间长度是多少？

在过去的六个月中，大海捞针的数量是多少？

您还可以使用聚合来回答更细微的问题，例如：

您最受欢迎的针头制造商是哪些？

是否有异常或异常的针团？

由于聚合利用了用于搜索的相同数据结构，因此它们也非常快。这使您可以实时分析和可视化数据。您的报告和仪表板会随着数据的更改而更新，因此您可以根据最新信息采取措施。



重要的是，汇总通常跟搜索请求一起运行。您可以在单个请求中同时对相同数据进行文档搜索，过滤结果并执行分析。而且由于聚合是在特定搜索的上下文中计算的，因此您不仅显示了所有 70 针大小的针数，而且还显示了符合用户搜索条件的 70 针大小的针数 - 例如，所有尺寸的 70 个不粘绣针。



1. **大海捞针能有多少根？** 👉昵称带小熊的玩家，**总共有多少人？**
2. **针的平均长度是多少？** 👉这批小熊玩家的**平均家园积分是多少？**
3. **按制造商分类的针的中间长度是多少？** 👉**按性别分组**：分别算出男性小熊玩家、女性小熊玩家的平均等级。
4. **在过去的六个月中，大海捞针的数量是多少？** 👉近 6 个月，每个月新注册的 “小熊” 昵称玩家各有多少人。
5. **您最受欢迎的针头制造商是哪些？** 👉统计这批小熊玩家，各个等级分别多少人，找出人数最多的 3 个等级。
6. **是否有异常的针团？** 👉大部分小熊玩家积分只有几百，找出少数家园积分几十万的异常高积分玩家。

### 可伸缩性：集群、节点、分片

Elasticsearch 的宗旨在于始终可用，并根据你的需求进行扩展。它是一个天然的分布式系统，你可以将服务器（节点）添加到集群以增加容量，Elasticsearch 分配数据和查询会自动负载到所有可用节点，不需要大幅修改你的应用程序，Elasticsearch 知道如何平衡多节点集群以提高扩展性和高可用性。节点越多，性能越好。



Elasticsearch 是如何工作的？实际上，Elasticsearch 索引只是一个或多个物理碎片的逻辑分组，其中每个碎片都是一个独立的索引。通过将索引中的文档分布在多个碎片上，并将这些碎片分布到多个节点上，Elasticsearch 就可以实现冗余功能，这即可以防止硬件故障，又可以在添加节点到集群时，增加查询能力。随着集群的增长（或收缩），Elasticsearch 会自动迁移碎片以重新平衡集群。



就拿游戏《皮卡堂过家家》来解释吧：

场景：皮卡堂有海量玩家，几十万玩家文档，存在 ES 的`player`玩家索引。 如果全部玩家数据只放在**一台机器**上：

1. 这台机器硬盘坏了 → 全部玩家数据丢失。
2. 玩家搜索查询很多，这一台机器扛不住，查询很慢。

#### 1、索引是逻辑概念，分片才是真正存数据的

我们创建逻辑索引 `player`，设置 **3 个主分片**。 ES 会把全部玩家文档打散，分散存放：

- 主分片 0：存一部分玩家（昵称 A‑F 开头）
- 主分片 1：存一部分玩家（昵称 G‑M 开头）
- 主分片 2：存一部分玩家（昵称 N‑Z 开头）

`player`只是一个逻辑名字，它本身不存数据；真正的数据存在 3 个互相独立的物理分片上。 就好比 “皮卡堂玩家总库” 只是个名字，真实玩家数据被拆成 3 堆，分开存放。

#### 2、分片分散到不同节点，副本实现冗余，防止硬件故障

集群一共 3 台服务器：节点 A、节点 B、节点 C。 把 3 个**主分片分别放在不同机器**。 再给每个主分片配**1 个副本分片（备份）**：

- 主分片 0 在节点 A；它的副本 0 放在节点 B
- 主分片 1 在节点 B；它的副本 1 放在节点 C
- 主分片 2 在节点 C；它的副本 2 放在节点 A

👉好处： 假如**节点 A 机器硬盘坏了宕机**： 节点 A 上的主分片 0 没了，但是副本 0 在节点 B 完好无损。ES 自动把副本 0 升级成新的主分片，数据不会丢失，集群继续正常工作。这就是**防止硬件故障的冗余功能**。

#### 3、增加节点，提升查询能力（扩容）

现在玩家越来越多，查询搜索请求爆炸，3 台机器扛不住。 我们新增一台服务器：**节点 D，加入集群**。

副本分片可以对外提供查询。ES 会自动把一部分副本分片迁移到新节点 D。 原本只有 3 台机器分担查询，现在 4 台机器一起处理玩家的搜索请求，**整体查询速度、并发能力提高**。

加机器不用改代码，不用手动迁移数据，ES 自动调度分片。

#### 4、集群自动迁移分片，实现重新平衡

两种情况：

1. **扩容：新增节点 D** → ES 自动把部分分片挪到 D，不让某一台机器数据过多。
2. **缩容：下线一台机器 B** → ES 自动把 B 上面所有分片迁移到 A、C、D，保证每台机器数据量尽量均匀。

不用人工手动复制数据，ES 内部自动完成分片搬迁，维持集群负载均衡。



分片有两种类型：主分片和副本分片。索引中的每个文档都有一个主分片，副本分片是主分片的副本，副本分片可提供数据的冗余副本，以防止硬件故障并增加处理读取请求（如搜索或检索文档）的能力。



创建索引时，索引中主分片的数量是固定的，但副本分片数是随时可更改的，其更改操作不会中断索引或者查询。



讲了这么多概念，接下来该实操了

------

## 安装

我们要安装两个东西，一个是elasticSearch，一个是kibana

安装之前想扩展一个小知识

kibana的端口号是5601，为啥呢？我们把它反过来看看，10gs,像不像logs?

## 启动

由于kibana和elasticSearch都不支持热加载核心配置，所以修改完配置后一定要记得 **重新启动程序！！！**

由于启动kibana太慢了，为了节省时间，我们就用curl命令来学习es的用法

```bash
curl -u elastic:密码 "http://localhost:9200/_cat/health?v"
默认是GET请求方法
只有不确定才加-X POST等
```

## 操作

### 建表

```json
POST /client/_doc/pkt
{
    "title":"皮卡堂过家家",
    "desc":"这是一款女生都爱玩的小游戏",
    "author":"管管"
}
```



### 插入数据

```json
PUT /customer/_doc/1
{
  "name": "John Doe"
}
```

elastic 底层干的就是一件事，把 JSON 文档存储在索引中，比如上述例子，在 /book 这个索引的 /_doc 这个固定类型关键字，文档中编号为 / 1 的这里存储数据如上所示，有点像把一张纸放在某本书的某一页的感觉。

### 根据ID查询数据信息

```markdown
GET /customer/_doc/1
```

可以得到下面结果

```json
{
  "_index":"customer",
  "_id":"1",
  "_version":1,
  "_seq_no":0,
  "_primary_term":1,
  "found":true,
  "_source":{"name":"John Doe"}
}

```

其中，found表示是否找到，source表示源数据是什么

### 批量插入数据

> 如果有大量的文档需要插入，你可以使用 BULK API 批量插入数据。使用 Bulk API 要比单独请求提交要快，是因为它最大限度地减少了网络传输次数。
>
> 最佳批量插入大小取决于许多因素：文档大小和复杂性、索引、搜索负载以及集群的可用资源。每次批量处理最好在 1000 ~ 5000 个文档，文档大小最好是在 5M ~ 15M 之间。
>
> 将数据批量导入 Elasticsearch，你就可以开始搜索和分析了。
> 下载 acounts.jsondemo 数据。
> 数据 git 地址 github.com/elastic/elasticsearch/b...
> 随机生成的 demo 数据展示了以下的用户信息：

```json
{
    "account_number": 0,
    "balance": 16623,
    "firstname": "Bradshaw",
    "lastname": "Mckenzie",
    "age": 29,
    "gender": "F",
    "address": "244 Columbus Place",
    "employer": "Euron",
    "email": "bradshawmckenzie@euron.com",
    "city": "Hobucken",
    "state": "CO"
}
```

```markdown
{"index":{"_id":"101"}}
{"name":"张三"}
{"index":{"_id":"102"}}
{"name":"李四"}
```

### 删除

删除普通索引

```
DELETE customer
```



### 开始搜索

> 一旦你开始在 Elasticsearch 插入数据，你就可以通过_search 方式发送请求来进行搜索，如果使用匹配搜索功能，在请求体中使用 Elasticsearch Query DSL 指定搜索条件。你也可以在请求头指定要搜索的索引名称。
>
> 如下图所示：搜索银行索引中，所有账号按照 account_number 排序:
>

```json
GET /bank/_search
{
  "query": { "match_all": {} },
  "sort": [
    { "account_number": "asc" }
  ]
}
```

默认情况下，会返回符合条件搜索的前 10 个文档

如果想分页查询可以这么写：

```markdown
GET /bank/_search
{
  "query": { "match_all": {} },
  "sort": [
    { "account_number": "asc" }
  ],
  "from": 10,
  "size": 10
}
```

想搜索特定的字段，可以使用匹配查询。如下所示，请求搜索地址字段以查询地址中包含 mill 或 lane 的客户：

```markdown
GET /bank/_search
{
  "query": { "match": { "address": "mill lane" } }
}
```

如果要全部匹配，就要用上match_phrase了，毕竟是一个短语，不可以分开的，如下所示

```markdown
GET /bank/_search
{
  "query": { "match_phrase": { "address": "mill lane" } }
}
```

match和match_phrase的区别就好像是or和and的区别，match_phrase是要包含上述的所有词汇，而match只要匹配到就ok了的。



match适合用于**模糊搜索、找人记不全名字，宽泛的用户检索**。

而match_phrase不仅要同时包含 `mill` 和 `lane`，**词语的顺序、连续位置也必须一致**。



> 如果要构造更复杂的查询，可以使用布尔查询来组合多个查询条件，must match、should match、must not match
>
> 如图所示：请求在银行索引里搜索年龄是 40 的，但不居住在爱达荷州 (ID) 的客户
>

```
GET /bank/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "age": "40" } }
      ],
      "must_not": [
        { "match": { "state": "ID" } }
      ]
    }
  }
}
```



> 布尔查询中每个 must，should,must_not 都被称为查询子句。每个
> must 或者 should 查询子句中的条件都会影响文档的相关得分。得分越高，文档跟搜索条件匹配得越好。默认情况下，Elasticsearch 返回的文档会根据相关性算分倒序排列。
>
> must_not 子句中认为是过滤条件。它会过滤返回结果，但不会影响文档的相关性算分，你还可以明确指定任意过滤条件去筛选结构化数据文档。
>
> 如图所示：请求搜索余额在 20000 ~ 30000（包括 30000） 之间的账户
>

```
GET /bank/_search
{
  "query": {
    "bool": {
      "must": { "match_all": {} },
      "filter": {
        "range": {
          "balance": {
            "gte": 20000,
            "lte": 30000
          }
        }
      }
    }
  }
}
```

### 聚合分析

> Elasticsearch 聚合让你看到一些有关搜索结果元信息，返回结果能显示例如：“德克萨斯州有多少个开户的人？” 或者 “田纳西州的平均帐户余额是多少？”。你可以搜索文档，过滤 hits，使用聚合去分析并返回结果。

#### 例子 1：统计每个 VIP 等级有多少玩家（分组计数）

需求：想看每个 VIP 等级分别有多少玩家，**不要返回一个个玩家详情，只要统计数字**。

```markdown
GET /pet_room/_search
{
  "size": 0,
  "aggs": {
    "group_by_vip": {
      "terms": {
        "field": "vip_level"
      }
    }
  }
}
```

aggs意思是开启聚合，terms相当于MySQL中的group by

返回结果如下

```markdown
"aggregations" : {
  "group_by_vip" : {
    "buckets" : [
      { "key" : 0, "doc_count" : 520 },
      { "key" : 1, "doc_count" : 210 },
      { "key" : 2, "doc_count" : 95 }
    ]
  }
}
```



其他聚合类型等用到再在线查看。



补充点：

1. **DSL**：Domain‑Specific Language，**领域特定语言**

> Elasticsearch DSL，专指 ES 的 JSON 查询语法，我们前面学的`match`、`match_phrase`、`aggs`聚合都属于 ES‑DSL。

1. **EQL**：Event Query Language，**事件查询语言**

> ES 专门面向日志、事件数据的查询语言，适合事件序列检索。

1. **SQL**：Structured Query Language，**结构化查询语言**

> Elasticsearch SQL，ES 内置，可以直接写 SQL 语句查询 ES 索引，不用写 JSON‑DSL。

总结一下，上述为DSL，EQL，SQL用法，学完之后可以开始实践了。

## 补充：Elastic Stack全家桶尚未学习的内容

### 分词器

前面提到IK分词，这里稍微补充讲解。

https://www.elastic.co/guide/en/elasticsearch/reference/7.17/analysis-analyzers.html这篇文章讲的是内置分词器。

我们来一个一个讲解

#### 标准分词器

```
POST _analyze
{
  "analyzer": "standard",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```

分词结果如下：

```
[ the, 2, quick, brown, foxes, jumped, over, the, lazy, dog's, bone ]
```

发现没，Standard标准分词器以空白格，标点作为分割边界。



标准分词器支持以下几个参数配置：max_token_length,stopwords和stopwords_path,其中，max_token_length默认长度是255，

stopwords和另一个用法区别在哪？可以看看下面这张表格

| 参数           | 使用方式                       | 适合场景                       |
| -------------- | ------------------------------ | ------------------------------ |
| stopwords      | JSON 内直接写数组`["a","the"]` | 停用词少，十几个以内，简单测试 |
| stopwords_path | 读取 config 下外部 txt 文件    | 停用词量大，成百上千条         |





以下面例子讲解

```json
PUT my-index-000001
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_english_analyzer": {
          "type": "standard",
          "max_token_length": 5,
          "stopwords": "_english_"
        }
      }
    }
  }
}

POST my-index-000001/_analyze
{
  "analyzer": "my_english_analyzer",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```

```
[ 2, quick, brown, foxes, jumpe, d, over, lazy, dog's, bone ]
```

发现没？这个stopwords实际上就是过滤掉没有实际意义的词汇

#### 简单分词器

```
POST _analyze
{
  "analyzer": "simple",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```

看看原文是怎么解释的吧~

> The `simple` analyzer breaks text into tokens at any non-letter character, such as numbers, spaces, hyphens and apostrophes, discards non-letter characters, and changes uppercase to lowercase.
>
> 

#### 空格分词器

```
POST _analyze
{
  "analyzer": "whitespace",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```

空格分词器顾名思义，根据空格划分词汇



#### 停止分析器



```
POST _analyze
{
  "analyzer": "stop",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```



1. 按**非字母（空格、数字、标点）分割文本**；
2. 全部转小写；
3. **直接使用 `_english_` 英文停用词表，过滤掉英文停用词（the、over、a、is）**。

> 默认行为：`stop`分词器内置开启英文停用词，不需要你手动写 stopwords 参数。

##### 上面这段文本输出的 token

原始：`The 2 QUICK Brown‑Foxes jumped over the lazy dog's bone.` 经过 stop 分词器处理：

- 删掉停用词：`the`、`over`
- 转小写
- 按标点空格切分

输出词条： `quick`、`brown`、`foxes`、`jumped`、`lazy`、`dog`、`s`、`bone`

#### 关键词分词器

```
POST _analyze
{
  "analyzer": "keyword",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```

> The `keyword` analyzer is a “noop” analyzer which returns the entire input string as a single token.

也就相当于输入啥就输出啥，整句话当作专业术语

```
[ The 2 QUICK Brown-Foxes jumped over the lazy dog's bone. ]
```

#### Pattern分词器

```
POST _analyze
{
  "analyzer": "pattern",
  "text": "The 2 QUICK Brown-Foxes jumped over the lazy dog's bone."
}
```

##### pattern 分词器厉害之处：可以自定义正则分割规则

内置直接用 `"analyzer":"pattern"` 用的是默认`\W+`。 我们可以自己创建索引，改写正则，想按什么字符切就按什么切。

示例：**按横杠 `-` 分割字符串**

```
PUT test-pattern
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_pattern_analyzer": {
          "type": "pattern",
          "pattern": "-",   //遇到横杠就切分
          "lowercase": true
        }
      }
    }
  }
}

POST test-pattern/_analyze
{
  "analyzer":"my_pattern_analyzer",
  "text":"apple-banana-orange"
}
```

输出：`apple`、`banana`、`orange`

官方文档还有剩余两种分词器就不解释了。

### IK分词器

这其实是一个插件

项目链接：https://github.com/infinilabs/analysis-ik

### 打分机制

比如有几个内容：

1.小张不要内耗

2.内耗星球

3.黑子不要再内内内内内耗了



用户搜索后根据关键词匹配程度按照对应排序结果返回。

