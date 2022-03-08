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
