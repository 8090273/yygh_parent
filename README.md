# 医患通 预约挂号系统
是一个分布式微服务架构的系统
=======
# 简介
这里是医患通项目的后端工程
# 开始搭建
## 新建父工程
点击新建工程 SpringBoot 更名
## 修改父工程依赖
Spring Boot的版本为2.2.1
## 建立子模块
model：放置所有实体类
## 照着文档复制
其实应该敲一步写一步笔记
# 管理实体类
## model模块
所有实体类都放入model模块中
### model.hosp.HosplitalSet
医院相关设置实体类  
@ApiModelProperty()用于方法，字段； 表示对model属性的说明或者数据操作更改  
### model.base.BaseEntity
放置通用的属性
# 编写DAO层
## 创建Mapper文件
在service_hosp模块下新建接口：mapper.HospitalSetMapper  
继承`BaseMapper<HospitalSet>`  
## 编写Service层
新建Service继承`IService<HospitalSet>`接口  
新建impl实现类 继承`ServiceImpl<HospitalSetMapper, HospitalSet>`实现`HospitalSetService`
# 编写controller层
新建controller.HospitalSetController,标注注解`@RestController`来使Spring接管控制层,并且返回JSON数据  
标注`@RequestMapping("admin/hosp/hospitalSet")`注册接口地址  
注入Service层
## 编写业务方法
### 查询医院设置表中的所有信息
标注`@GetMapping("findAll")`声明接口详细地址，调用service层的`.list()`方法，不传入参数则为查询所有信息，传参为条件查询  
### 删除医院设置表中某条信息
removeHospSet()方法,删除一般都为逻辑删除  
逻辑删除的实现在modle模块中的BaseEntity通用实体类的`isDeleted`属性上标注`@TableLogic`
使用`@DeleteMapping("{id}")`注解，因为使用了大括号，所以使用`@PathVariable`接收值

# Swagger2
Swagger2是一个api接口文档，可以帮我们快速编写新的API接口文档  
因为Swagger在很多接口都需要使用，所以提取到了common模块
## 创建swagger配置
直接把文件复制到common模块的service_util模块的common.config.Swagger2Config中  
`webApiConfig()`方法用于声明组**webApi**，路径为 `/api/.*`  
`adminApiConfig()`方法配置组**adminApi**,路径为`/admin/.*`  
记得把common.service_util包在service.pom中引入  
启动类上标注`@ComponentScan(basePackages = "com.teen")`
## 具体配置
可以在controller层上添加注解`@Api(tags="医院设置管理")`  
可以在具体方法上加上`@ApiOperation(value="获取医院设置所有信息")`
## 访问接口并测试
启动项目后访问接口  
http://localhost:8201/swagger-ui.html
# 接口返回结构集统一
在common模块下的common_util中新建common.result包并复制粘贴上代码  
## ResultCodeEnum
ResultCodeEnum类为枚举类，里边枚举了各种状态信息
## Result
Result为返回结果集  
其中包含3个属性：返回状态码、返回消息、返回数据  
`Result.ok()`方法为成功  
`Result.fail()`方法为失败
## 改造controller
使用返回结果集来统一返回结果
记得在service里加common_uitl的pom依赖
# 条件查询带分页
## 创建VO类，封装条件值
VO类：HospitalSetQueryVo
## 编写controller获取条件对象、分页数据
mp的Page对象传入当前页码和每页条数可以实现快捷分页查找
### 构造查询条件器
mp的QueryWapper可以构造简单查询条件
## 修改前后端交互
前端给后端不直接传对象，而是一个JSON字符串，所以要加上`@RequestBody(required=false)`注解  
此注解默认接收JSON并解析，参数`required=false`表示可以为空  
修改请求方式为POST请求
# 添加医院设置
添加的医院需要后端手动设置两个属性：密钥和状态
## 设置密钥
密钥使用MD5加密  系统时间+随机数，MD5使用Spring自带的方法：  
```
//使用随机数生成器
Random random = new Random();
//密钥内容为系统时间+随机数 
 hospitalSet.setSignKey(DigestUtils.md5DigestAsHex((System.currentTimeMillis()+random.nextInt(1000)+"").getBytes(StandardCharsets.UTF_8)));
```
## 设置状态
1为可用 0为不可用
## swagger测试数据
```json
{
  "apiUrl": "http://localhost:9999",
  "contactsName": "张三",
  "contactsPhone": "18522459954",
  "hoscode": "1000_02",
  "hosname": "北京人民医院",
  "id": 8
}
```
`@RequestBody`需要和POST一起用

# 批量删除
弹幕：集合没有实现序列化，在分布式环境下会接收不了数据  
实际：实现了序列化接口，但是使用了关键字`transient`修饰`elementData`属性，每次序列化时，先调用 `defaultWriteObject()` 方法序列化 ArrayList 中的**非 transient 元素**，然后**遍历 elementData**，**只序列化已存入的元素，这样既加快了序列化的速度，又减小了序列化之后的文件大小**。  
## 注意事项
使用`@DeleteMapping`注解进行删除操作  
传入List集合

# 医院锁定和解锁接口
只有医院的状态为解锁状态，我们才能进行和医院系统的对接，实现数据的操作；如果把医院的状态锁定，则不能进行对接  
## 编码
使用`@PutMapping()`注解完成RestFull风格编写  


