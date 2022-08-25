# MultiLogin 多 Yggdrasil 共存的实现

[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)

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
* 异步/同步皮肤修复机智
* 可在 mineskin.org 无法访问 Yggdrasil 提供的材质链接（皮肤站）下使用皮肤修复功能
* HTTP 请求异常重试机智
* 支持使用鉴权代理访问 HTTP 服务

## BUG 汇报

[832210691](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 点击此处，来加入 [QQ](https://im.qq.com/) 交流群

[new issue](https://github.com/CaaMoe/MultiLogin/issues/new) 点击此处，提交你的问题

## 构建

克隆这个项目，执行以下指令：

    gradle shadowJar

构建输出位于 `*/build/libs` 下。

## 安装

插件需要使用 `Java 11`， 不需要安装 `authlib-injector` 、没有任何前置插件，也不需要添加和更改 `JVM`
参数，将合适的版本丢进合适的文件夹下即可使用。

## 文档

[Wiki](https://github.com/CaaMoe/MultiLogin/wiki)
