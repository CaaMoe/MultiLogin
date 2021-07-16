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
    APPLY_YGGDRASIL_NO_ENABLE("apply_yggdrasil_no_enable", "", "");

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