# 发送签名密钥
我们通过医院的接口和我们平台进行对接，双方要约定好一个密钥。   
在添加医院的接口中我们手动设置了每一个医院的密钥，我们要把密钥发送给医院的接口，让它对其进行配置，完成一个相互的对接，所以我们要进行一个发送
## 编码
根据id查到实体类  
得到实体类中的医院编号和签名密钥   
后期再写发送短信的业务

### 发送短信

待定

# 全局异常处理

要把所有异常做一个统一的处理，而不是返回“500”  

因为异常处理全局都能用到，所以提取到common模块中

## 创建异常类

在common模块的common_util中创建`exception.GlobalExceptionHandler`类，并标注注解`@ControllerAdvice`

### @ControllerAdvice

本质上是一个Component，因此也会被当成组件扫描   

此注解其实是一个增强的Controller，使用这个Controller，可实现三个方面的功能，因为是SpringMVC提供的功能，所以可用在SpringBoot中

- 全局异常处理(`@ExceptionHandler`)：用来指明
- 全局数据绑定(`@InitBinder`)
- 全局数据预处理（`@ModelAttribute`）

`@ExceptionHandler({Exception.class})`将所有异常都接收

## 定义自定义异常类

直接粘贴代码  

`yyghExceptionn`继承了RuntimeException，其中有一个属性：状态码   

有两个构造方法，可用传入（信息、状态码）或（异常枚举类）

# 日志

## 设置日志级别

在`application.properties`文件中添加

```properties
logging.level.root=debug
```

此级别可看到更多的信息

## Logback日志

springBoot内部使用Logback作为日志实现的框架

### 配置日志

在resources下新建logback-spring.xml  

编写相关代码（复制粘贴）

# 配置分页插件

## 添加Bean

在service模块的service_hosp模块下，config.HospConfig类中添加  

```java
@Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
```

# 数据字典模块

因为有很多数据不会被更改（如省、市区），所以可以抽取出一个数据字典。数据字典

## 新建模块

新建Service下的service_dict文件  

照常创建config、service、mapper、controller并复制粘贴service_hosp模块即可

## 查询id下的子数据

id = parent_id  

通过parent_id可以获得层级关系，进而获得层级列表。  

```java
QueryWrapper wrapper = new QueryWrapper();
wrapper.eq("parent_id",id);
List<Dict> list = baseMapper.selectList(wrapper);
```

同时还要判断子节点下是否有子节点，因为hasChildren属性并不在数据库表中。

# EasyExcel表格框架

通过Excel表格加载到数据库中

## 引入依赖

```xml
<dependencies>
    <!-- https://mvnrepository.com/artifact/com.alibaba/easyexcel -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>easyexcel</artifactId>
        <version>2.1.1</version>
    </dependency>
</dependencies>
```

## 读写数据字典

使用`EasyExcel.write().sheet().doWrite()`来进行写表格操作  

## 导出数据库中的数据字典到excel表格中

因为要向响应中添加数据，所以controller中接收`HttpServletResponse`参数。  

设置下载信息

```java
//设置下载信息
response.setContentType("application/vnd.ms-excel");
response.setCharacterEncoding("utf-8");
String fileName = URLEncoder.encode("数据字典","utf-8");
```

设置响应头，以下载的方式打开

```java
//以下载方式打开
response.setHeader("Content-disposition",
                   "attachment;filename="+fileName+".xlsx");
```

对数据库中查询出的dict对象进行封装，封装为Vo对象`DictEeVos`  

可以使用工具类进行深拷贝

```java
//使用工具类进行深拷贝
BeanUtils.copyProperties(dict,dictEeVo);
```

最后进行表格的写操作

```java
//进行表格写操作
EasyExcel.write(response.getOutputStream(),DictEeVo.class)
    .sheet("数据字典")
    .doWrite(dictEeVos);
```

注意：**controller层使用void返回，不然会报错**

## 将表格数据导入数据字典

### controller层

传参`MultipartFile file`以便接收前端传来的文件  

### 配置监听器

想要将表格写入数据库中，需要配置一个配置类来监听表格  

新建`listener.DictListener`类，继承`AnalysisEventListener<DictEeVo>`，实现抽象方法

```java
//一行一行的读取，从第二行开始
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dictMapper.insert(dict);

    }
```

因为要进行增删改查，所以要使用到DictMapper，这里使用有参构造注入依赖

```java
//因为要进行增删改查，所以需要依赖注入mapper
private DictMapper dictMapper;
//使用有参构造注入依赖
public DictListener(DictMapper dictMapper) {
    this.dictMapper = dictMapper;
}
```

### service层

直接调用`EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(dictMapper)).sheet().doRead();`方法即可，传参 *输入流*、*实体类*、*监听器*  

### 实体类注意事项

其他实体类都继承有`BaseEntity`，这样会导致主键自增，我们想自定义主键，必须解除继承，自行编译。

# 缓存！！！

缓存是为了提供查询速度  

适合做缓存的数据：不经常修改的数据，固定的数据，经常做查询的数据

## Spring Cache+Redis缓存数据

`@Transactional`注解事务的注解Cache支持，且提供了Cache抽象，方便切换各种底层Cache（如**redis**）

### 使用Spring Cache的好处

1. 提供基本的Cache抽象。方便切换各种底层Cache
2. 通过注解Cache可以实现类似于事务的操作，缓存逻辑透明的应用到我们的业务代码上，且需要更少的代码就可以完成
3. 提供事务的回滚和字典回滚缓存
4. 支持比较复杂的缓存逻辑

