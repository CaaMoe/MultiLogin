package moe.caa.multilogin.velocity.database

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*


/**
 * 用户表, 存放每个在线角色的信息
 */
object UserDataTableV3 : CompositeIdTable("multilogin_user_data_v3") {
    // 角色的在线 UUID
    val onlineUUID: Column<UUID> = uuid("online_uuid")

    // 角色使用的验证服务ID
    val serviceId: Column<Int> = integer("service_id")

    // 角色的在线用户名
    val onlineName: Column<String> = varchar("online_name", 64).index()

    // 角色分配到的游戏内档案
    val inGameProfileUUID: Column<UUID> = uuid("in_game_profile_uuid")

    // 角色是否具有白名单
    val whitelist: Column<Boolean> = bool("whitelist")

    override val primaryKey = PrimaryKey(onlineUUID, serviceId)
}

class UserDataEntry(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<UserDataEntry>(UserDataTableV3)

    // 角色的在线 UUID
    val onlineUUID by UserDataTableV3.onlineUUID

    // 角色使用的验证服务ID
    val serviceId by UserDataTableV3.serviceId

    // 角色的在线用户名
    var onlineName by UserDataTableV3.onlineName

    // 角色分配到的游戏内档案
    var inGameProfileUUID by UserDataTableV3.inGameProfileUUID

    // 角色是否具有白名单
    var whitelist by UserDataTableV3.whitelist
}


/**
 * 档案表, 存放每个游戏内档案的信息
 */
object ProfileTableV3 : IdTable<UUID>("multilogin_in_game_profile_v3") {
    // 档案的 UUID
    override val id: Column<EntityID<UUID>> = uuid("in_game_uuid").entityId()

    // 档案所使用的名称的小写
    val currentUserNameLowerCase: Column<String> = varchar("current_username_lower_case", 64).uniqueIndex()

    // 原始档案名称
    val currentUserNameOriginal: Column<String> = varchar("current_username_original", 64)
    override val primaryKey = PrimaryKey(id)
}

class ProfileAuthenticationEntry(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileAuthenticationEntry>(ProfileAuthenticationTableV1)

    // 档案所使用的密码
    val password by ProfileAuthenticationTableV1.password

    // 档案所使用的密码盐值
    val salt by ProfileAuthenticationTableV1.salt

    // 档案所使用的totp验证器信息
    val totp by ProfileAuthenticationTableV1.totp
}


/**
 * 档案密码表, 存放每个游戏内档案的密码、totp信息
 */
object ProfileAuthenticationTableV1 : IdTable<UUID>("multilogin_in_game_profile_authentication_v1") {
    // 档案的 UUID
    override val id: Column<EntityID<UUID>> = uuid("profile_uuid").entityId()

    // 档案所使用的密码
    val password: Column<String?> = varchar("password", 64).nullable()

    // 档案所使用的密码盐值
    val salt: Column<String?> = varchar("salt", 64).nullable()

    // 档案所使用的totp验证器信息
    val totp: Column<String?> = varchar("totp", 64).nullable()
    override val primaryKey = PrimaryKey(id)
}

class ProfileEntry(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileEntry>(ProfileTableV3)

    // 档案所使用的名称的小写
    private var currentUserNameLowerCase by ProfileTableV3.currentUserNameLowerCase

    // 原始档案名称
    private var currentUserNameOriginal by ProfileTableV3.currentUserNameOriginal

    fun getUsername() = currentUserNameOriginal

    fun setUsername(username: String) {
        currentUserNameLowerCase = username.lowercase()
        currentUserNameOriginal = username
    }
}


/**
 * 登录日志库, 记录档案的登录日志
 */
object ProfileLoginLogTableV1 : IntIdTable("multilogin_in_game_profile_login_v1") {
    // 档案的 UUID
    val profileUUID: Column<UUID> = uuid("profile_uuid").index()

    // 用户的 UUID
    val userUUID: Column<UUID> = uuid("user_uuid")

    // 用户使用的验证服务 ID
    val userServiceId: Column<Int> = integer("user_service_id")

    // 登录时间
    val loginDateTime: Column<LocalDateTime> = datetime("login_date_time")

    // 登录IP
    val loginIp: Column<String> = varchar("login_ip", 64)
}

class ProfileLoginLogEntry(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProfileLoginLogEntry>(ProfileLoginLogTableV1)

    // 用户的 UUID
    val userUUID by ProfileLoginLogTableV1.userUUID

    // 用户使用的验证服务 ID
    val userServiceId by ProfileLoginLogTableV1.userServiceId

    // 登录时间
    val loginDateTime by ProfileLoginLogTableV1.loginDateTime

    // 登录IP
    val loginIp by ProfileLoginLogTableV1.loginIp
}