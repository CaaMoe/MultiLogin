package moe.caa.multilogin.core.database.v4

import moe.caa.multilogin.api.data.IProfileData
import moe.caa.multilogin.api.data.IUserData
import moe.caa.multilogin.core.resource.configuration.service.BaseService
import java.util.*
import kotlin.properties.Delegates

// 这里不用刀是为了能更好的随时new和操作数据吧...

data class UnmodifiedProfileData(
    private val profileUuid: UUID,
    private val profileName: String, // 这里包含 profileName 和 lowercaseProfileName, 读只读 profileName, lowercaseProfileName 只作为忽略大小写的唯一约束
) : IProfileData {
    // 如果不是数据库读出来的话, 可以不写这个值
    var dataIndex by Delegates.notNull<Int>()

    override fun getProfileId() = dataIndex
    override fun getProfileUUID() = profileUuid
    override fun getProfileUsername() = profileName
}

data class UnmodifiedUserData(
    private val service: BaseService?,
    private val loginUuid: UUID,
    private val loginName: String,
    private val whitelist: Boolean,
    private val initialProfile: UnmodifiedProfileData,  // 初始档案不可修改!
    private val linkToProfile: UnmodifiedProfileData
) : IUserData {
    var dataIndex by Delegates.notNull<Int>()

    override fun getUserId() = dataIndex
    override fun getService() = service
    override fun getLoginUUID() = loginUuid
    override fun getLoginUsername() = loginName
    override fun hasWhitelist() = whitelist
    override fun getInitialProfileData() = initialProfile
    override fun getLinkToProfileData() = linkToProfile
}