## 项目集成SpringCache

因为很多模块都用到缓存，所以将模块提取到common模块中

### 添加依赖

在service_util模块中添加

### 添加配置类

添加`config.RedisConfig`类，标注`@Configuration`,` @EnableCaching`,`@EnableCaching`表示开启缓存  

编写固定配置：

```java
@Configuration
@EnableCaching
public class RedisConfig {
    /**
     * 自定义key规则
     * 可以帮助生成唯一的key
     * @return
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                //类名
                sb.append(target.getClass().getName());
                //方法名
                sb.append(method.getName());
                for (Object obj : params) {
                    //各个对象
                    sb.append(obj.toString());
                }
                //类名+方法名+对象
                return sb.toString();
            }
        };
    }

    /**
     * 设置RedisTemplate规则
     * 从Redis中存取内容需要用到RedisTemplate
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //新建RedisTemplate
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置Jackson的Redis序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        //序列号key value
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 设置CacheManager缓存规则
     * @param factory
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 配置序列化（解决乱码的问题）,过期时间600秒
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
        return cacheManager;
    }
}
```

## 重要注解

### @Cacehable

当第一次查询数据库后，将数据添加到缓存中，第二次查询数据库时可以直接走缓存，不必再走数据库

### @CachePut

使用该注解标注的方法，每次都会执行，并将结构存入指定缓存中。其他方法可以直接从响应的缓存中国读取缓存数据，而不需要再去查询数据库。一般用在新增方法上

### @CacheEvict

清空指定缓存。一般用在更新或删除操作上

## 开启redis

url:  192.168.118.144:6379  

root:  /usr/local/redis  

密码：root  

cli登录： auth root

## 使用

在查询方法上标注`@Cacheable`  

```java
@Cacheable(value = "dict",keyGenerator="keyGenerator")
public List<Dict> getChildData(Long id)
```



在更新方法上标注`@CacheEvit()`

```java
@CacheEvict(value = "dict",allEntries = true) //每次更新都会清空缓存
public void importDict(MultipartFile file)
```

# 配置nginx

## 修改配置文件

监听9001端口，并匹配路径，路径中有`/hosp`则转发到`192.168.118.1:8201`中，路径中有`/dict`则转发到`192.168.118.1:8202`中

## 启动nginx

路径：/usr/local/nginx/sbin

命令:`./nginx`

## 修改前端配置文件

修改前端的.env.development文件，改为`http://192.168.118.147:9001`

# MongoDB

MongoDB是一种NoSQL数据库，存储结构类似于JSON对象

为何使用NooSQL：

1. 对数据库的高并发读写
2. 对海量数据的高效率存储和访问
3. 对数据库的高可扩展性和高可用性

缺点：

1. 数据库事务一致性需求
2. 数据库的写实时性和读实时性需求
3. 对复杂的sql查询，特别是多表关联查询的需求

## 安装mongoDB

默认端口27017

### 拉取docker镜像

`docker pull mongo:latest`

### 启动容器

```
docker run -d --restart=always -p 27017:27017 --name mymongo -v /mydata/mongoDB:/data/db -d mongo
```

### 进入容器

```
docker exec -it mymongo bin/bash 
```

然后开启客户端 `mongo`

## 适用场景

1. 网站数据：Mongo非常适合实时的插入，更新与查询，并具备网站实时数据存储所需的复制及高度伸缩性。

2. 缓存：由于性能很高，Mongo也适合作为信息基础设施的缓存层。在系统重启之后，由M ongo搭建的持久化缓存层可以避免下层的数据源过载。

3. 大尺寸，低价值的数据：使用传统的关系型数据库存储一些数据时可能会比较昂贵， 在此之前，很多时候程序员往往会选择传统的文件进行存储。

4. 高伸缩性的场景：Mongo非常适合由数十或数百台服务器组成的数据库。Mongo的路线图中已经包含对Map Reduce弓摩的内置支持。

5. 用于对象及 JSON数据的存储：Mongo的BSON数据格式非常适合文档化格式的存储 及查询。

## 不适用场景

1. 高度事务性的系统：例如银行或会计系统。传统的关系型数据库目前还是更适用于需要大量原子性复杂事务的应用程序。

2. 传统的商业智能应用：针对特定问题的BI数据库会对产生高度优化的查询方式。对于此类应用，数据仓库可能是更合适的选择。

## mongoDB学习

1. `collection`数据库表/集合（table）
2. `document`数据记录行/文档(row)
3. `field`数据字典/域(column)
4. **不支持表连接！！**
5. 自动将`_id`设为主键

### 基本命令

`use test`：如果存在，则切换，不存在则创建

`show dbs;` :查询所有数据库

`db.dropDatabase();`删除当前使用数据库

`db.getName();`：查看当前使用的数据库

`db.stats();`:显示当前db状态

# 整合医院系统

医院系统主要用于模拟接口对接  

直接在父工程下新建springBoot工程，然后复制粘贴即可  

## 为何要整合？

此项目为一个预约挂号平台，其中有 数据显示、挂号、预约等相关业务，这些功能需要一些信息，如：医院信息、科室信息、排班信息等。  

这些信息是由各大医院进行添加的，并非我们第三方平台提供的，所以需要对每个医院的平台进行接口连接。  

连接成功后，医院方可通过平台的接口上传科室信息、上传排班信息等

# 开发对接医院接口

## 整合mongoDB

### 添加依赖

