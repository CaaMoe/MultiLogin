## 指令和权限

### 补充说明

* `<**档案信息**>`表示`<profile_username|profile_uuid|profile_id>` 的简写, 各个参数意思:
    * `profile_username`: 档案名称(游戏内名称)
    * `profile_uuid`: 档案UUID(游戏内UUID)
    * `profile_id`: 档案唯一ID

* `<**在线信息**>`表示`<<service_id> <login_username|login_uuid>>|<login_id>` 的简写, 各个参数意思:
    * `service_id` 验证服务ID
    * `login_username` 登录名称
    * `login_uuid` 登录UUID
    * `login_id` 登录唯一ID

### 根指令

* `/multilogin reload`
    * 权限: `multilogin.command.reload`
    * 说明: 重载插件除了数据库外的所有配置

* `/multilogin list`
    * 权限: `multilogin.command.list`
    * 说明: 打印以 service 分组的在线玩家列表

* `/multilogin confirm`
* `/multilogin help`

### link指令

* `/multilogin link to <档案信息>`
    * 权限: `multilogin.command.link.to`
    * 说明: 向指定档案发起link请求
    * 参数:
        * `档案信息`: 要连接到的指定档案
    * 要求:
        * 玩家身份执行
        * 玩家通过`MultiLogin`登录游戏

* `/multilogin link accept <在线信息> <code>`
    * 权限: `multilogin.command.link.accept`
    * 说明: 同意link请求
    * 参数:
        * `在线信息`: 要同意的在线信息
        * `code`: 同意验证码
    * 要求:
        * 玩家身份执行
        * 玩家通过`MultiLogin`登录游戏

* `/multilogin unlink`
    * 权限: `multilogin.command.unlink`
    * 说明: 撤销当前登录的账号的link
    * 要求:
        * 玩家身份执行
        * 玩家通过`MultiLogin`登录游戏

* `/multilogin unlink <在线信息>`
    * 权限: `multilogin.command.unlink.other`
    * 说明:
        * 撤销当前登录档案中的link
        * 只会撤销当前指令执行者所使用的档案的 linker
    * 参数:
        * `在线信息`: 要撤销的在线信息
    * 要求:
        * 玩家身份执行
        * 玩家通过`MultiLogin`登录游戏

### profile指令

* `/multilogin profile info <档案信息>`
    * 权限: `multilogin.command.profile.info.other`
    * 说明: 打印指定档案所使用的档案信息
    * 参数:
        * `档案信息`: 指定档案信息

* `/multilogin profile create <profile_name>`
    * 权限: `multilogin.command.profile.create`
    * 说明:
        * 创建指定游戏内档案
        * 档案 UUID 将随机生成
    * 参数:
        * `profile_name`: 所需要创建的档案名字

* `/multilogin profile create <profile_name> <profile_uuid>`
    * 权限: `multilogin.command.profile.create.specific`
    * 说明: 创建指定游戏内档案
    * 参数:
        * `profile_name`: 所需要创建的档案名字
        * `profile_uuid`: 所需要创建的档案的 UUID
    * 要求:
        * 指定的 UUID 版本号需要大于等于 3

* `/multilogin profile rename <new_profile_name>`
    * 权限: `multilogin.command.profile.rename`
    * 说明: 修改当前指令执行者所使用的档案的档案名称
    * 参数:
        * `new_profile_name`: 新档案名称
    * 要求:
        * 玩家身份执行
        * 玩家通过`MultiLogin`登录游戏

* `/multilogin profile rename <new_profile_name> <档案信息>`
    * 权限: `multilogin.command.profile.rename.other`
    * 说明: 修改指定档案的档案名称
    * 参数:
        * `new_profile_name`: 新档案名称
        * `档案信息`: 要修改的档案信息

* `/multilogin profile link <档案信息>`
    * 权限: `multilogin.command.profile.link`
    * 说明: 强制当前登录账号连接到档案
    * 参数:
        * `档案信息`: 要连接的档案
    * 要求:
        * 玩家身份执行
        * 玩家通过`MultiLogin`登录游戏

* `/multilogin profile link <档案信息> <在线信息>`
    * 权限: `multilogin.command.profile.link.other`
    * 说明: 强制指定在线信息连接到档案
    * 参数:
        * `档案信息`: 要连接的档案
        * `在线信息`: 要创建连接的在线信息

* `/multilogin profile unlink`
* `/multilogin profile unlink <在线信息>`

### 查询指令
* `/multilogin info`
* `/multilogin find profile`
* `/multilogin find profile <档案信息>`
* `/multilogin find user`
* `/multilogin find user <在线信息>`
* `/multilogin find linker`
* `/multilogin find linker <档案信息>`
* `/multilogin find initer`
* `/multilogin find initer <档案信息>`
* `/multilogin find user`
* `/multilogin find user <在线信息>`

### 白名单指令

* `/multilogin whitelist addcache <login_name|login_uuid>`
* `/multilogin whitelist addcache <login_name|login_uuid> <service_id>`
* `/multilogin whitelist removecache <login_name|login_uuid>`
* `/multilogin whitelist removecache <login_name|login_uuid> <service_id>`
* `/multilogin whitelist add <在线信息>`
* `/multilogin whitelist remove <在线信息>`