package moe.caa.multilogin.core.database.v4

import java.util.*
import kotlin.properties.Delegates

// 这里不用刀是为了能更好的随时new和操作数据吧...

data class ProfileData(
    val profileUuid: UUID,
    var profileName: String, // 这里包含 profileName 和 lowercaseProfileName, 读只读 profileName, lowercaseProfileName 只作为忽略大小写的唯一约束
){
    // 如果不是数据库读出来的话, 可以不写这个值
    var dataIndex by Delegates.notNull<Int>()
}

data class UserData(
    val serviceId: Int,
    val loginUuid: UUID,
    var loginName: String,
    var whitelist: Boolean,
    val initialProfileId: Int,  // 初始档案不可修改!
    var linkToProfileId: Int
){
    var dataIndex by Delegates.notNull<Int>()
}