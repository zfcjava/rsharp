# rsharp
a sharp is plugin that replace the package name of an app module rapidly

## 项目目录

## 功能描述

本插件主要是完成替换功能。主要包括的功能如下：

- [x] 包名替换
  - [x] 简单替换，替换范围src/main目录下的源文件
  - [ ] 配置多渠道打包，适配不同的变体
- [ ] 文案替换(不同国家语言替换和对比)

## 引入插件

```
//file: build.gradle [某个模块下的gradle配置文件]

apply plugin: 'com.android.application'
// 指明插件名称
apply plugin: 'com.canjun.rsharp'

...
rsharp{
		//新的包名
    newPackageName = "com.xxx.android"
    //现有包名
    oldPackageName = "com.dirk.rsharp"
    //额外检查的文件名称或者目录，相对于当 当前module的根目录
    checkFiles = ['google-services.json']
}
```

