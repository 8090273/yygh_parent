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