在service_hosp中添加依赖

```xml
<dependencies>
<!--        spring Boot整合mongoDB-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency
</dependencies>
```

### 添加repository

新建`repository.HospitalRepository`接口，继承`MongoRepository<Hospital,String> `，并添加注解`@Repository`

## 添加service层

新建`service.HospitalService`，实现类中注入依赖

```java
@Service
public class HospitalServiceImpl implements HospitalService {
    //因为service要调用mongoDB，所以注入依赖
    @Autowired
    private HospitalRepository hospitalRepository;
}
```

## controller层

因为此接口要对外进行调用，所以建在api下

新建`api.ApiController`   

调用接口`/api/hosp`

# 上传医院接口

## 接口地址

`@PostMapping("saveHospital")`

## controller层

使用`HttpServletRequest`接收JSON传参  

通过`request.getParameterMap()`获得map类型的医院信息。此时医院信息map中的value为String[]字符串数组，需要转为Object的value的Map  

```java
//获取传过来的医院信息
Map<String, String[]> requestMap = request.getParameterMap();
//将map的value类型从字符串数组改为object，使用自行封装的工具类
Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
```

将map传给service层做具体业务逻辑处理

## service层

将传来的map封装为实体类，以便更好地适配数据库进行存取。

```java
//把形参转换为实体类
//把map转为JSON字符串，再将JSON字符串转为实体类
String mapJSON = JSONObject.toJSONString(paramMap);
Hospital hospital = JSONObject.parseObject(mapJSON, Hospital.class);
```

再根据hoscode查询是否有数据，没有则添加，有则修改

```java
//根据医院code判断数据库中是否有值
String hoscode = hospital.getHoscode();
Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);


if(hospitalExist == null){
    //没有则添加
    hospital.setStatus(0);
    hospital.setCreateTime(new Date());
    hospital.setUpdateTime(new Date());
    hospital.setIsDeleted(0);
    hospitalRepository.save(hospital);

}else {
    //有则修改
    hospital.setStatus(hospitalExist.getStatus());
    hospital.setCreateTime(hospitalExist.getCreateTime());
    hospital.setUpdateTime(new Date());
    hospital.setIsDeleted(0);
    //修改完成
    hospitalRepository.save(hospital);
}
```



## repository层

因为Spring Data提供了支持，我们只需要按照Spring Data规范写方法名即可，无需写方法的具体实现。  

查询方法以`get|find|read`开头都可以

# 签名校验

为了保证接口的安全性，对调用者的签名进行校验，校验失败无法进行调用

## 获取医院方签名

在controller中判断hoscode是否存在，不存在则抛出异常`throw new YyghException(ResultCodeEnum.PARAM_ERROR);`  

因为医院方传输的base64格式中的`+`被自动解析成了字符串连接符，变为了`" "`，导致md5验证失败，所以在校验前务必转换格式，将所有的`" "`转换回`+`  

```java
//血妈坑壁！！！！艹！
//传输过程中“+”转换为了“ ”，因此我们要转换回来
String logoDataString = (String)paramMap.get("logoData");
if(!StringUtils.isEmpty(logoDataString)) {
    String logoData = logoDataString.replaceAll(" ", "+");
    paramMap.put("logoData", logoData);
}
```

将本地的签名取出，加密后与医院方的签名进行匹配  

```java
//将本地签名取出
String hoscode = (String) paramMap.get("hoscode");
String hospSetSign = hospitalSetService.getSignKey(hoscode);

//使用封装好的工具类来校验
if (!HttpRequestHelper.isSignEquals(paramMap,hospSetSign)){
    throw new YyghException(ResultCodeEnum.SIGN_ERROR);
}
```

加密算法：将paramMap中的所有元素进行拼接，并拼接上本地Sign，进行MD5加密

```java
if(paramMap.containsKey("sign")) {
    paramMap.remove("sign");
}
//TreeMap保证有序
TreeMap<String, Object> sorted = new TreeMap<>(paramMap);
StringBuilder str = new StringBuilder();
for (Map.Entry<String, Object> param : sorted.entrySet()) {
    str.append(param.getValue().toString()).append("|");
}
str.append(signKey);
//明文
String plaintext = str.toString();
log.info("加密前：" + plaintext);
//密文
String md5Str = MD5.encrypt(plaintext);
log.info("加密后：" + md5Str);
return md5Str;
```

医院方为了保证每次发送的密文不同，在paramMap中加入了时间戳

# 查询医院接口

## controller层

接口：`hospital/show`  ,POST方式，需要使用`HttpServletRequest`类来接收请求

## 封装校验函数

因为校验函数频繁使用，所以封装一下，可以封装为工具类，我选择封装到service层业务逻辑中（别学我）

```java
public void verificationSign(Map<String, Object> paramMap) {
    //验证hoscode是否存在
    if(StringUtils.isEmpty(paramMap.get("hoscode"))){
        throw new YyghException(ResultCodeEnum.PARAM_ERROR);
    }


    //将本地签名取出
    String hoscode = (String) paramMap.get("hoscode");
    String hospSetSign = getSignKey(hoscode);

    //使用封装好的工具类来校验
    if (!HttpRequestHelper.isSignEquals(paramMap,hospSetSign)){
        throw new YyghException(ResultCodeEnum.SIGN_ERROR);
    }
}
```



# 使用Repository对MongoDB进行分页查询

## 方法传入参数

