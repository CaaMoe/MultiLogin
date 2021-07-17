package moe.caa.multilogin.core.language;

public enum LanguageKeys {
    PLUGIN_LOADED("plugin_loaded"),
    USE_INSIDE_LANGUAGE("use_inside_language"),
    USE_OUTSIDE_LANGUAGE("use_outside_language"),
    REPAIR_LANGUAGE_KEY("repair_language_key", ""),
    DEBUG_ENABLE("debug_enable"),
    LOGGER_FILE_ERROR("logger_file_error"),
    CONFIG_LOAD_ERROR("config_load_error"),
    SERVICES_NOTHING("services_nothing"),
    CONFIGURATION_VALUE_ERROR("configuration_value_error", ""),
    CONFIGURATION_KEY_ERROR("configuration_key_error", ""),
    URL_ILLEGAL_FORMAT("url_illegal_format", ""),
    YGGDRASIL_CONFIGURATION_ERROR("yggdrasil_configuration_error", "", ""),
    APPLY_YGGDRASIL("apply_yggdrasil", "", ""),
    APPLY_YGGDRASIL_NO_ENABLE("apply_yggdrasil_no_enable", "", ""),
    DATABASE_CONNECTED("database_connected", ""),
    DATABASE_CONNECT_ERROR("database_connect_error"),
    REFLECT_INIT_ERROR("reflect_init_error", ""),
    LIBRARY_INIT("library_init"),
    LIBRARY_DOWNLOADING("library_downloading", ""),
    LIBRARY_DOWNLOADED("library_downloaded", ""),
    LIBRARY_LOADED("library_loaded", ""),
    LIBRARY_LOAD_FAILED("library_load_failed", ""),
    VERIFICATION_NO_ADAPTER("verification_no_adapter"),
    VERIFICATION_NO_CHAE("verification_no_chae"),
    VERIFICATION_RUSH_NAME("verification_rush_name"),
    VERIFICATION_RUSH_NAME_ONL("verification_rush_name_Onl"),
    VERIFICATION_NO_WHITELIST("verification_no_whitelist"),
    VERIFICATION_ERROR("verification_error"),
    VERIFICATION_ALLOW("verification_allow", "", "", "", "", ""),
    NIT_ONLINE("not_online"),
    ERROR_AUTH("error_auth");

    public final String key;
    public final Object[] args;

    LanguageKeys(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    public String getMessage(Object... args) {
        return LanguageHandler.getMessage(this, args);
    }
}
