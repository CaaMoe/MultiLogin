# MultiLogin

[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)

## 什么是外置登录和多外置登录共存？

外置登录其实就是正版登录，在连接和登入服务器时验证账户身份，而不用在游戏内输入 “/login password” 和 “/register password confirm”
来注册和登入。（详见 [authlib-injector 开源的游戏外登录规范](https://github.com/yushijinhun/authlib-injector)）

多外置登录共存是能让一个服务器能支持使用多种不同的外置登录方式进行游戏的一种机制，比如某个服务器可以同时支持正版登录、统一通行证、皮肤战登录，从而不需要所谓的**一刀切**，强制用户注册更多的、不必要的账户。

## 功能特性

* **多 Yggdrasil 共存** 支持更多的 Yggdrasil 账户验证服务器共存，比 YOP 的还要多
* **高度可配置的 Yggdrasil 方式** 支持市面上几乎所有类型的 Yggdrasil 账户验证服务器
* **账户安全机制** 限制只使用其中一种 Yggdrasil 登入方式，杜绝可能出现的重复 UUID 问题
* **用户名核查** 阻止服务器内出现同名账户。
* **抢注机制** 设置某一验证服务器下的玩家可以使用已被记录的用户名强制登入游戏
* **正则检查** 多 Yggdrasil 下分组设置的用户名正则匹配检查
* **高级白名单系统** 多 Yggdrasil 下分组管理的白名单系统
* **重复登入机制** 可设置掉线或强登
* **控制玩家在游戏内的 UUID** 一把锁，两把钥匙
* **PlaceholderAPI 变量支持** Bukkit Only
* **皮肤问题修复** 使正版在不装 CSL 的情况下也能看到外置登入的皮肤
* **多端支持、超高兼容性** 支持 Velocity、Bungee、Bukkit甚至 Fabric 和插件模组端Arclight、CatServer [1.8+]

## 关于混合验证登入下的账户安全问题

由于 Minecraft 原版机制，玩家数据都是使用从外置登录传来的 UUID 作为主键进行读取和保存的，即一个外置服务器下是绝不可能存在有两个相同 UUID 的账户。 而多个外置服务器下是很有可能存在有两个相同 UUID
的账户，这就使得**一旦出现这些账户，数据安全将难以保证，并且极难排查，这将是个毁灭性的后果。**

MultiLogin 通过记录和保存玩家第一次登入成功后使用的**外置登入服务器的path**来判断是否允许登入游戏，这就使得重复 UUID 的两个账户杀一个留一个，从而极大的保证了数据安全。 比如 正版账户A 与 外置账户B 的 UUID
重复，且 A 和 B 从未登入过服务器，当 A 抢先一步登入系统后，B 尝试登入时将会被踢出并且提示 `您只能通过 ‘正版’ 的登入方式进入游戏。`

能不能让两个重复 UUID 的账户也能登入游戏并且又能保证数据安全呢？这当然可以，控制玩家在游戏内的 UUID 也是插件的一大特色功能。但是安全性等待考量，所以现在不行

关于服务器内可能会出现同名问题，MultiLogin 当然也有考虑到（阻止服务器内出现同名账户）。并且还附加了一些有关用户名的实用功能，具体请看配置文件注释

## 如何使用？

与同类型程序相比，MultiLogin 不需要修改或添加任何服务端启动参数（不需要安装任何 authlib-injector 以及同类型前置 javaagent 程序）， 只需将适合服务端的插件本体丢入 plugins(mods)
文件夹下即可。

## 外置配置模板和添加一个外置服务器

插件现在支持添加几乎所有类型的 Yggdrasil 账户验证服务器，具体设置方法如下：

### 模板：

    # 这是一个示例配置：
    # 节点名称 'demo' 作为混合验证系统区分 Yggdrasil 验证服务器唯一性的凭据，设置好后请不要随意的去改动它，
    # 随意的更改节点名称将会导致二次验证系统无法验明用户身份而拒绝用户的登入请求。
    # 可以仿写此节点用来添加新的 Yggdrasil 账户验证服务器。
    demo:
    
        # 启用该验证服务器。
        # 值为 'false' 时将不添加该 Yggdrasil 账户验证服务器到混合登入系统中去，将拒绝一切使用该 Yggdrasil 账户验证服务器的玩家登入游戏。
        # 默认值 'true'
        enable: false
    
        # 该账户验证服务器中的别称。
        # 不用于父节点名称，此节点的值可以随意修改。
        # 必填值，在 Bukkit 服务端中作为变量传递。
        name: "Demo Yggdrasil"
    
        # Yggdrasil 账户验证服务器的基本属性和信息设置。
        body:
    
          # Yggdrasil 账户验证服务器服务端 hasJoined 阶段部分验证请求链接设置。
          # 此节点必须指定完整的请求链接。
          # 其中 GET 请求中的占位变量 {username} 表示 username，占位变量 {serverId} 表示 serverId，
          #        占位变量 {passIpContent} 表示 passIpContent 节点所设置的内容。
          # 请求的 URL 对大小写敏感，设置需谨慎！
          # 一些例子:
          #      GET: "https://example.com/session/minecraft/hasJoined?username={username}&serverId={serverId}{passIpContent}"
          #      GET: "https://example.com/session/minecraft/hasJoined?username={username}&serverId={serverId}"
          #      POST: "https://example.com/session/minecraft/hasJoined"
          # 必填值
          url: "https://example.com/session/minecraft/hasJoined?username={username}&serverId={serverId}"
    
          # hasJoined 阶段使用 POST 请求的方式验证账户身份。
          # 绝大多数下， Yggdrasil 账户验证服务器 hasJoined 阶段都采用 GET 请求方式验证用户数据的，
          # 如果你不了解当前 Yggdrasil 验证流程的话，请勿擅自改动它。
          # 默认值 'false'
          postMode: false
    
          # hasJoined 阶段是否向 Yggdrasil 账户验证服务器传递用户 IP 信息，如果存在。
          # 默认值 'false'
          passIp: false
    
          # 设置 url 节点 {passIpContent} 变量内容，
          # 其中，变量 {ip} 为所获取到的 IP 信息。
          # 仅当 passIp 节点为 true 时此节点有效。
          # 请求的 URL 对大小写敏感，设置需谨慎！
          # 请留意字符串拼接的特殊值 '&' 和 ', '
          # 一些例子:
          #      GET: '&ip={ip}'
          #      POST: ', "ip":"{ip}"'
          # 默认值 '&ip={ip}'
          passIpContent: "&ip={ip}"
    
          # 设置 post 的请求内容。
          # 其中 POST 请求中的占位变量 {username} 表示 username，占位变量 {serverId} 表示 serverId，
          #        占位变量 {passIpContent} 表示 passIpContent 节点所设置的内容。
          # 请求的 URL 对大小写敏感，设置需谨慎！
          # 一些例子:
          #        '{"username":"{username}", "serverId":"{serverId}"{passIpContent}}'
          #        '{"username":"{username}", "serverId":"{serverId}"}'
          # 默认值 '{"username":"{username}", "serverId":"{serverId}"}'
          postContent: '{"username":"{username}", "serverId":"{serverId}"}'
    
        # 设置玩家首次登入后游戏内的 UUID 生成规则
        # 目前只支持以下值:
        #   DEFAULT   - 使用 Yggdrasil 账户证服务器提供的在线 UUID
        #   OFFLINE   - 自动生成离线 UUID
        #   RANDOM    - 随机 UUID
        # 请谨慎设置和修改此节点，避免增加日后维护的心智负担。
        # 为避免触发 Paper 端的某些机制，强烈建议此值为 DEFAULT
        # 默认值 'DEFAULT'
        convUuid: DEFAULT
    
        # 当玩家首次登入时分配到的 UUID 已被使用，自动更正到随机的 UUID。
        # 请尽量保持此值为 true ，除非您了解后果是什么。
        # 默认值 'true'
        convRepeat: true
    
        # 玩家允许设置的用户名正则。
        # 若不匹配将会拒绝当前玩家的登入请求
        # 如果此值留空或未设置，则应用根节点下 'nameAllowedRegular' 节点值。
        # 默认值 ''
        nameAllowedRegular: ''
    
        # 是否为当前验证服务器单独开启白名单。
        # 当全局白名单为 true 时，此节点强制为 true。
        # 默认值: false
        whitelist: false
    
        # 设置占线登入机制，登入时游戏内存在有相同游戏内 UUID 的玩家时
        # 若为 false 将把游戏内玩家踢出，允许验证中的玩家登入（原版默认）
        # 若为 true 则拒绝当前的登入请求
        # 默认值: false
        refuseRepeatedLogin: false
    
        # 验证错误重试次数
        # 默认值: 1
        authRetry: 1

        # 皮肤修复规则，用来解决不同 Yggdrasil 账户验证服务器下的皮肤不可见问题。
        #    比如使用 Minecraft 原版验证服务器的玩家无法看到使用第三方外置验证登入玩家的皮肤。
        # 目前只支持设置以下值:
        #   OFF       - 关闭这个功能
        #   LOGIN     - 占用登入时间进行皮肤修复操作（修复时间过长会导致登入超时）
        #   ASYNC     - 登入后修复（修复成功后需要重新连接服务器）
        # 系统将会自动过滤掉皮肤源域名为 '*.minecraft.net' 的情况
        # 皮肤修复服务器来自 'mineskin.org'
        # 默认值 'OFF'
        skinRestorer: OFF
    
        # 皮肤修复错误重试次数
        # 默认值: 2
        skinRestorerRetry: 2

### 例子

* 添加 Minecraft 原版验证服务器

      official:
        name: "正版"
        body:
          url: "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={username}&serverId={serverId}{passIpContent}"


* 添加 Blessing Skin 类型验证服务器

      blessingSkin:
        name: "Blessing Skin"
        body:
  	  
            # 假设当前 Blessing Skin 的 Yggdrasil api 地址为 {url}
            url: "{url}/sessionserver/session/minecraft/hasJoined?username={username}&serverId={serverId}{passIpContent}"

* 添加 统一通行证 账户验证服务器

      nide8:
        name: "统一通行证"
        body:
  	  
            # 假设当前服务器 UID 为 {uid}
            url: "https://auth2.nide8.com:233/{uid}/sessionserver/session/minecraft/hasJoined?username={username}&serverId={serverId}{passIpContent}"

## 命令和权限

| 命令 | 权限 | 简介 |
|  ----  | ----  | --- |
| /multilogin reload                                                    |  command.multilogin.reload            | 重载插件
| /multilogin yggdrasil info <yggdrasil_path>                            |  command.multilogin.yggdrasil.info            | 获得 Yggdrasil 信息
| /multilogin yggdrasil list                                            |  command.multilogin.yggdrasil.list            | 获得 Yggdrasil 列表
| /multilogin userdata info <online_uuid>                                |  command.multilogin.userdata.info            | 获得一条用户记录
| /multilogin userdata modify yggdrasil <yggdrasil_path> <online_uuid>    |  command.multilogin.userdata.modify.yggdrasil            | 更改某用户的登入方式
| /multilogin userdata modify redirect <redirect_uuid> <online_uuid>    |  command.multilogin.userdata.modify.redirectUuid            | 更改某用户的游戏内UUID
| /multilogin userdata remove <online_uuid>                                |  command.multilogin.userdata.remove            | 移除某用户的记录
| /multilogin skinrestorer remove <online_uuid>                            |  ommand.multilogin.skinrestorer.remove            | 移除某用户的皮肤修复记录
| /multilogin skinrestorer removeAll                                    |  command.multilogin.skinrestorer.remove.all            | 移除所有用户的皮肤修复记录
| /multilogin confirm                                                    |  command.multilogin.confirm            | 确认当前风险操作
| /multilogin list                                                        |  command.multilogin.list            | 获得玩家列表
| /multilogin update                                                    |  command.multilogin.update            | 检查更新
| /whitelist add <name&#124;uuid>                                        |  command.multilogin.whitelist.add            | 白名单添加
| /whitelist remove <name&#124;uuid>                                    |  command.multilogin.whitelist.remove            | 白名单移除
| /whitelist list                                                        |  command.multilogin.whitelist.list            | 白名单列表
| /whitelist clearCache                                                    |  command.multilogin.whitelist.clearCache            | 清除缓存白名单

## PlaceholderAPI 变量（Bukkit Only）

| 变量 | 简介 |
|  ----  | --- |
| %multilogin_currentname%   | 玩家当前的游戏 ID|
| %multilogin_onlineuuid%   | 玩家在线的 UUID|
| %multilogin_redirecteduuid%   | 玩家在游戏内的 UUID|
| %multilogin_whitelist%  | 玩家是否具有白名单|
| %multilogin_yggdrasilname%  | 玩家所在的 Yggdrasil 账户验证服务器的名字|
| %multilogin_yggdrasilpath% | 玩家所在的 Yggdrasil 账户验证服务器的路径|

如果你在使用这个插件时有任何的疑问或建议，欢迎加入我们的 QQ
群互相讨论: [![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
