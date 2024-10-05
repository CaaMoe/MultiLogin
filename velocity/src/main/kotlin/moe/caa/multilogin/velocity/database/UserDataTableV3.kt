package moe.caa.multilogin.velocity.database

import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
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
    val inGameProfileUUID: Column<UUID?> = uuid("in_game_profile_uuid").nullable()

    // 角色是否具有白名单
    var whitelist: Column<Boolean> = bool("whitelist").default(false)

    override val primaryKey = PrimaryKey(onlineUUID, serviceId)
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

/**
 * 缓冲白名单表, 存放缓冲白名单用
 */
object CacheWhitelistTableV2 : IntIdTable("multilogin_cache_whitelist_v2") {
    // 白名单目标给谁, 限制 service Id
    val serviceId: Column<Int> = integer("service_id")

    // 白名单目标给谁
    val target: Column<String> = varchar("target", 64)

    init {
        uniqueIndex(serviceId, target)
    }
}