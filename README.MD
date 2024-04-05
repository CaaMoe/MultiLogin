<div align="center">

# MultiLogin

_✨ 正版与多种外置登录共存 ✨_

[![GitHub release](https://img.shields.io/github/release/CaaMoe/MultiLogin.svg)](https://github.com/CaaMoe/MultiLogin/releases/)
[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
[![Join our Discord](https://img.shields.io/discord/1225725211727499347.svg?logo=discord&label=)](https://discord.gg/HJXHCZRS)


</div>

## 注意

v8 项目还在开发中, 你不应该现在去使用它. 请继续使用 v6 版本

## 简介

这里是 MultiLogin v8 源码仓库, MultiLogin 是一款主要为 Minecraft 反代端设计的插件, 旨在安全的实现对正版与多种外置登录共存的支持,
用于连接两个或多个外置验证服务器下的玩家, 使他们能够在同一个服务器上一起游戏.

## 特性

* 支持 128 个不同来源的验证服务器同时共存
* 游戏内档案管理机制
* 支持自动修复签名缺失的皮肤(需要安装 SkinsRestorer)
* 修复聊天签名问题
* 支持接管 Floodgate

## 前置和需求

* Java 17
* SkinsRestorer(可选)
* Floodgate(可选)

## 安装

1. [下载](https://github.com/CaaMoe/MultiLogin/releases/latest) 插件
2. 丢进 plugins
3. 启动服务器
4. 编辑插件配置
5. 重新加载插件或者重启服务器

## 构建

1. 克隆这个项目
2. 执行 `./gradlew shadowJar`
3. 在 `*/build/libs/*` 下寻找你需要的插件本体

或者你也可以

1. [Fork](https://github.com/CaaMoe/MultiLogin/fork) 此项目
2. 开启 Actions
3. 随便提交一个文件

## BUG 汇报

[Weekly Version](https://github.com/CaaMoe/MultiLogin/releases/tag/weekly) 点击此处，也许你遇到的问题已修复

[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
点击此处，来加入QQ交流群

[![Join our Discord](https://img.shields.io/discord/1225725211727499347.svg?logo=discord&label=)](https://discord.gg/HJXHCZRS)
点击此处，来加入Discord

[new issue](https://github.com/CaaMoe/MultiLogin/issues/new) 点击此处，提交你的问题

## 贡献者

<a href="https://github.com/CaaMoe/MultiLogin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CaaMoe/MultiLogin"  alt="作者头像"/>
</a>

[我也想为贡献者之一？](https://github.com/CaaMoe/MultiLogin/pulls)