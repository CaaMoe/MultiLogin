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

插件现在支持添加几乎所有类型的Yggdrasil服务器，设置它也是个很简单的过程。

它的配置格式为

    # 节点名称为插件标记名称，一旦设置请谨慎修改。
    # 该标记作为系统识别用户验证服务器的依据。胡乱更改会导致系统无法验证用户身份而被拒绝登入请求。
    demo:

        # 启用该验证服务器
        # 关闭将拒绝所有使用该验证登入的玩家登入游戏
        enable: false

        # 验证服务器的别称。
        # 必填，不允许为空字符串。
        name: "yggdrasil name"

        # 验证服务器地址。
        # 必填，不允许为空字符串，URL末尾不需要'/'。
        # 关于他的详细解释，请查看noUrlDeal节点注释
        url: "https://example.com/yggdrasil"

        # 是否使用”https://github.com/yushijinhun/authlib-injector/wiki“api标准来检查一个链接是否有效
        checkUrl: true

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

        # 以post请求方式验证账户身份
        # 绝大多数下，Yggdrasil服务器都使用GET请求方式验证用户数据的，
        # 如果你不了解当前Yggdrasil验证流程的话，请勿擅自修改它
        postMode: false

        # 不对URL进行自动补充，
        # 补充的值为‘/sessionserver/session/minecraft’
        #
        # 若此Yggdrasil验证服务器的验证链接并不是以’/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s%s‘结尾的.
        # 请将此值设置为true，并将完整的验证链接填入节点url中（url不包含hasJoined?username=%s&serverId=%s%s）
        # 例子1：
        #    你的链接为：'https://example.com/yggdrasil/session/minecraft/hasJoined?username=%s&serverId=%s%s'
        #    此节点设置为true，设置url为‘https://example.com/yggdrasil/session/minecraft’
        #
        # 例子2:
        #   你的链接为：'https://example.com/yggdrasil/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s%s'
        #   此节点设置为false, 设置url为‘https://a.com/yggdrasil’
        noUrlDeal: false

        # 自定义head内容
        # 若此Yggdrasil验证服务器的最终链接参数头部分并不是’hasJoined?‘而是’hasJoinserver?‘或其他值，请将该值填入此节点中
        #
        # 例子1:
        #   你的链接为：'https://example.com/yggdrasil/session/minecraft/hasJoined?username=%s&serverId=%s%s'
        #   此节点设置为 'hasJoined?'.
        #
        # 例子2:
        #   你的链接为：'https://example.com/yggdrasil/session/minecraft/hasJoinserver?username=%s&serverId=%s%s'
        #   此节点设置为 'hasJoinserver?'.
        head: "hasJoined?"

在添加一个Yggdrasil之前，我们需要知道验证服务器的完整的验证链接。

#### 例子 1
 官方的验证链接为`https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s%s` ，它的配置关键值为

    # 验证链接，详见noUrlDeal节点注释
    url: "https://sessionserver.mojang.com/session/minecraft"

    # 官方并没有‘https://github.com/yushijinhun/authlib-injector/wiki’所定义的公共api，检查并不会通过
    checkUrl: false

    # 验证方式不为POST请求
    postMode: false

    # 验证链接并不是以‘/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s%s‘结尾的
    # 所以将‘https://sessionserver.mojang.com/session/minecraft’写入到url中并且设置此值为true
    noUrlDeal: true

    # head内容为‘hasJoined?’
    head: "hasJoined?"

#### 例子 2
 littleSkin.cn 的验证链接为`https://mcskin.littleservice.cn/api/yggdrasil/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s%s` ，它的配置关键值为

    # 验证链接，详见noUrlDeal节点注释
    url: "https://mcskin.littleservice.cn/api/yggdrasil"

    # 该验证服务器是以‘https://github.com/yushijinhun/authlib-injector/wiki’为标准设计的，检查理论通过
    checkUrl: true

    # 验证方式不为POST请求
    postMode: false

    # 验证链接是以‘/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s%s‘结尾的
    # 所以将‘https://mcskin.littleservice.cn/api/yggdrasil’写入到url中并且设置此值为false
    noUrlDeal: false

    # head内容为‘hasJoined?’
    head: "hasJoined?"