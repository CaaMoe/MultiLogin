package moe.caa.multilogin.core.config

class MultiloginConfig {
    public var tablePrefix: String = "multilogin"
        private set

    companion object {
        @Volatile
        private var instance: MultiloginConfig? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: MultiloginConfig().also { instance = it }   // Todo: Load table prefix here.
            }
    }
}
