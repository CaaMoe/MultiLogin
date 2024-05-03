package moe.caa.multilogin.core.main

import moe.caa.multilogin.api.MultiLoginAPI
import moe.caa.multilogin.api.data.IProfileData
import moe.caa.multilogin.api.data.IUserData
import moe.caa.multilogin.api.service.IService
import moe.caa.multilogin.core.resource.configuration.GeneralConfiguration
import moe.caa.multilogin.core.resource.configuration.service.BaseService
import java.util.*

class MultiLoginAPIImpl(private val multiCore: MultiCore) : MultiLoginAPI {
    override fun getServices(): Collection<BaseService> =
        Collections.unmodifiableCollection(GeneralConfiguration.services.values)

    override fun getPlayerData(profileUuid: UUID) = multiCore.dataManager.verifiedProfileData[profileUuid]

    override fun getService(serviceId: Int) = GeneralConfiguration.services[serviceId]

    override fun addCacheWhitelist(loginUsername: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun addCacheWhitelist(loginUsername: String, service: IService): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasCacheWhitelist(loginUsername: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasCacheWhitelist(loginUsername: String, service: IService): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeCacheWhitelist(loginUsername: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeCacheWhitelist(loginUsername: String, service: IService): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAllUserData(): MutableList<IUserData> {
        TODO("Not yet implemented")
    }

    override fun findAllUserData(service: IService): MutableList<IUserData> {
        TODO("Not yet implemented")
    }

    override fun findUserData(userId: Int): IUserData? {
        TODO("Not yet implemented")
    }

    override fun findUserData(loginUUID: UUID, service: IService): IUserData? {
        TODO("Not yet implemented")
    }

    override fun findAllProfileData(): MutableList<IProfileData> {
        TODO("Not yet implemented")
    }

    override fun findProfileData(profileId: Int): IProfileData? {
        TODO("Not yet implemented")
    }

    override fun findProfileData(profileUuid: UUID): IProfileData? {
        TODO("Not yet implemented")
    }

    override fun findProfileData(profileUsername: String): IProfileData? {
        TODO("Not yet implemented")
    }

    override fun findWhoInitializedIt(profileData: IProfileData): IUserData? {
        TODO("Not yet implemented")
    }

    override fun findLinker(profileData: IProfileData): MutableList<IUserData>? {
        TODO("Not yet implemented")
    }

    override fun createProfile(profileUUID: UUID, profileName: String): IProfileData {
        TODO("Not yet implemented")
    }

    override fun renameProfile(handle: IProfileData, newProfileName: String): IProfileData {
        TODO("Not yet implemented")
    }

    override fun setLinkToProfile(userData: IUserData, profileData: IProfileData): IUserData {
        TODO("Not yet implemented")
    }

    override fun setLinkToInitialProfile(userData: IUserData): IUserData {
        TODO("Not yet implemented")
    }

    override fun setWhitelist(handle: IUserData, whitelist: Boolean): IUserData {
        TODO("Not yet implemented")
    }
}