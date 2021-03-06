# MultiLogin

这是一款高兼容性的外置登入插件，理论上支持Spigot 1.8+版本和大多数Bungee版本。

该插件通过使用反射修改Yggdrasil验证过程从而达到实现多Yggdrasil共存的效果。

该插件最大的好处就是 完全不需要去对启动服务器的命令行进行修改 只需要放在plugins内就可以使用

## 功能

截止目前，该插件有下列几项功能：

* 支持Bungee
* 多Yggdrasil共存
* 可设置游戏内UUID
* 兼容多Yggdrasil环境下的白名单
* 可为单独的验证服务器内玩家开启白名单

* 账号安全
    * ID保护
        * 防止服务器在多Ygg环境下出现重名账户
        * 防止特定情况下ID被抢注而无法游戏
    * 账号保护：防止服务器在多Ygg环境下出现重UUID账户而导致数据错乱（概率极低）

## 命令、权限和变量

|  命令   | 权限  | 说明 |
|  ----  | ---- | ---- |
|/Whitelist add <target>|multilogin.whitelist.add|添加target到白名单中|
|/Whitelist remove <target>|multilogin.whitelist.remove|添移除target的白名单|
|/Whitelist on|multilogin.whitelist.on|开启全局白名单|
|/Whitelist off|multilogin.whitelist.off|关闭全局白名单|
|/Multilogin query [target]|multilogin.multilogin.query|查询target（可以是离线玩家）是通过何种方式登入的游戏|
|/Multilogin reload<target>|multilogin.multilogin.reload|重新加载配置文件|
| |multilogin.update|接收新版本通知|
| |    multilogin.whitelist.tab|自动补全Whitelist命令参数所需要的权限|
| |    multilogin.multilogin.tab|自动补全Multilogin命令参数所需要的权限|

### 变量 （Bukkit Only）

| 变量 | 说明 |
|  ----  | ---- |
|%multilogin_currentname%    | 玩家当前ID|
|%multilogin_onlineuuid%     | 玩家在线的UUID|
|%multilogin_redirecteduuid% | 玩家在游戏内的UUID|
|%multilogin_whitelist%      | 玩家具有白名单|
|%multilogin_yggdrasilname%  | 玩家所在的Yggdrasil服务器的名字|
|%multilogin_yggdrasilpath%  | 玩家所在的Yggdrasil服务器的路径|

## 使用

### 添加一个Yggdrasil服务器

插件现在支持添加几乎所有类型的Yggdrasil服务器，设置它也是个很简单的过程

一般，它的配置格式为

                        demo:
                            enable: false
                            name: "yggdrasil name"
                            url: "https://example.com/yggdrasil"
                            checkUrl: true
                            convUuid: DEFAULT
                            whitelist: true
                            postMode: false
                            noUrlDeal: false
                            head: "hasJoined?"

配置的根节点 ‘demo’ 为插件识别Yggdrasil服务器的唯一方式，该值不建议设置除数字、字母以外的值。若执意修改它，将会导致所有曾经通过该验证方式验证的玩家无法登入游戏。

节点’enable‘，意思为是否启用当前Yggdrasil服务器，若他的值为false，就相当于它什么都做不了.

节点’name‘，既为Yggdrasil验证服务器的名称，您可以随便修改它，根节点更加像玩家的uuid，此节点的值像玩家的name

节点’url‘，它用来设置Yggdrasil验证链接，一般符合技术规范的Yggdrasil链接都可以直接使用浏览器打开，可以看到一些JSON消息（纯文本）

节点’checkUrl‘，它用来设置插件启动或重载时，是否进行检查节点’url‘内的值的有效性，若当前Yggdrasil不遵循技术规范的话，此值建议为false（比如Minecraft正版验证，它就不遵循第三方设定的技术规范）

节点’convUuid‘，在配置文件节点中已经明确定义了他的用法，即控制玩家第一次进入服务器时所分配到的UUID，适用于离线服转正版或伪正版服。若您不知道此节点是干嘛的，那使用默认值。

节点’whitelist‘，即为白名单，没什么好讲的，都在注释里面写清楚了

节点’postMode‘，如果该Yggdrasil验证方式是使用post报文的方式响应数据，此值设置为true。绝大多数情况下，Yggdrasil服务器都是以GET方式响应数据的，不是特殊需求，请使用默认值.

节点’noUrlDeal‘，若此Yggdrasil验证服务器的最终链接并不是以’/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s%s‘结尾的，请将此值设置为true，并将完整的验证链接填入节点url中（末尾不带hasJoined?username=%s&serverId=%s%s）.

节点’head‘，若此Yggdrasil验证服务器的最终链接参数头部分并不是’hasJoined?‘而是’hasJoinserver?‘或其他值，请将该值填入此节点中

最后，若配置无误，将配置节点全部复制到services节点下，重启或重载插件配置文件即可生效

详细请查阅插件config.yml文件
