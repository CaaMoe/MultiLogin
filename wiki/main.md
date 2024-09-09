# MultiLogin Wiki

## 配置文件结构和内容

* `examples`: 存放 service 的所有示例文件, 会在每次启动时**删除**和**覆盖**.
* `services`: 存放和配置所有 service 的地方.
* `libraries`: 存放插件运行时需要使用到的依赖.
* `tmp`: 存放插件运行时产生的临时文件, 会在每次启动时**删除**.
* `config.conf`: 插件主配置文件.
* `message.conf`: 消息配置文件.

## 使用前需要注意的地方

* 尽量避免中途安装这个插件, 中途安装请做好足够的心理准备和寻找大佬相助.
* 切记不能中途更改 service 配置文件中的 `service_id`, 改动会造成不可估量的后果.
* 切记选择**值得信赖**的外置验证服务提供商, 否则会死得很惨.
* 在velocity上使用为了安全需要打开 `player-info-forwarding-mode`.
* 需要搭配反聊天签名插件使用, 否则会提示 `无效的个人聊天公钥签名`.

## 快速装配

从首次启动后的 `examples` 文件夹中选择你想要启用的验证服务, 比如 `examples/official.conf` 和 `examples/littleskin.conf`,
把它们复制到 `services` 中, 根据你的喜好分别打开这两个文件修改其中的 `service_id` 即可启用这两个外置登录功能.

## 指令和权限

[点击跳转](https://github.com/CaaMoe/MultiLogin/blob/v9/wiki/commands.md)

## 进阶使用

参考 [service 完整示例文件](https://github.com/CaaMoe/MultiLogin/blob/v9/velocity/src/main/resources/examples/template_cn_full.conf)

## 最后

[点我点我](https://jq.qq.com/?_wv=1027&k=WrOTGIC7) 马上加入 QQ 群获得最新的咨询.