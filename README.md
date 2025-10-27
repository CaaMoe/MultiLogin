<div align="center">

# 🌐 MultiLogin

_✨ 一款支持多平台、多账号登录的 Minecraft 服务器验证插件 ✨_

![GitHub commit activity](https://img.shields.io/github/commit-activity/t/CaaMoe/MultiLogin?style=flat-square)
[![GitHub release](https://img.shields.io/github/release/CaaMoe/MultiLogin.svg?style=flat-square)](https://github.com/CaaMoe/MultiLogin/releases/)
[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
[![Discord](https://img.shields.io/discord/1225725211727499347.svg?logo=discord&style=flat-square)](https://discord.gg/9vh4kZRFCj)
[![bStats](https://img.shields.io/bstats/servers/21890?color=brightgreen&label=bStats&style=flat-square)](https://bstats.org/plugin/velocity/MultiLogin/21890)

</div>

> [!CAUTION]
> 🚧 当前分支仍处于验证开发阶段，尚未稳定。  
> **请勿在生产环境中使用！**  
> 测试/正式版本将于未来更新中发布，敬请期待。

---

## 📖 项目概述

**MultiLogin** 是一个为 **Minecraft 服务器** 提供多登录支持的插件。  
它允许玩家使用不同来源的账号登录到同一服务器，并提供灵活的配置和扩展能力。

主要功能包括：

- 🔐 **分离验证服务器**：  
  支持将验证流程与主游戏服务器分离，玩家在独立的验证服务器上完成登录后再被传送回主服。
- 👥 **多角色支持**：  
  玩家可自由创建、编辑、切换多个游戏角色（如“小号”或不同身份角色）。
- 🧩 **跨平台兼容**：  
  兼容多种 Minecraft
  服务端平台（如 [Paper](https://papermc.io/software/paper)、[Velocity](https://papermc.io/software/velocity) 等）。
- 📝 **MiniMessage 消息格式**：  
  支持 [MiniMessage](https://docs.papermc.io/adventure/minimessage) 格式，允许自定义带样式和颜色的消息。
- 💾 **多数据库后端**：  
  使用 [Exposed](https://github.com/JetBrains/Exposed) ORM 框架，支持多种数据库：  
  `SQLite`、`MySQL`、`PostgreSQL` 等。
- 🧠 更多功能正在开发中……

---

## 🧭 远程验证服务器

MultiLogin 支持将“验证逻辑”从主服务器中独立出来或者混合使用。  
玩家首先连接验证服务器，在该服务器完成登录、认证操作后，再通过 `transfer` 协议被转送至主服务器。

在转送时，验证服务器会将携带经过 **签名** 后的验证数据塞入客户端 **cookie** 中，从而实现安全的身份传递。

这种结构的优势在于：

- ✅ 扩展性强，便于集成第三方登录方式
- ✅ 验证逻辑与业务逻辑完全解耦
- ✅ 提高安全性与可维护性
- ✅ 等等……

如何搭建验证服务器，请参考 [远程验证服务器搭建指南](docs/remote-authentication-server.md)。

---

## ⚙️ 安装指南

1. 从 [GitHub Releases 页面](https://github.com/CaaMoe/MultiLogin/releases) 下载适配你服务端的平台版本。
2. 将 `.jar` 文件放入服务器的 `plugins`（或 `mods`）目录。
3. 启动或重启服务器以加载插件。

---

## 🛠️ 配置说明

首次运行插件后，将在以下目录自动生成配置文件： `plugins/MultiLogin/` 或（取决于平台）`plugins/multilogin/`、
`config/multilogin/`

你可以通过编辑配置文件来自定义：

- 验证服务器分离/单独本地在线验证/混合模式
- 角色绑定策略
- 数据库连接设置  
  等等……

修改完成后，可使用命令 `/multilogin admin reload` 或重启服务器以应用更改。

---

## 🧩 从源码构建

如果你希望自行编译 MultiLogin，可按以下步骤操作：

1. 克隆仓库：
   ```bash
   git clone https://github.com/CaaMoe/MultiLogin.git
   ```

2. 进入项目目录：
    ```bash
   cd MultiLogin
    ```

3. 构建项目：
    ```bash
    ./gradlew build
   ```
4. 构建完成后，生成的插件文件位于：
   ```
   outputs/
   ```

### 💡 或使用 GitHub Actions 自动构建

1. [Fork](https://github.com/CaaMoe/MultiLogin/fork) 本仓库
2. 启用 GitHub Actions。
3. 任意提交一次代码，即可自动触发构建流程。

---

## 📞 联系我们

如果你在使用过程中遇到问题或有建议，欢迎通过以下渠道反馈：

- 💬 加入 [QQ](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 群
- 🌍 加入 [Discord](https://discord.gg/9vh4kZRFCj) 服务器
- 🐛 提交 [Issue](https://github.com/CaaMoe/MultiLogin/issues/new)

---

## 📜 开源许可证

本项目基于 [![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
协议发布。
详细内容请参阅 [LICENSE](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE) 文件。

---

## 💖 致谢

感谢所有为 MultiLogin 做出贡献的开发者与社区成员！
你们的支持与反馈让项目不断完善与成长。

<a href="https://github.com/CaaMoe/MultiLogin/graphs/contributors"> <img src="https://contrib.rocks/image?repo=CaaMoe/MultiLogin" alt="贡献者头像"/> </a>

> Made with ❤️ by the MultiLogin Community.