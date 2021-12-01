# MultiLogin 多 Yggdrasil 共存的实现

[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)

## 概述

MultiLogin 是一款服务端插件(MOD)， 功能是让您的服务器支持正版与多种外置登录共存， 用来连接两个或多个外置验证服务器下的玩家，让他们能在一起玩。

## 特性

* 支持两种以上的 Yggdrasil 共存，比 [YggdrasilOfficialProxy](https://github.com/YggdrasilOfficialProxy/YggdrasilOfficialProxy) 的还要多
* 支持配置市面上几乎所有类型的 Yggdrasil 服务器
    * 支持添加 [authlib-injector](https://github.com/to2mbn/authlib-injector) 所规范的 Yggdrasil 服务器
    * 支持添加 [统一通行证](https://login2.nide8.com:233/) Yggdrasil 服务器
    * 支持添加畸形 Yggdrasil 服务器
* 限制账户仅能使用其中一种验证方式，杜绝重复 UUID 所造成的危害，保护账号安全
* 阻止服务器内出现重名账户
* 阻止强制登入 ID 已被注册的账户
* 可使用正则约束用户名命名方式
* 支持以 Yggdrasil 服务器分组管理的白名单系统
* 支持控制用户在游戏内的 UUID
* 可控制的重复登入机制
* 支持 PlaceholderAPI
* 自动修复皮肤签名问题
* 支持 Velocity、Bungee、Bukkit、Fabric

## BUG 汇报

[832210691](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 点击此处，来加入 [QQ](https://im.qq.com/) 交流群

[new issue](https://github.com/CaaMoe/MultiLogin/issues/new) 点击此处，提交你的问题

## 构建

克隆这个项目，执行以下指令：

    gradle shadowJar

构建输出位于 `bukkit/build/libs`、`bungee/build/libs`、`velocity/build/libs`、`fabric/build/libs` 下。

## 安装

插件不需要使用 `authlib-injector` 无必要的前置插件，不需要添加和更改 `JVM` 参数，将合适的版本丢进 `plugins`、`mods` 文件夹下即可使用

## 文档

* [快速上手](https://github.com/CaaMoe/MultiLogin/wiki#%E5%BF%AB%E9%80%9F%E4%B8%8A%E6%89%8B)
* [命令、权限和变量](https://github.com/CaaMoe/MultiLogin/wiki#%E5%91%BD%E4%BB%A4%E6%9D%83%E9%99%90%E5%92%8C%E5%8F%98%E9%87%8F)
* [进阶使用](https://github.com/CaaMoe/MultiLogin/wiki#%E8%BF%9B%E9%98%B6%E4%BD%BF%E7%94%A8)
* [API](https://github.com/CaaMoe/MultiLogin/wiki#api)

## 开源协议

`MultiLogin`采用 `GPL-3.0`协议开源
