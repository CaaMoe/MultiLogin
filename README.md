<div align="center">

# MultiLogin

_✨ 正版与多种外置登录共存 ✨_

[![GitHub release](https://img.shields.io/github/release/CaaMoe/MultiLogin.svg)](https://github.com/CaaMoe/MultiLogin/releases/)
[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)

</div>

## 概述

MultiLogin 是一款服务端插件， 功能是让您的服务器支持正版与多种外置登录共存， 用来连接两个或多个外置验证服务器下的玩家，让他们能在一起玩。

## 特性

* 支持设置多达 128 个 Yggdrasil 同时共存
* 支持配置市面上几乎所有类型的 Yggdrasil 服务器
* 以 Yggdrasil 分组管理的白名单系统
* 以 Yggdrasil 分组设置的名称正则约束
* 阻止服务器内出现重名和抢占名称的账户
* 支持控制玩家的 UUID 生成规则
* 支持设置玩家在游戏内的 UUID
* 支持设置多种登录方式登录到同一个游戏数据
* 可控制的重复（异地）登录机制
* 异步/同步皮肤修复机制
* 可在 mineskin.org 无法访问 Yggdrasil 提供的材质链接（皮肤站）下使用皮肤修复功能
* HTTP 请求异常重试机制
* 支持使用鉴权代理访问 HTTP 服务

## 安装

最低需要 `Java 11`， 不需要安装 `authlib-injector` ，没有任何前置插件，也不需要添加和更改 `JVM` 参数

~~把大象装进冰箱需要几步？~~

1. [下载](https://github.com/CaaMoe/MultiLogin/releases)插件
2. 丢进 plugins
3. 启动服务器

## 配置

详见 [Wiki](https://github.com/CaaMoe/MultiLogin/wiki)

## 构建

1. 克隆这个项目
2. 参照说明补全 velocity 的依赖
3. 执行 `gradle shadowJar`
4. 在 `*/build/libs` 下寻找你需要的

或者你也可以

1. Fork 此项目
2. 开启 action
3. 随便提交一个文件

## BUG 汇报

[832210691](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 点击此处，来加入 [QQ](https://im.qq.com/) 交流群

[new issue](https://github.com/CaaMoe/MultiLogin/issues/new) 点击此处，提交你的问题

## 贡献者

<a href="https://github.com/CaaMoe/MultiLogin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CaaMoe/MultiLogin"  alt="作者头像"/>
</a>

[我也想为贡献者之一？](https://github.com/CaaMoe/MultiLogin/pulls)
