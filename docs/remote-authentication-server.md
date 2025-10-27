# 远程验证服务器搭建指南

> [!WARNING]
> 🚧 该文档仍在编写中，部分内容可能不完整或有误。请谨慎参考。
> 

本文档介绍如何搭建一个或多个远程验证服务器，以便扩展更多的登录方式和认证机制。

## 目录

- [前提条件](#前提条件)
- [搭建步骤](#搭建步骤)
- [配置远程验证服务器](#配置远程验证服务器)
- [配置主服务器](#配置主服务器)
- [测试与验证](#测试与验证)
- [常见问题](#常见问题)

## 前提条件

在开始之前，请确保您已经具备以下条件：

- 一台或多台可用的服务器，用于搭建专用的远程验证服务器。
- 确保每台服务器的时间同步，以避免认证过程中出现时间相关的问题。

## 搭建步骤

1. 前往 [Paper 下载页](https://papermc.io/downloads/paper)，下载你认为合适版本的 Paper 服务器。
2. 按照官方文档搭建并启动 Paper 服务器。
3. 下载并安装 [MultiLoginRemoteAuthentication](https://github.com/CaaMoe/MultiLoginRemoteAuthentication) 插件到验证服务器的
   `plugins` 目录。
4. 重启 Paper 服务器以加载此插件。

## 配置远程验证服务器

1. 打开验证服务器的 `plugins/MultiLoginRemoteAuthentication/config.yml` 文件。

## 配置主服务器

## 测试与验证

## 常见问题