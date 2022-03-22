# 这里用于记录bug修改
# README.md合并分支时被覆盖导致文档消失
## 描述
因为`.gitignore`中配置了忽略`README.md`，所以第一次提交时没有提交上README.md文件,但是我想上传到远程仓库上，所以手动add了一下，但是并未commit。然后直接进行了pull和push操作，点击了marge自动合并，导致卸载README.md中的笔记全部都被覆盖消失了。。。
## 原因
没有commit便进行了pull拉取+marge自动覆盖
## 解决办法
路径： idea底部-> git -> Shelf -> 右键->选择Unshelve(或者选中状态下点击左侧按钮)  
点击Unshelve Changes
## 总结
每次add后一定要commit提交，每次pull前一定要检测是否将所有文件都进行了commit

# 前端npm install 报错

## 描述

前端项目使用vue-admin的模板，直接npm install安装依赖，但是报错：解析依赖树出错！  

执行`npm i --legacy-peer-deps`时依然出错，看日志可能是python问题。  

尝试去换新的vue-admin-template吧

## 原因

本地npm版本为16，过高了，需要降版本

## 解决办法

从github上克隆了最新的基于vue脚手架的vue-admin-template哈哈哈哈

## 总结

老项目使用老版本工具，如果不按视频的每一步进行，就会出现各种奇奇怪怪的bug

# 前端改完登录api后保存找不到‘token'

## 描述

点击登录按钮报错：不能从未定义中找到token

## 原因

原来是不可以用`{data} = {'token':'admin}`来给data赋值，会造成data 的 undefined！！

## 解决

把`{data}`的括号都去掉

## 总结

es6语法害人啊

# 后端bug分页查询未返回total数据

## 描述

调用后端接口的`admin/hosp/hospitalSet/findHospitalSet/${current}/${limit}`时返回total为0

## 原因

后端未配置分页查询插件。跟着视频敲的，我也避免不了

## 解决

配置分页查询插件即可

## 总结

无法避免

# 前后端交互错误，前端报错403，后端报错不支持GET请求，但是明明是POST请求

## 描述

打开前端的医院设置页面，前端请求一个分页查询，明明设置的post请求，但是显示get请求，报错403  

403：服务器拒绝访问，可能是没有权限访问  

我猜是跨域问题

## 原因

果然是跨域问题，服务器端未设置跨域资源共享

## 解决

在controller中添加`@CrossOrigin`注解即可

## 总结

跨域问题的解决多种多样，又学到一种

# 前端请求成功依然显示报错

## 描述

修改完跨域问题后，后端响应200，结果前端蹦出来红色error’显示成功‘，当时就觉得奇怪，查看控制台发现在request.js中返回的信息

## 原因

这个框架的响应过滤，成功状态码是20000，改成200就行

## 解决

在request.js中修改状态码拦截为200

## 总结

框架很方便，出错很诡异

# 前端批量删除数据未删除

## 描述

前端删除显示成功，但是数据库中数据并未改变

## 原因

el-table中的`@selection-change="handleSelectionChange"`没有加`@`

## 解决

加上@符号就好了

## 总结

没复制完全，淦

# 前端访问另外一个微服务（8202）时只能访问到第一个微服务

## 描述

前端发送请求只能访问8201，无法访问8202

## 原因

配置访问多个端口很麻烦

## 解决

用nginx或gateway转发

## 总结

# Redis连接不上

## 描述

后端连接redis时报错，说redis在安全模式下拒绝访问

## 原因

redis开启了安全模式，并且没有设置密码

## 解决

设置密码即可

```
config set requirepass root
```

设置完成后登录

```
auth root
```

然后在配置文件中添加

```pr
spring.redis.password=root
```

## 总结

轻松解决

# 配置nginx后发现请求后端接口超时

## 描述

前端报错请求超时，进入nginx的error日志中看到请求windows主机端口超时，我猜可能是防火墙没关，先看看端口通不通-----不通

## 原因

tnnd！！！！  

原来是我后端没启动！！！  

我服了！！！

## 解决

启动后端微服务即可。。。

1. 测试端口

   需要使用telnet，先在linux中安装

   `yum install telnet-server`

   `yum install telnet`

2. 测试不通，开启windows端口

## 总结

粗心大意白忙活半小时

# 医院模拟接口无数据显示

## 描述

进入医院模拟平台，发现没有任何数据

## 原因

数据库中没有id为1的医院

## 解决

在数据库中添加一条记录即可

## 总结

弹幕666

# 医院模拟接口redis连接失败，无法创建RedisTemplate

## 描述

连接redis时，创建redisTemplate失败，报错：`java.lang.NoClassDefFoundError: org/apache/commons/pool2/impl/GenericObjectPoolConfig`

## 原因

没有添加相关依赖

## 解决

添加依赖  

```xml
<dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
```



## 总结

有时候未找到类的原因是没有添加依赖  

要注意最后一行的报错

# 医院的MD5加密与平台的MD5加密不同

## 描述

医院方的明文与平台方发明文相同，使用同样的MD5加密（字符编码也统一过了），但是密文却不相同  

还有很重要的一点：每次md5加密结果都不一样

---

花费7个小时依然未解决，最后五分钟找到原因  

## 原因

两边的map内容应该不同，但是明文相同  

---

明文不同！因为将图片转为了base64编码，在网络传输过程中，java自动将`+` 解析为了 `" "`！！！导致两边明文不同

## 解决

待解决。。。  

先改为只对sign加密了

---

已解决！  

需要将传过来的对象中的`logoData`中的符号转换一下  

```java
//血妈坑壁！！！！艹！
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String)paramMap.get("logoData");
        if(!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll(" ", "+");
            paramMap.put("logoData", logoData);
        }
```



## 总结

累死累活一下午，啥也没de出来

---

忙活7个小时，不如摆烂五分钟。真正意义上的漏洞

# 后端微服务service-hosp的接口`@GetMapping("list/{page}/{limit}")`报错ribbonServerList创建Bean异常（重复多次）

## 描述

后台的前端访问医院列表时返回“失败”信息，经排查是微服务service-hosp出错，报错` Error creating bean with name 'ribbonServerList' defined in com.alibaba.cloud.nacos.ribbon.NacosRibbonClientConfiguration`创建ribbonServiceList失败  

  `Error creating bean with name 'ribbonClientConfig' defined in org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration` 创建ribbonClientConfig失败

## 原因

可能是依赖冲突，反正Maven日常犯病

## 解决

刷新了一下maven就好了

## 总结

maven真的耽误事，好多次了

# 前端bug医院列表数据显示不完全

## 描述

医院列表组件，未显示等级信息（三甲医院）

## 原因

response.data中的key值不对，应该取`hospitalLevel`

## 解决

将医院等级栏改为`<el-table-column prop="param.hospitalLevel" label="等级" width="90"/>`即可

## 总结

对响应的数据不够了解，回头要通读代码

# 前端bug选择市后不在其中填入

## 描述

## 原因

## 解决

待解决

## 总结

## 前端Bug点击查看医院详情时导航栏消失

## 描述

点击查看医院详情时跳转到隐藏页面，但导航栏消失了

## 原因

隐藏路由设置在了外部

## 解决

将隐藏路由设置在`/hospSet`内部即可

## 总结

前端知识忘差不多了
