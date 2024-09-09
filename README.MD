<div align="center">

# MultiLogin

_✨ 正版和多种外置登录共存 ✨_

[![gitHub download](https://img.shields.io/github/downloads/CaaMoe/MultiLogin/total.svg)](https://github.com/CaaMoe/MultiLogin/releases/)
[![gitHub release](https://img.shields.io/github/release/CaaMoe/MultiLogin.svg)](https://github.com/CaaMoe/MultiLogin/releases/)
[![gitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)

</div>

> [!WARNING]
>
> MultiLogin v9 仍然在开发和测试中.

## 这是什么

这是一个运行在 [Velocity](https://github.com/PaperMC/Velocity) 上的插件,
功能是让正版和使用 [authlib-injector](https://github.com/yushijinhun/authlib-injector) 或其他外置登录的玩家能在同一个服务器下一起游戏.

## 插件能做什么

* 支持超级多个不同来源的外置验证共存
* 智能管理游戏内的档案
* 档案离线登录支持
* ~~丰富~~的操作指令
* 还有...很多

## 运行环境要求

* Velocity 3.3.0+
* Java 17+

## 如何安装

从 [这里](https://github.com/CaaMoe/MultiLogin/releases) 下载到插件本体后直接塞到 `./plugins` 插件文件夹下重新启动代理即可.

第一次使用的话请仔细阅读 [这个](https://github.com/CaaMoe/MultiLogin/blob/v9/wiki/main.md) 文章之后再来来编辑插件的配置文件.

## 问题反馈和功能建议

或许你遇到的问题已经在 [周更版](https://github.com/CaaMoe/MultiLogin/releases/tag/weekly) 中修复,
也可以马上跑到 [github ci](https://github.com/CaaMoe/MultiLogin/actions) 中翻翻看看有没有你想要的.

你也可以加入 QQ 交流群 [832210691](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 进来喝杯茶, 聊聊你的问题和想法, 谈谈你的人生.

也可以点击 [github issue](https://github.com/CaaMoe/MultiLogin/issues/new) 来提交你的问题或者建议.

## 如何编译这个项目

克隆完这个项目后参照 [说明](https://github.com/CaaMoe/MultiLogin/blob/v9/velocity/libraries/README.MD) 补全 Velocity
丢失的依赖并且执行 `./gradlew build` 即可, 编译通过后后从对应的 `libs` 文件夹中即可找到你想要的家伙.

## 项目贡献

[点我点我](https://github.com/CaaMoe/MultiLogin/pulls)

<a href="https://github.com/CaaMoe/MultiLogin/graphs/contributors">
<img src="https://contrib.rocks/image?repo=CaaMoe/MultiLogin"  alt="作者头像"/>
</a>