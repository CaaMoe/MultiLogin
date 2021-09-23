# MultiLogin

[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)
[![GitHub license](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)

让你的服务端支持正版登入和多种外置登入共存。

    这是 MultiLogin 的第三次正在重写的分支 R2 (暂不可使用)

## 功能特性

* 多 Yggdrasil 共存
* 高度可设置 Yggdrasil 方式，支持市面上几乎所有类型的 Yggdrasil 服务器
* 账户安全机制
    * 账户安全核查
        * 限制只使用一种 Yggdrasil 登入方式，杜绝可能出现的重复 UUID 问题
        * 可设置占线登入方式
    * 用户名核查
        * 阻止服务器内出现同名账户
        * 可设置部分账户验证服务器下的玩家跳过重名检查并进行占线登入
        * 多 Yggdrasil 下分组设置的用户名正则匹配检查
    * 支持多 Yggdrasil 下分组管理的白名程序
* 可控制生成玩家在游戏内的 UUID
* PlaceholderAPI 变量支持 (Bukkit)
* 自动修复多 Yggdrasil 情况下出现的皮肤问题 (即将到来)

## BStats 数据

![BStats](https://bstats.org/signatures/bukkit/MultiLoginR.svg)

## 关于混合验证登入的安全性问题

混合验证登入的潜在问题就是有可能存在两个同名甚至同 UUID 的玩家，一旦出现这种情况可能会出现数据混乱、命令选择器失效等致命问题，及难排查。

MultiLogin 能很好的处理角色重名和 UUID 重复的问题，理论上不会存在什么安全性的问题。

## 如何使用

### 安装

与同类型程序相比，MultiLogin 不需要修改或添加任何服务端启动参数（不需要安装任何 authlib-injector 以及同类型前置 javaagent 程序）， 只需将适合服务端的插件本体丢入 plugins(mods) 文件夹下即可。

### 添加一个 Yggdrasil 账户验证服务器

插件现在支持添加几乎所有类型的 Yggdrasil 账户验证服务器，具体设置方法如下：

#### 模板：

    # 这是一个示例配置：
    # 节点名称 'demo' 作为混合验证系统识别 Yggdrasil 验证服务器唯一性的依据，一旦设置请不要随意的去改动它，
    # 胡乱更改节点名称将会导致系统无法验证用户身份而被拒绝用户登入请求。
    # 您可以仿写此节点用来添加新的 Yggdrasil 账户验证服务器。
    demo:
  
      # 启用该验证服务器。
      # 值为 'false' 时将不添加该 Yggdrasil 账户验证服务器到混合登入系统中去，将拒绝一切使用该 Yggdrasil 账户验证服务器的球员登入游戏。
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
        # 其中 GET 请求中的占位变量 {0} 或 {username} 表示 username，占位变量 {1} 或 {serverId} 表示 serverId，
        #        占位变量 {2} 或 {passIpContent} 表示 passIpContent 节点所设置的内容。
        # 请求的 URL 对大小写非常敏感，设置需谨慎！
        # 一些例子 "https://example.com/session/minecraft/hasJoined?username={username}&serverId={serverId}"
        # 必填值！
        url: "https://example.com/session/minecraft/hasJoined?username={username}&serverId={serverId}{passIpContent}"
  
        # hasJoined 阶段使用 POST（报文） 请求的方式验证账户身份。
        # 绝大多数下， Yggdrasil 账户验证服务器 hasJoined 阶段都采用 GET 请求方式验证用户数据的，
        # 如果你不了解当前 Yggdrasil 验证流程的话，请勿擅自改动它。
        # 默认值 'false'
        postMode: false
  
        # hasJoined 阶段是否向 Yggdrasil 账户验证服务器传递用户 IP 信息，如果存在。
        # 默认值 'false'
        passIp: false
  
        # 设置 url 节点 {2} 或 {passIpContent} 变量内容，
        # 其中，变量 {0} 或 {ip} 为所获取到的 IP 信息。
        # 仅当 passIp 节点为 true 时此节点有效。
        # 请求的 URL 对大小写非常敏感，设置需谨慎！
        # 一些例子 ', "ip": "{ip}"'、 '&ip={ip}'
        # 默认值 '&ip={ip}'
        passIpContent: "&ip={ip}"
  
        # 设置 post 的请求内容。
        # 其中 POST 请求中的占位变量 {0} 或 {username} 表示 username，占位变量 {1} 或 {serverId} 表示 serverId，
        #        占位变量 {2} 或 {passIpContent} 表示 passIpContent 节点所设置的内容。
        # 请求的 URL 对大小写非常敏感，设置需谨慎！
        # 一些例子 '{"username":"{username}", "serverId":"{serverId}"{passIpContent}}'、 '{"username":"{username}", "serverId":"{serverId}"}'
        # 默认值 '{"username":"{username}", "serverId":"{serverId}"}'
        postContent: '{"username":"{username}", "serverId":"{serverId}"}'
  
      # 当玩家首次登入且使用当前 Yggdrasil 账户验证服务器验明身份后，它的游戏内 UUID 生成规则设置：
      # 目前只支持以下值:
      #   DEFAULT   - 使用 Yggdrasil 账户证服务器提供的在线 UUID
      #   OFFLINE   - 自动生成离线 UUID（盗版UUID）
      #   RANDOM    - 随机 UUID
      # 请谨慎设置和修改此节点，避免增加日后维护的心智负担。
      # 为避免触发 Paper 端的某些机制，强烈建议此值为 DEFAULT
      # 默认值 'DEFAULT'
      convUuid: DEFAULT
  
      # 是否当首次登入的玩家生成的游戏内 UUID 已存在，自动更正到随机的 UUID。
      # 请尽量保持此值为 true ，除非您了解后果是什么（虽然几率很小）。
      # 默认值 'true'
      convRepeat: true
  
      # 当前 Yggdrasil 账户验证服务器下的玩家允许设置的用户名正则设置。
      # 若不匹配将会拒绝当前玩家的登入请求
      # 如果此值留空或未设置，则应用根节点下 'nameAllowedRegular' 节点值。
      # 默认值 ''
      nameAllowedRegular: ''
  
      # 是否为当前验证服务器单独开启白名单。
      # 当全局白名单为 true 时，此节点无效。
      # 默认值: false
      whitelist: false
  
      # 当玩家登入时，服务器中已有 相同游戏内 UUID 的情况是否拒绝玩家登入。
      # 若为 false 将把游戏内相同 UUID 的玩家踢出，允许验证中的玩家登入（原版默认）
      # 若为 true 则拒绝当前的登入请求
      # 默认值: false
      refuseRepeatedLogin: false
  
      # 验证错误重试次数
      # 默认值: 1
      authRetry: 1
  
      # 当前 Yggdrasil 账户验证服务器是否具有绝对的 ID 使用、更换权限。
      # 若为 'true', 其他 Yggdrasil 账户验证服务器下的玩家都不能在服务器内抢夺和使用此验证服务器下玩家的 ID 使用权限。
      # 默认值 'false'
      safeId: false

#### 例子

* 添加 Minecraft 原版验证服务器

      officialCustom:
        name: "正版"
        body:
          url: "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={0}&serverId={1}{2}"


* 添加 Blessing Skin 类型验证服务器

      blessingSkinCustom:
        name: "Blessing Skin"
        body:
  	  
            # 假设当前 Blessing Skin 的 Yggdrasil api 地址为 {url}
            url: "{url}/sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}"

* 添加 统一通行证 账户验证服务器

      blessingSkinCustom:
        name: "统一通行证"
        body:
  	  
            # 假设当前服务器 UID 为 {uid}
            url: "https://auth2.nide8.com:233/{uid}/sessionserver/session/minecraft/hasJoined?serverId={1}&username={0}"

## 命令和权限

| 命令 | 权限 | 简介 |
|  ----  | ----  | --- |
| /multilogin reload   | | 重新加载配置文件|
| /multilogin query name &lt;name&gt; | | 查询 name 所属用户信息（可能多个）|
| /multilogin query redirectUuid &lt;UUID&gt; | | 查询 redirectUuid 所属用户信息（可能多个）|
| /multilogin query onlineUuid &lt;UUID&gt;     | |查询 onlineUuid 所属用户信息 |
| /multilogin query yggdrasil &lt;path&gt;     | |查询 path 所属 Yggdrasil 账户验证服务器信息 |
| /multilogin update     | | 检查更新|
| /multilogin list | | 查看具有 Yggdrasil 登入信息的在线玩家列表|
| /multilogin remove &lt;onlineUuid&gt;     | | 移除一条记录|
|/multilogin confirm    | | 确认当前风险操作|
| /whitelist add &lt;name&#124;uuid&gt;     | | 将 target 添加到白名单中|
| /whitelist remove &lt;name&#124;uuid&gt;    | | 移除 target 的白名单|
| /whitelist have &lt;name&#124;uuid&gt;         | | 判断 target 是否具有白名单|
| /whitelist list         | | 获得白名单列表|

如果你在使用这个插件时有任何的疑问或建议，欢迎加入我们的 QQ
群互相讨论: [![GitHub license](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
