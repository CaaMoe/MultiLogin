# MultiLogin

这是一款高兼容性的外置登入插件，理论上支持Spigot 1.8+版本和大多数Bungee版本。

该插件通过使用反射修改Yggdrasil验证过程从而达到实现多Yggdrasil共存的效果。

该插件最大的好处就是 完全不需要去对启动服务器的命令行进行修改 只需要放在plugins内就可以使用

## 功能

截止目前，该插件有下列几项功能：

* 支持Bungee
* 多Yggdrasil共存
* 修复多Yggdrasil下存在的皮肤显示问题
* 设置游戏内UUID
* 高级Yggdrasil白名单系统

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

插件现在支持添加几乎所有类型的Yggdrasil服务器，设置它也是个很简单的过程。

它的配置格式为

    # 节点名称为插件标记名称，一旦设置请谨慎修改。
    # 该标记作为系统识别用户验证服务器的依据。胡乱更改会导致系统无法验证用户身份而被拒绝登入请求。
    # 您可以仿写此节点用来添加新的Yggdrasil验证服务器。
    demo:

        # 启用该验证服务器
        # 关闭将拒绝所有使用该验证登入的玩家登入游戏
        enable: false
    
        # Yggdrasil服务器的基本属性设置
        body:
    
          # 服务器类型，只能是以下值
          # BLESSING_SKIN                     // 表示Blessing skin验证
          # MINECRAFT （仅能出现一次）         // 表示正版验证
          # CUSTOM                           // 自定义验证
          serverType: MINECRAFT
    
          # 验证服务器的别称。
          # 必填，不允许为空字符串。
          name: "Deom Yggdrasil"
    
          # Yggdrasil验证请求链接
          # 当serverType为‘MINECRAFT’时，此节点无效
          # 当serverType为‘BLESSING_SKIN’时，只需要指定Yggdrasil api地址即可（比如LittleSkin的URL为：‘https://mcskin.littleservice.cn/api/yggdrasil’）
          # 当serverType为‘CUSTOM’时，必须指定完整的请求链接（比如：‘https://example.com/yggdrasil/session/hasJoined?username=%s&serverId=%s’， 若是POST请求，则填写完整的请求链接）
          url: ""
    
          # 以post请求方式验证账户身份
          # 绝大多数下，Yggdrasil服务器都使用GET请求方式验证用户数据的，
          # 如果你不了解当前Yggdrasil验证流程的话，请勿擅自修改它
          # 仅当serverType节点值为‘CUSTOM’时，此节点才有效
          postMode: false
    
          # 设置post的请求内容
          # 仅当postMode节点值为true时，此节点才有效
          postContent: '{"username":"{0}", "serverId":"{1}"}'
    
        # 配置加载时是否通过发送假请求识别Yggdrasil配置是否正确.
        checkUrl: false
    
        # 当某名玩家首次在此节点所设置的验证服务器中验证通过后，游戏内的UUID会按照此节点所填写的规则来生成
        # 目前只支持以下值:
        #   DEFAULT   -使用Yggdrasil验证服务器提供的UUID
        #   OFFLINE   -自动生成离线UUID（盗版UUID）
        # 请谨慎设置和修改该节点，避免增加日后维护的心智负担。
        # 必填，不允许为无效的值。
        # 为避免触发Paper端的某些机制，强烈建议此值为 DEFAULT
        convUuid: DEFAULT
    
        # 是否为当前验证服务器单独设置白名单。
        # 当全局白名单为true时，此节点无效。
        whitelist: true
    
        # 设置当前验证服务器是否启用皮肤修复功能
        # 仅当serverType节点值不为‘MINECRAFT’时，此节点才有效
        skinRepair: false
    
        # 设置当前验证服务器皮肤修复错误重试次数
        # 仅当skinRepair节点值为true时，此节点才有效
        skinRepairRetry: 3
    
        # 验证错误重试次数
        authRetry: 1


在添加一个Yggdrasil之前，我们需要知道验证服务器的完整的验证链接。

#### 例子 1
 官方的验证链接为`https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s` ，配置文件值为：

    body:
        serverType: MINECRAFT

#### 例子 2
 littleSkin.cn 的验证链接为`https://mcskin.littleservice.cn/api/yggdrasil/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s` ，配置文件值为：

    body:
        serverType: BLESSING_SKIN
        url: "https://mcskin.littleservice.cn/api/yggdrasil"


#### 其他例子

一般的，所有以`https://github.com/yushijinhun/authlib-injector/wiki` 服务端技术规范设计的Yggdrasil验证服务器都可以设置为：

    body:
        serverType: BLESSING_SKIN
        url: "https://example.cn/api/yggdrasil"

国内几乎所有的Yggdrasil验证服务器都是以它设计的，其他高级节点的设计是为了让插件兼容更多的、奇怪的Yggdrasil验证服务器。
如果没有特别需求的话，尽量使用节点的默认值

## 

如果你在使用这个插件时有任何的疑问或建议，欢迎加入Q群互相讨论：832210691
