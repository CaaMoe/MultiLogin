package moe.caa.multilogin.core.database.dao

interface ProfileDAO {
    fun findYggdrasilAuthServices(loginName: String): Set<Int>
}