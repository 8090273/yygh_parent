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