- page：当前页码
- limit：每页记录数
- userQueryVo：当前实体类的QueryVo对象

## 方法返回值

org.springframework.data.domain.page对象，是一个分页结果对象，包含记录数、页码、查出的记录等待分页查询结果

## 常用方法及对象

- Pageable：分页对象，传给`repository.findAll()`中。
- `PageRequest.of(page,limit,sort)`：返回Pageable分页对象。传入参数：当前页码、每页记录数、sort排序对象
- Sort：排序对象，传给`PageRequest.of()`方法设置排序规则
- `Sort.by(Sort.Direction.DESC,"createTime")`：返回排序对象，传入参数：排序规则、依据字段字符串
- ExampleMatcher：条件匹配器，用于设置限制条件，如模糊搜索、忽视大小写。传给`Example.of()`方法
- Example\<User\> ：条件匹配器实例，传给`userRepository.findAll()`方法
- `Example.of(user,matcher)`：返回Example\<User\>对象，传入参数：实体类对象、matcher条件匹配器。
- `userRepository.finAll(example,pageable)`：返回Page\<User\>分页结果对象。传入参数：example条件匹配器实例对象，pageable分页对象

## 实例代码：分页查询科室信息

```java
public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
    //查出的结果以创建时间排序
    Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
    //创建分页对象,参数为当前页码，每页记录数，排序规则
    Pageable pageable = PageRequest.of(page,limit,sort);
    //条件匹配器实例需要实体类型的对象，所以new一个
    Schedule schedule = new Schedule();
    //为实体类型的对象赋值，使用工具类直接copy,将前者的值copy给后者
    BeanUtils.copyProperties(scheduleQueryVo,schedule);
    //不要忘了设置状态和逻辑删除标志
    schedule.setIsDeleted(0); //0为未删除
    schedule.setStatus(1);  //1为可用

    //创建条件匹配器，设置限制条件
    ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)  //改变默认字符串匹配规则：模糊搜索
        .withIgnoreCase(true);  //忽视大小写
    //Example.of()传入实体类型schedule和条件匹配器(ExampleMather)，返回条件匹配器实例
    Example<Schedule> example = Example.of(schedule, matcher);
    //使用findAll方法分页查询，传入条件匹配器实例Example和分页对象
    Page<Schedule> pages = scheduleRepository.findAll(example,pageable);
    return pages;
}
```

# 接入微服务！！！

# 医院管理模块

service_hosp模块要调用service_dict模块，属于微服务之间的调用（远程调用）

# 接入Nacos

启动nacos（windows本机8848端口）  

## 注入依赖

```xml
<!-- 服务注册 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

## 主启动类上标注注解

在dict模块和hosp模块的主启动类上标注`@EnableDiscoveryClient`开启服务发现

## 配置文件

配置文件中添加nacos设置  

```properties
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
```

# 使用Feign服务调用

## 新建模块

新建一个service_client,并在此模块下新建一个新模块service_dict_client

## 添加依赖

在service_client模块中添加依赖  

```xml
<dependency>
    <groupId>com.teen</groupId>
    <artifactId>common_util</artifactId>
    <version>1.0</version>
</dependency>

<dependency>
    <groupId>com.teen</groupId>
    <artifactId>model</artifactId>
    <version>1.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <scope>provided</scope>
</dependency>
```

## 创建调用接口

在service_dict_client模块下创建`com.teen.yygh.dict.client.DictFeignClient`接口  

接口上标注`@FeignClient("service-dict")`来开启对微服务的调用  

将接口声明出来，记得路径补全

```java
@FeignClient("service-dict")
public interface DictFeignClient {

    /**
     * 调用dict端的接口，根据dictCode和value查询数据字典名称
     * 注意：@PathVariable中必须指明参数
     * @param dictCode
     * @param value
     * @return
     */
    @GetMapping("/admin/dict/getDickName/{dictCode}/{value}")
    public String getDickName(@PathVariable("dictCode") String dictCode,
                              @PathVariable("value") String value);

    @GetMapping("/admin/dict/getDickName/{value}")
    public String getDictName(@PathVariable("value") String value);
}
```

## 在调用端引入Client

service-hosp要调用service-dict，所以需要在service_hosp模块中引入service_dict_clietn模块  

在service_hosp的pom文件中

```xml
<dependency>
    <groupId>com.teen</groupId>
    <artifactId>service_dict_client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

因为要使用注解，所以添加依赖  

```xml
<!-- 服务调用feign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## 整合使用openFeign

在service_hosp模块的主启动类上添加注解`@EnableFeignClients(basePackages = "com.teen")`来开启远程服务调用  

在service_hosp模块下的`service.Impl.HospitalServiceImpl`中注入依赖  

```java
@Autowired
private DictFeignClient dictFeignClient;
```

为了让springBoot可管理依赖，在DictFeignClient中标注`@Repository`或`@Service`

### 完善分页查询医院业务（使用SpringData分页功能）

#### controller层

```java
@ApiOperation(value = "分页查询医院信息")
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page,
                           @PathVariable Integer limit,
                           HospitalQueryVo hospitalQueryVo){
        //条件查询带分页的查询都应经过QueryVo类进行封装

        //因为医院数据都存在mongodb中
        Page<Hospital> pageModel = hospitalService.selectHospPage(page,limit,hospitalQueryVo);

        return Result.ok(pageModel);

    }
