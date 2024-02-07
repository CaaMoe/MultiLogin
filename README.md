<div align="center">

# MultiLogin

_✨ 正版与多种外置登录共存 ✨_

[![GitHub release](https://img.shields.io/github/release/CaaMoe/MultiLogin.svg)](https://github.com/CaaMoe/MultiLogin/releases/)
[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)

</div>

## 概述

MultiLogin 是一款服务端插件，旨在实现对正版与多种外置登录共存的支持，用于连接两个或多个外置验证服务器下的玩家，使他们能够在同一个服务器上一起游戏。

## 特性

* 支持多达 128 个不同来源的 Yggdrasil 同时共存
* 鉴权代理、重试机制
* 游戏内档案管理系统
* 异步/同步皮肤修复机制
* 支持接管 Floodgate

## 安装

最低需要 `Java 11`， 不需要安装 `authlib-injector` ，没有任何前置插件，也不需要添加和更改 `JVM` 参数

~~把大象装进冰箱需要几步？~~

1. [下载](https://github.com/CaaMoe/MultiLogin/releases) 插件
2. 丢进 plugins
3. 启动服务器

## 配置

详见 [Wiki](https://github.com/CaaMoe/MultiLogin/wiki)

## 构建

1. 克隆这个项目
2. 参照 [说明](https://github.com/CaaMoe/MultiLogin/blob/v6/velocity/libraries/README.md) 补全 velocity 的依赖
3. 执行 `./gradlew shadowJar` / `gradlew shadowJar`
4. 在 `*/build/libs` 下寻找你需要的

或者你也可以

1. [Fork](https://github.com/CaaMoe/MultiLogin/fork) 此项目
2. 开启 Actions
3. 随便提交一个文件

## BUG 汇报

[832210691](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 点击此处，来加入QQ交流群

[new issue](https://github.com/CaaMoe/MultiLogin/issues/new) 点击此处，提交你的问题

## 贡献者

<a href="https://github.com/CaaMoe/MultiLogin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CaaMoe/MultiLogin"  alt="作者头像"/>
</a>

[我也想为贡献者之一？](https://github.com/CaaMoe/MultiLogin/pulls)
