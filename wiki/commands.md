## 基础指令

* `/multilogin about`
    * 权限: `multilogin.command.about`
    * 描述:  输出插件信息

* `/multilogin reload`
    * 权限: `multilogin.command.reload`
    * 描述:  重载插件配置和语言文件

* `/multilogin me`
    * 权限: `multilogin.command.me`
    * 描述:  查询自己的信息

* `/multilogin info <target>`
    * 权限: `multilogin.command.info`
    * 描述:  查询指定玩家的信息
        * 参数:
            * `<target>` 必填参数, 指定的代理在线玩家

* `/multilogin list`
    * 权限 `multilogin.command.list`     
    * 描述: 获取当前服务器所有在线玩家集合并且以 service 进行分组展示.
  *
* `/multilogin confirm`
    * 权限 `multilogin.command.confirm`
    * 描述: 风险操作确认

## 白名单指令

* `/multilogin whitelist addCache [service] <name_or_uuid>`
    * 权限: `multilogin.command.whitelist.add.cache`
    * 描述: `添加缓存白名单, 当缓存白名单匹配到任何一个角色时自动删除并且自动给予当前角色永久白名单`
    * 参数:
        * `<name_or_uuid>` 必填参数, 可填角色名称或者uuid
        * `[service]` 可选参数, 填写角色来源验证服务id, 不填则匹配所有服务

* `/multilogin whitelist addSpecific <service> <name_or_uuid>`
    * 权限: `multilogin.command.whitelist.add.specific`
    * 描述: `给指定角色添加永久白名单`
    * 参数:
        * `<name_or_uuid>` 必填参数, 可填角色名称或者uuid
        * `<service>` 必填参数, 角色的来源验证服务id

* `/multilogin whitelist removeCache [service] <name_or_uuid>`
    * 权限: `multilogin.command.whitelist.remove.cache`
    * 描述: `删除缓存白名单`
    * 参数:
        * `<name_or_uuid>` 必填参数, 可填角色名称或者uuid
        * `[service]` 可选参数, 填写角色来源验证服务id, 不填则匹配所有服务

* `/multilogin whitelist removeSpecific <service> <name_or_uuid>`
    * 权限: `multilogin.command.whitelist.remove.specific`
    * 描述: `删除指定角色的永久白名单`
    * 参数:
        * `<name_or_uuid>` 必填参数, 可填角色名称或者uuid
        * `<service>` 必填参数, 角色的来源验证服务id

* `/multilogin whitelist clearCache`
    * 权限: `multilogin.command.whitelist.clear.cache`
    * 描述: `清空缓存白名单`

## 档案操作指令

* `/multilogin profile create <profile_name> [profile_uuid]`
    * 权限: `multilogin.command.profile.create`
    * 描述: `创建一份游戏档案`
    * 参数:
        * `<profile_name>` 必填参数, 档案游戏名称
        * `<profile_uuid>` 选填参数, 角档案uuid


* `/multilogin profile rename <new_profile_name> <target_profile_name>`
    * 权限: `multilogin.command.profile.rename`
    * 描述: `重命名游戏档案`
    * 参数:
        * `<new_profile_name>` 必填参数, 新的游戏档案名称
        * `<target_profile_name>` 必填参数, 需要更改的游戏档案名称


* `/multilogin profile info <profile_name_or_profile_uuid>`
    * 权限: `multilogin.command.profile.info`
    * 描述: `查询指定游戏档案信息`
    * 参数:
        * `<profile_name_or_profile_uuid>` 必填参数, 游戏档案名称或者uuid

## 角色操作指令

* `/multilogin user info <service_id> <user_name_or_user_uuid>`
    * 权限: `multilogin.command.user.info`
    * 描述: `查询指定角色信息`
    * 参数:
        * `<service_id>` 必填参数, 角色来源服务id
        * `<user_name_or_user_uuid>` 必填参数, 角色名称或者uuid

    
* `/multilogin user setProfile <profile_name_or_profile_uuid> <service_id> <user_name_or_user_uuid>`
    * 权限: `multilogin.command.user.ser.profile`
    * 描述: `设置角色的游戏档案`
    * 参数:
        * `<profile_name_or_profile_uuid>` 必填参数, 游戏档案名称或者uuid
        * `<service_id>` 必填参数, 角色来源服务id
        * `<user_name_or_user_uuid>` 必填参数, 角色名称或者uuid

## LINK相关指令

* `/multilogin link to <profile_name_or_profile_uuid>`
    * 权限: `multilogin.command.link.to`
    * 描述: `向指定档案创建link请求`
    * 参数:
        * `<profile_name_or_profile_uuid>` 必填参数, 游戏档案名称或者uuid


* `/multilogin link accept <service_id> <user_name_or_user_uuid> <verify_code>`
    * 权限: `multilogin.command.link.accept`
    * 描述: `同意目标角色的link请求`
    * 参数:
        * `<service_id>` 必填参数, 角色来源服务id
        * `<user_name_or_user_uuid>` 必填参数, 角色名称或者uuid
        * `<verify_code>` 必填参数, Link 校验码