```

#### service层实现类

通过SpringData的分页功能实现

```java
@Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //凭自己感觉写写试试？ 写出来辣~

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //设置排序规则
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //核心 分页对象
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example example = Example.of(hospital,matcher);

        //使用Repository查询出mongodb中的数据
        Page<Hospital> pages = hospitalRepository.findAll(example,pageable);
        //从pages中取出对象  取出的是mongodb中所有医院的详细信息，但其中不包括医院等级，医院等级在dict中，需要远程调用dict微服务中的接口获取医院等级
//        List<Hospital> hospitalList = pages.getContent(); 这是原写法
        //Hospital类中有一个param属性，是一个HashMap集合，里边可添加其他参数
        //可改成java8新特性中的stream()流写法+lambda表达式写法
        pages.getContent().stream().forEach(item -> {
            this.setHospitalHosType(item);
        });


        return pages;
    }
```

之前通过`Page<Hospital> pages = hospitalRepository.findAll(example,pageable);`得到的分页查询结果中，通过`.getContent()`可得到对象列表  

使用java8新特性流写法来完成最后的获取医院等级  

```java
pages.getContent().stream().forEach(item -> {
    this.setHospitalHosType(item);
});
```

通过`dictFeignClient.getDictName()`可以远程调用其他微服务中的接口  

```java
private Hospital setHospitalHosType(Hospital hospital) {
    //在这里使用feign远程调用微服务完成业务
    //根据dictCode和value（Hostype）来获取医院等级
    String hospitalLevel = dictFeignClient.getDickName("Hostype", hospital.getHostype());

    //练习
    //查询省
    String province = dictFeignClient.getDictName(hospital.getProvinceCode());
    //查询市
    String city = dictFeignClient.getDictName(hospital.getCityCode());
    //地区
    String district = dictFeignClient.getDictName(hospital.getDistrictCode());
    //练习end
    //并放入param属性中
    hospital.getParam().put("Address",province+city+district);
    hospital.getParam().put("hospitalLevel",hospitalLevel);
    return hospital;
}
```

# 数据字典显示接口

根据DictCode获取其下级节点，DictCode为具体字典类别（Province省、computer电脑、Hostype医院等级等）  

主要用于查询省，用于初始化前端的下拉框中的省、市

## controller层

```java
/**
     * 根据dictCode来获取省id 86，
     * 再根据id 86联动查询出具体的省市
     * @param dictCode
     * @return
     */
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findChildByDictCode/{dictCode}")
    public Result<List<Dict>> findChildByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> list = dictService.findChildByDictCode(dictCode);
        return Result.ok(list);
    }
```

## service层

```java
/**
     * 根据DictCode查询第一层级的id，再将id作为parentId查询其子节点
     * @param dictCode
     * @return
     */
    @Override
    public List<Dict> findChildByDictCode(String dictCode) {
        Dict codeDict = this.getDictByDictCode(dictCode);
        if(null == codeDict) return null;
        return this.getChildData(codeDict.getId());
    }
```

调用了getDictByDictCode方法，此方法通过dict_code查询dict_code为`省`的记录，返回该记录

```java
/**
     * 根据dictCode查询Dict
     * @param dictCode
     * @return
     */
    private Dict getDictByDictCode(String dictCode){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("dict_code",dictCode);
        Dict dict = dictMapper.selectOne(wrapper);
        return dict;
    }
