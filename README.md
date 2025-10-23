<div align="center">

# MultiLogin

_✨ 多登录 ✨_

![GitHub commit activity](https://img.shields.io/github/commit-activity/t/CaaMoe/MultiLogin?style=flat-square)
[![GitHub release](https://img.shields.io/github/release/CaaMoe/MultiLogin.svg?style=flat-square)](https://github.com/CaaMoe/MultiLogin/releases/)
[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
[![Join our Discord](https://img.shields.io/discord/1225725211727499347.svg?logo=discord&style=flat-square)](https://discord.gg/9vh4kZRFCj)
[![bStats](https://img.shields.io/bstats/servers/21890?color=brightgreen&label=bStats&logo=bs&style=flat-square)](https://bstats.org/plugin/velocity/MultiLogin/21890)

</div>

> [!CAUTION]
> 当前分支还在验证开发中, 目前还不能使用, 绝对不能用于生产环境, 请耐心等待测试/正式版本发布.

## 概述

MultiLogin 是一个用于 Minecraft 服务器的多登录插件, 它允许玩家使用多个平台的不同账号登录服务器, 并提供了丰富的管理功能和配置选项, 以满足不同服务器的需求, 包括但不限于以下功能:

- 支持多种外置登录方式: 玩家可以使用多种不同的外置账号登录到服务器, 如 [MC正版](https://www.minecraft.net), [LittleSkin 外置登录](https://manual.littlesk.in/yggdrasil) 等.
- 角色切换功能: 玩家可以方便地创建和切换游戏内不同的角色, 实现小号/多角色切换.
- 角色编辑功能: 玩家可以自定义角色的一些基本信息, 如昵称等.
- 跨平台支持: 插件设计为兼容多种 Minecraft 服务器平台, 如 [Paper](https://papermc.io/software/paper), [Velocity*](https://papermc.io/software/velocity) 等.
- MiniMessage支持: 插件内置对 [MiniMessage](https://docs.papermc.io/adventure/minimessage) 格式的支持, 允许管理员使用丰富的文本格式和颜色来定制消息内容.
- 数据存储支持: 使用 [Exposed](https://github.com/JetBrains/Exposed) 支持多种数据库存储方式, 包括 [SQLite](https://sqlite.org/)、 [MySQL](https://www.mysql.com/)、[PostgreSQL](https://www.postgresql.org/) 等.
- 还有更多功能正在开发中...

## 安装

1. 下载对应平台的 MultiLogin 插件 JAR 文件, 可从 [GitHub Releases 页面](https://github.com/CaaMoe/MultiLogin/releases) 获取.
2. 将下载的 JAR 文件放入 Minecraft 服务器的 `plugins`(或者是`mods`) 目录.
3. 重启服务器即可使用.

## 配置

插件首次运行时会生成默认的配置文件, 位于 `plugins/MultiLogin`(或者是`plugins/multilogin`, 也可能是`config/multilogin`) 下. 你可以根据需要修改配置文件中的选项, 例如启用或禁用特定的登录方式, 设置角色绑定规则等.
修改配置文件后, 需要重启服务器或使用 `/multilogin reload` 命令来应用更改.

## 命令
以下是 MultiLogin 插件提供的主要命令:
- `/multilogin help` - 显示帮助信息.

## 权限节点
- `multilogin.help` - 允许使用 `/multilogin help` 命令.

## 构建
如果你想从源代码构建 MultiLogin 插件, 请按照以下步骤操作:
1. 克隆本仓库:
   ```bash
   git clone https://github.com/CaaMoe/MultiLogin.git
    ```
2. 进入项目目录:
   ```bash
   cd MultiLogin
   ```
3. 使用 Gradle 构建项目:
   ```bash
   ./gradlew build
   ```
4. 构建完成后, 生成的 JAR 文件将位于 `outputs` 目录下.

或者你也可以

1. [Fork](https://github.com/CaaMoe/MultiLogin/fork) 本仓库到你的 GitHub 账户.
2. 开启 GitHub Actions 自动构建.
3. 随便提交/修改/删除一个文件以触发工作流来使用预设的环境构建插件.


## 联系我们
如果你在使用过程中遇到任何问题或有任何建议, 欢迎通过以下方式联系我们:

- 加入我们的 [QQ 群](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
- 访问我们的 [Discord 服务器](https://discord.gg/9vh4kZRFCj)
- 在 GitHub 上提交 [问题](https://github.com/CaaMoe/MultiLogin/issues/new)

## 许可证
本项目采用 [![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE) 许可证, 详情请参阅 [LICENSE](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE) 文件.

## 致谢
感谢所有为 MultiLogin 插件做出贡献的开发者和社区成员, 你们的支持和反馈使得这个项目得以不断改进和发展!

<a href="https://github.com/CaaMoe/MultiLogin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CaaMoe/MultiLogin"  alt="作者头像"/>
</a>

[我也想为贡献者之一?](https://github.com/CaaMoe/MultiLogin/pulls)