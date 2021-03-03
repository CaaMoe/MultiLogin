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

请查阅插件config.yml文件
