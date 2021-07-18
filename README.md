# MultiLogin
[![GitHub license](https://img.shields.io/github/license/CaaMoe/MultiLogin?style=flat-square)](https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE)

让服务端同时支持多种外置登入共存

## 功能

* 多 Yggdrasil 共存
* 可控制玩家在游戏内的 UUID
* 支持 Yggdrasil 分组管理的白名单系统
* 阻止服务器出现同名账户
* 防止出现抢注服务器内 ID 的情况
* 账户安全保护机制

## 如何使用

### 安装

插件理论上支持 Spigot 1.8+ 和 大多数 Bungee 服务端，
与同类型程序相比，MultiLogin 不需要修改或添加任何服务端启动参数，
将适合的插件本体丢入 plugins 文件夹下即可使用。

### 添加一个 Yggdrasil 账户验证服务器

插件现在支持添加几乎所有类型的 Yggdrasil 账户验证服务器，具体设置方法如下：

#### 模板：

    # 节点名称作为插件内标记名称，一旦设置请谨慎修改它。
    # 该标记名称作为系统识别 Yggdrasil 验证服务器的依据。胡乱更改会导致系统无法验证用户身份而被拒绝用户登入游戏。
    # 您可以仿写此节点用来添加新的 Yggdrasil 验证服务器。
    demo:

      # 启用该验证服务器。
      # 关闭它将拒绝所有使用该 Yggdrasil 账户验证登入的玩家登入游戏。
      enable: true

      # 该验证服务器的别称，作为 Yggdrasil 账户验证服务器的 display
      name: 'demo yggdrasil service'

      # Yggdrasil 验证服务器的基本属性设置。
      body:

        # 服务器类型，
        # 目前只支持以下值:
        # BLESSING_SKIN                            // 表示 Blessing skin 的 Yggdrasil 验证服务器
        # MINECRAFT （仅能出现一次）                  // 表示 Minecraft 原版验证类型服务器
        # CUSTOM                                   // 其他类型验证服务器
        serverType: CUSTOM

        # Yggdrasil 验证服务器服务端 hasJoin 部分验证请求链接设置。
        # 当 serverType 为 ‘MINECRAFT’ 时，此节点无需设置。
        # 当 serverType 为 ‘BLESSING_SKIN’ 时，只需要指定 Yggdrasil api 地址即可（比如 LittleSkin 的 Yggdrasil api 地址为：‘https://mcskin.littleservice.cn/api/yggdrasil’）。
        # 当 serverType 为 ‘CUSTOM’时，必须指定完整的请求链接，其中 GET 请求中的变量 {0} 表示 username ，变量 {1} 表示 serverId ，变量 {2} 表示 passIpContent 节点所设置内容。
        #       （比如：‘https://example.com/yggdrasil/session/hasJoined?username={0}&serverId={1}{2}’ ，若是POST请求，则需填写完整的请求链接即可）。
        url: "https://example.com/yggdrasil/session/hasJoined?username={0}&serverId={1}{2}"

        # 以 post 请求方式验证账户身份
        # 绝大多数下， Yggdrasil 验证服务器都使用 GET 请求方式验证用户数据的，
        # 如果你不了解当前 Yggdrasil 验证流程的话，请勿擅自修改它。
        # 仅当 serverType 节点值为 ‘CUSTOM’ 时，此节点才有效。
        postMode: false

        # hasJoin 阶段是否传递用户 IP 信息，如果存在。
        # 若不了解当前 Yggdrasil 验证流程的话，请勿擅自修改它。
        passIp: true

        # 设置 url 节点 {2} 变量内容，
        # 其中，变量 {0} 为所获取到的 IP 信息。
        # 仅当 passIp 节点为 true 时此节点有效。
        passIpContent: "ip={0}"

        # 设置 post 的请求内容。
        # 仅当 postMode 节点值为 true 时，此节点才有效。
        # 其中 GET 请求中的变量 {0} 表示 username ，变量 {1} 表示 serverId ，变量 {2} 表示 passIpContentByPost 节点所设置内容
        postContent: '{"username":"{0}", "serverId":"{1}"}'

        # 设置 url 节点 {2} 变量内容，
        # 仅当 passIp 节点为 true 时此节点有效
        passIpContentByPost: '"ip":"{0}"'

      # 是否通过发送假请求识别插件内 Yggdrasil 账户验证服务器配置是否正确.
      checkUrl: true

      # 当某名玩家第一次在此节点所设置的验证服务器中验证通过后，游戏内的 UUID 会按照此节点所填写的规则来生成：
      # 目前只支持以下值:
      #   DEFAULT   - 使用 Yggdrasil 验证服务器提供的UUID
      #   OFFLINE   - 自动生成离线 UUID（盗版UUID）
      #   RANDOM    - 随机 UUID
      # 请谨慎设置和修改此节点，避免增加日后维护的心智负担。
      # 为避免触发 Paper 端的某些机制，强烈建议此值为 DEFAULT
      convUuid: DEFAULT

      # 当某名玩家第一次登入成功后，若通过节点 ‘convUuid’ 所生成的 UUID 已被使用时，则自动修正为随机的 UUID 避免数据错乱。
      # 请尽量保持此值为 true ，除非您了解后果是什么（虽然几率很小）。
      convRepeat: true

      # 玩家允许设置的用户名正则，不匹配将会拒绝登入请求。
      nameAllowedRegular: '^[0-9a-zA-Z_]{2,10}$'

      # 是否为当前验证服务器单独设置白名单。
      # 当全局白名单为true时，此节点无效。
      whitelist: true

      # 验证错误重试次数
      authRetry: 1

#### 例子
* 添加 Minecraft 原版验证服务器

      official:
        body:
  	
  	      # 这里需填写 MINECRAFT 类型
          serverType: MINECRAFT
  	  
  	      # hasJoin 阶段是否传递用户 IP 信息，如果存在。
  	      passIp: false

* 添加 Blessing Skin 类型验证服务器

      blessingSkin:
        body:
  	
  	      # 这里需填写 BLESSING_SKIN 类型
          serverType: BLESSING_SKIN
  	
  	      # 这里需要填写的是 Blessing Skin 提供的 Yggdrasil api 地址
  	      url: "https://example.com/api/yggdrasil"

* 以特殊的方式添加 Minecraft 原版验证服务器

      officialCustom:
        body:
    
          # 这里需填写 CUSTOM 类型
          serverType: CUSTOM
  	  
  	      # 这是原版验证服务器 HasJoin 阶段向 Yggdrasil 账户验证服务器所请求的 URL
  	      # 其中，变量 {0} 表示 username ，变量 {1} 表示 serverId ，变量 {2} 表示 passIpContentByPost 节点所设置的内容
  	      url: "https://sessionserver.mojang.com/session/minecraft/hasJoined?username={0}&serverId={1}{2}"
  	
  	      # 由于原版账户验证服务器 HasJoin 阶段是采用 HTTP GET 请求来验证用户登入消息的，所以
  	      # 此值应为 false
  	      postMode: false
  	      
  	      # hasJoin 阶段是否传递用户 IP 信息，如果存在。
  	      passIp: true
  	  
  	      # 原版验证服务器中 url 占位符 {2} 内容为 ‘ip={IP}’
  	      # 所以此值应为 ‘ip={0}’ ，其中，变量 {0} 表示 ip 
          passIpContent: "ip={0}"

* 以特殊的方式添加 Blessing Skin 类型验证服务器

      blessingSkinCustom:
        body:
    
          # 这里需填写 CUSTOM 类型
          serverType: CUSTOM
  	  
  	      # 假如当前 Blessing Skin 的 Yggdrasil api 地址为 {0} ，则 url 值应该为 ‘{0}/sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}’
  	      # 其中，变量 {0} 表示 username ，变量 {1} 表示 serverId ，变量 {2} 表示 passIpContentByPost 节点所设置的内容
  	      url: "{0}/sessionserver/session/minecraft/hasJoined?username={0}&serverId={1}{2}"
  	
  	      # 由于 Blessing Skin 的 Yggdrasil 账户验证服务器 HasJoin 阶段是采用 HTTP GET 请求来验证用户登入消息的，所以
  	      # 此值应为 false
  	      postMode: false
  	      
  	      # hasJoin 阶段是否传递用户 IP 信息，如果存在。
  	      passIp: true
  	      
  	      # Blessing Skin 的 Yggdrasil 账户验证服务器中 url 占位符 {2} 内容为 ‘ip={IP}’
  	      # 所以此值应为 ‘ip={0}’ ，其中，变量 {0} 表示 ip 
          passIpContent: "ip={0}"

如果你在使用这个插件时有任何的疑问或建议，欢迎加入 QQ 群讨论：832210691