```

之后调用了getChildData方法，此方法将id作为parentId查询所有parentId为id的所有记录

```java
/**
     * 将id作为parent_id，查询所有parent_id为`id`的记录
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "dict",keyGenerator="keyGenerator")
    public List<Dict> getChildData(Long id) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("parent_id",id);
        List<Dict> list = baseMapper.selectList(wrapper);
        //为每个dict对象的hasChild属性赋值，当孩子节点还有孩子节点时，前端可以继续递归查询
        for (Dict dict: list) {
            dict.setHasChildren(hasChild(dict.getId()));
        }
        System.out.println("查询了数据库！");
        return list;
    }

    //还要判断子节点是否有子节点
    /**
     * 判断此节点是否有孩子节点
     * @param id
     * @return 返回布尔值，有则为真
     */
    public boolean hasChild(Long id){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
```

# 更新医院上线状态接口

后台的前端点击改变医院上线信息后，要更新医院上下线状态，此状态决定是否在前台给患者显示医院

## controller层

```java
/**
     * 改变医院的上线状态
     * @param id 通过id查询具体医院
     * @param status 应修改的医院状态
     * @return
     */
    @ApiOperation(value = "改变医院上线状态")
    @GetMapping("updateHospitalStatus/{id}/{status}")
    public Result updateHospitalStatus(@PathVariable String id,
                                       @PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }
```

## service层

```java
/**
     * 修改医院的上线状态
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(String id, Integer status) {
        //根据id查询出对象
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }
```

# 查看医院详情接口

当前端点击查看时，携带id请求接口，此接口返回一个HashMap集合，包含两个key：`hospital`医院信息、 `bookingRule`预约规则信息

## controller层

```java
/**
     * 根据id查询医院的具体信息
     * @param id
     * @return 返回HashMap集合 key： "hospital" "bookingRule"
     */
@ApiOperation(value = "查询医院详情信息")
@GetMapping("showHospitalDetails/{id}")
public Result showHospitalDetails(@PathVariable String id){
    //返回的是一个HashMap集合，包含hospital和bookingRule
    return Result.ok(hospitalService.getHospitalById(id));
}
```

## service层

通过id查到MongoDB中的医院信息，因为MongoDB中的信息不含医院等级和医院地址（使用逻辑外键存储），所以需要调用数据字典微服务，给查出的医院设置上等级信息、具体地址。  

而为了方便前端查询，我们将预约规则单独拎出来返回，所以使用了HashMap。HaspMap在底层会

```java
/**
 * 通过id获取医院的具体信息
 * @param id
 * @return
 */
@Override
public HashMap<String, Object> getHospitalById(String id) {
    HashMap<String, Object> map = new HashMap<>();
    Hospital hospital = hospitalRepository.findById(id).get();
    hospital = this.setHospitalHosType(hospital);
    map.put("hospital",hospital);
    map.put("bookingRule",hospital.getBookingRule());

    //因为不需要重复返回，所以把实体中的属性清空
    hospital.setBookingRule(null);
    return map;
}
```

# 排班接口

用来管理各个医院排班信息

## 新建排班Controller

在service_hosp模块下新建`controller.DepartmentController`  

加上注解`@RestController` `@RequestMapping("/admin/hosp/department")` `@CrossOrigin`

# 查询医院所有科室列表接口

通过hoscode来查询此医院的所有科室，返回科室列表list，因为一个科室可能包含多个小科室，所以应使用泛型`DepartmentVo`

## controller层

```java
@ApiOperation(value = "查询医院的所有科室列表")
@GetMapping("getDepartmentList/{hoscode}")
public Result getDepartmentList(@PathVariable String hoscode){
    //科室可能还包含小科室，所以应该使用DepartmentVo，此对象中有属性children为子科室列表
    List departmentList =  departmentService.findDepartmentTree(hoscode);

    return Result.ok(departmentList);
}
```

## service层

```java
/**
 * 根据hoscode查询此医院下所有科室
 * @param hoscode
 * @return
 */
@Override
public List<DepartmentVo> findDepartmentTree(String hoscode) {
    List<DepartmentVo> result = new ArrayList<>();
    //根据医院编号查询医院所有科室信息
    Department departmentQuery = new Department();
    departmentQuery.setHoscode(hoscode);
    //创建mongoDB中的example
    Example<Department> example = Example.of(departmentQuery);
    //数据库中所有科室的列表信息
    List<Department> departmentDbList = departmentRepository.findAll(example);

    //根据大科室的编号 bigcode 分组， 获取每个大科室里面下级子科室
    //使用java8新特性stream流 的分组
    Stream<Department> departmentStream = departmentDbList.stream(); //将集合变为流,基于流的计算并不改变原数据结构
    //对流做筛选分组 ,返回Map集合，key是科室编号，List是大科室下的小科室信息
    Map<String, List<Department>> departmentMap = departmentStream.collect(Collectors.groupingBy(Department::getDepcode));
    //同等写法
    /*Map<String,List<Department>> map = new HashMap<>();
    for (Department department : departmentList) {
        if (map.get(department.getHoscode()).isEmpty()){
            List list = new ArrayList();
            list.add(department);
            map.put(department.getHoscode(),list);
        }else {
            map.get(department.getHoscode()).add(department);
        }
    }*/

    //封装为DepartmentVo以便返回
    //遍历Map,得到Map的entry关系 然后entry.getKey()  entry.getValue()
    for (Map.Entry<String,List<Department>> entry : departmentMap.entrySet() ){
        String bigCode = entry.getKey();

        List<Department> departmentList = entry.getValue();
        
        //封装大科室
        DepartmentVo bigDepartmentVo = new DepartmentVo();
        bigDepartmentVo.setDepcode(bigCode);
        //大科室的名字是ArrayList中第一个科室
        bigDepartmentVo.setDepname(departmentList.get(0).getBigname());
        
        //封装子科室  子科室没有子科室了
        List<DepartmentVo> childrenDepartmentVoList = new ArrayList<>();
        for (Department department : departmentList){
            DepartmentVo childrenDepartmentVo = new DepartmentVo();
            childrenDepartmentVo.setDepcode(department.getDepcode());
            childrenDepartmentVo.setDepname(department.getDepname());
            //将子科室一个个放入list中
            childrenDepartmentVoList.add(childrenDepartmentVo);
        }
        //将大科室的children属性指向list
        bigDepartmentVo.setChildren(childrenDepartmentVoList);    
        //将大科室一个个放入结果list中
        result.add(bigDepartmentVo);
    }
    //返回结果list
    return result;
}
```

# 根据医院和科室编号分页查询排班信息

因为前端点击具体科室时，需要显示出科室具体的排班表，如几月几号周几几点，因为情况众多，需要分页显示

## controller层

传入：当前页码`page`，每页记录数`limit`，医院编号`hoscode`,科室编号`depcode`   

返回：一个Map<String,Object>集合,`scheduleRuleVos`按照workDate工作日期排序的排班信息列表list，`total`总记录数用于分页，`baseMap`存放基础信息，里边包含`hospitalName`医院名称，用于更好的显示前端

```java
/**根据医院和科室编号分页查询排班信息
     *
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return 返回一个Map集合，包含日期、已预约人数、可预约人数等
     */
    @ApiOperation(value = "根据医院和科室编号分页查询排班信息")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode){
        //因为返回值需要包含日期、已预约人数、可预约人数等，所以返回Map集合更方便
        //根据当前页码、每页记录数、医院编号、科室编号来分页查询排班规则信息
        Map<String ,Object> scheduleMap = scheduleService.getScheduleRulePageByHoscodeAndDepcode(page,limit,hoscode,depcode);
        return Result.ok(scheduleMap);

    }
