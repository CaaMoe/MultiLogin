package moe.caa.multilogin.core.database.v4

import org.jetbrains.exposed.dao.id.IntIdTable

object CacheWhitelistDataTable : IntIdTable(name = "multilogin_cache_whitelist_v4") {
    // 指定 Service (可空)
    val serviceId = integer("service_id").nullable()

    // 缓存白名单数据, 可填name或者uuid
    val cacheValue = varchar("value", 255)

    init {
        uniqueIndex(cacheValue)
    }
}

object ProfileDataTable : IntIdTable(name = "multilogin_in_game_profile_v4") {
    // 游戏内档案 uuid (不可变不为空)
    val profileId = uuid("profile_uuid")

    // 游戏内名字 (不为空)
    val profileName = varchar("username", 255)

    // 游戏内名字小写 (不为空)
    val profileNameLowerCase = varchar("username_lower_case", 255)

    init {
        uniqueIndex(profileNameLowerCase)
    }
}

object UserDataTable : IntIdTable(name = "multilogin_user_data_v4") {
    // 用户来源的 Service 的 id (不可变不为空)
    val serviceId = integer("service_id").check("service_id_range") { it.between(0, 127) }

    // 用户来源的 Service 提供的 uuid (不可变不为空)
    val loginUuid = uuid("login_uuid")

    // 用户来源的 Service 提供的 name (可更新不为空)
    val loginName = varchar("login_name", 255)

    // 用户有没有白名单
    val whitelist = bool("whitelist").default(false)

    // 用户第一次登录时分配给它的终身档案 (不可变不为空, 用于绑定用户档案)
    // ReadOnly
    val initialProfileId = integer("initial_profile_id").references(ProfileDataTable.id)

    // 用户正在使用的档案 (可更新不为空, 初始生成时值和 initial_profile 一致)
    val linkToProfileId = integer("link_to_profile_id").references(ProfileDataTable.id)
    init {
        uniqueIndex(serviceId, loginUuid)
        uniqueIndex(initialProfileId)
    }
}