package moe.caa.multilogin.core.database.v4

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ProfileData(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProfileData>(ProfileDataTable)

    var profileId by ProfileDataTable.profileId
    var profileName by ProfileDataTable.profileName
    var profileNameLowerCase by ProfileDataTable.profileNameLowerCase
}

class UserData(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserData>(UserDataTable)

    var serviceId by UserDataTable.serviceId
    var loginUuid by UserDataTable.loginUuid
    var loginName by UserDataTable.loginName
    var whitelist by UserDataTable.whitelist
    var initialProfileId by UserDataTable.initialProfileId
    var linkToProfileId by UserDataTable.linkToProfileId
}