```



## service层

使用springData来进行条件分页查询  

1. 使用`Criteria.where`封装条件对象，查询出医院编号和科室编号相同的记录
2. 使用`Aggregation.newAggregation(`实现分组操作，根据医生工作日期workDate分组
   1. `Aggregation.match(criteria),`传入封装好的条件
   2. `Aggregation.group("workDate") `用于分组
   3. `.first("workDate").as("workDate")`  对分组字段进行显示
3. 调用`mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);`方法执行查询，返回`AggregationResults`对象
4. 调用`aggResult.getMappedResults();`方法从对象中获取List数据
5. 因为还需要返回总记录数`total`，所以再次分组查询总记录数
6. 因为list集合中的对象的`workDate`属性需要转为星期以便显示，所以遍历集合并调用自定义方法`this.getDayOfWeek(new DateTime(workDate));`来转为星期并封装
7. 调用`hospitalService.getHospitalNameByHoscode(hoscode);`获取医院名称
8. 将医院名称放入baseMap中
9. 将list集合放入result.scheduleRuleVos中
10. 将total放入result.total中
11. 将baseMap放入result.baseMap中
12. 返回result集合

```java
/**
 * 分页查询出排班规则信息
 * 需要根据日期排序
 * @param page
 * @param limit
 * @param hoscode
 * @param depcode
 * @return 返回一个map集合，包括排班信息、日期、预约人数等
 */
@Override
public Map<String, Object> getScheduleRulePageByHoscodeAndDepcode(long page, long limit, String hoscode, String depcode) {
    //根据医院编号和科室编号查询
    //MongoTemplate中有一个封装条件对象
    Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

    //根据医生workDate分组
    //实现分组操作,用于聚合操作(注意别引错包） org.springframework.data.mongodb.core.aggregation.Aggregation;
    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.match(criteria),  //传入封装好的条件
        Aggregation.group("workDate") //分组字段
        .first("workDate").as("workDate")  //对分组字段进行显示
        //统级号源数量
        .count().as("docCount")
        .sum("reservedNumber").as("reservedNumber")
        .sum("availableNumber").as("availableNumber"),
        //排序
        Aggregation.sort(Sort.Direction.DESC,"workDate"),
        Aggregation.skip((page-1) * limit),
        Aggregation.limit(limit)
    );

    //调用方法 执行查询
    AggregationResults<BookingScheduleRuleVo> aggResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
    //从对象中获取数据
    List<BookingScheduleRuleVo> scheduleRuleVos = aggResult.getMappedResults();

    //分组查询总记录数
    Aggregation totalAgg = Aggregation.newAggregation(
        Aggregation.match(criteria),  //传入封装好的条件
        Aggregation.group("workDate") //分组字段
    );
    AggregationResults<BookingScheduleRuleVo> totalAggResult = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
    int total = totalAggResult.getMappedResults().size();

    //封装星期属性dayOfWeek
    for (BookingScheduleRuleVo scheduleRuleVo : scheduleRuleVos) {
        Date workDate = scheduleRuleVo.getWorkDate();
        //转为星期
        String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
        scheduleRuleVo.setDayOfWeek(dayOfWeek);

    }
    //设置最终数据，完成返回
    HashMap<String, Object> result = new HashMap<>();
    result.put("scheduleRuleVos",scheduleRuleVos);
    result.put("total",total);
    //获取医院名称,需要用另一个service的方法
    String hospitalName = hospitalService.getHospitalNameByHoscode(hoscode);

    //将基础信息放入baseMap
    HashMap<Object, Object> baseMap = new HashMap<>();
    baseMap.put("hospitalName",hospitalName);
    result.put("baseMap",baseMap);

    return result;
}
```

# 根据医院编号、科室编号、工作日期查询排班详情

当获得排班信息后，要根据排班的日期查询出具体的医生

## controller

调用`scheduleService`的`getScheduleRuleDetail`方法

```java
/**
 * 根据医院编号、科室编号、预约时间来查询详情
 * @param hoscode
 * @param depcode
 * @param workDate
 * @return 
 */
@ApiOperation(value = "根据医院和科室编号、工作日期查询预约详情")
@GetMapping("getScheduleRuleDetail/{hoscode}/{depcode}/{workDate}")
public Result getScheduleRuleDetail(@PathVariable String hoscode,
                                    @PathVariable String depcode,
                                    @PathVariable String workDate){
    List<Schedule> list = scheduleService.getScheduleRuleDetail(hoscode,depcode,workDate);

    return Result.ok(list);

}
```

## service层

```java
/**
 * 查询科室排班详情
 * @param hoscode
 * @param depcode
 * @param workDate
 * @return
 */
@Override
public List<Schedule> getScheduleRuleDetail(String hoscode, String depcode, String workDate) {
    List<Schedule> list = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
    //封装医院名称、科室名称、日期对应星期
    list.stream().forEach(item->{
        this.packageSchedule(item);
    });

    return list;
}

private void packageSchedule(Schedule schedule){
    //医院名称
    schedule.getParam().put("hosname",hospitalService.getHospitalNameByHoscode(schedule.getHoscode()));
    //设置科室名称
    schedule.getParam().put("depname",departmentService.getDepname(schedule.getHoscode(),schedule.getDepcode()));
    //设置星期
    schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
}
```

