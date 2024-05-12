package moe.caa.multilogin.core.auth.validate.entry;

import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;

/**
 * 玩家初始 UUID 生成程序
 */
public class AssignInGameFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public AssignInGameFlows(MultiCore core) {
        this.core = core;
    }

    @SneakyThrows
    @Override
    public Signal run(ValidateContext validateContext) {

        // 从数据库里面读登录档案的游戏内 UUID
        UUID inGameUUID = core.getSqlManager().getUserDataTable().getInGameUUID(
                validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
        );

        // 如果这个 UUID 不存在，表示是个预新玩家或是档案被清理的新玩家。这时需要分配个全新的身份卡给它。
        String loginName = validateContext.getBaseServiceAuthenticationResult().getResponse().getName();
        if (inGameUUID == null) {

            inGameUUID = validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getInitUUID()
                    .generateUUID(validateContext.getBaseServiceAuthenticationResult().getResponse().getId(), loginName);

            // 需要线程安全
            synchronized (AssignInGameFlows.class) {
                // 取没有被占用的 UUID
                while (core.getSqlManager().getInGameProfileTable().dataExists(inGameUUID)) {
                    LoggerProvider.getLogger().warn(String.format("UUID %s has been used and will take a random value.", inGameUUID.toString()));
                    inGameUUID = UUID.randomUUID();
                }
                // 身份卡UUID数据被确定
                // 更新数据
                core.getSqlManager().getUserDataTable().setInGameUUID(
                        validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                        validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                        inGameUUID);
            }
        }
        if (core.getPluginConfig().isAutoNameChange() && validateContext.isOnlineNameUpdated()) {
            String username = core.getSqlManager().getInGameProfileTable().getUsername(inGameUUID);
            if (!ValueUtil.isEmpty(username)) {
                core.getSqlManager().getInGameProfileTable().eraseUsername(username);
            }
        }

        // 身份卡UUID数据存在，看看数据库中有没有对应的记录
        boolean exist = core.getSqlManager().getInGameProfileTable().dataExists(inGameUUID);
        if (exist) {
            String username = core.getSqlManager().getInGameProfileTable().getUsername(inGameUUID);
            if (!ValueUtil.isEmpty(username)) {
                validateContext.getInGameProfile().setId(inGameUUID);
                validateContext.getInGameProfile().setName(username);
                return Signal.PASSED;
            }
        }

        String fixName = validateContext.getBaseServiceAuthenticationResult().getServiceConfig().generateName(loginName);
        if(fixName.isEmpty()) fixName = "1";

        String initFixName = fixName;
        if (core.getPluginConfig().isNameCorrect()) {
            boolean modified = false;
            UUID ownerUUID;
            while ((ownerUUID = core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(fixName)) != null) {
                if(ownerUUID.equals(inGameUUID)) break;
                fixName = incrementString(fixName);
                modified = true;
            }

            if(modified){
                UUID finalInGameUUID = inGameUUID;
                String finalFixName = fixName;
                LoggerProvider.getLogger().warn(String.format("The name %s is occupied, change it to %s.", initFixName, fixName));
                core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
                    IPlayer player = core.getPlugin().getRunServer().getPlayerManager().getPlayer(finalInGameUUID);
                    player.sendMessagePL(core.getLanguageHandler().getMessage("name_correct_info",
                            new Pair<>("old_name", initFixName),
                            new Pair<>("new_name", finalFixName)
                    ));
                }, 2000);
            }
        }

        // Username 需要更新
        if (exist) {
            try {
                core.getSqlManager().getInGameProfileTable().updateUsername(inGameUUID,
                        fixName);
                validateContext.getInGameProfile().setId(inGameUUID);
                validateContext.getInGameProfile().setName(fixName);
                return Signal.PASSED;
            } catch (SQLIntegrityConstraintViolationException e) {
                validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_repeated",
                        new Pair<>("name", validateContext.getInGameProfile().getName())
                ));
                return Signal.TERMINATED;
            }
        } else {
            try {
                core.getSqlManager().getInGameProfileTable().insertNewData(inGameUUID,
                        fixName);
                validateContext.getInGameProfile().setId(inGameUUID);
                validateContext.getInGameProfile().setName(fixName);
                return Signal.PASSED;
            } catch (SQLIntegrityConstraintViolationException e) {
                validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_repeated",
                        new Pair<>("name", validateContext.getInGameProfile().getName())
                ));
                return Signal.TERMINATED;
            }
        }
    }

    private String incrementString(String source){
        if (source.isEmpty()) return "1";

        char c = source.charAt(source.length() - 1);
        if (Character.isDigit(c)) {
            int i = Character.getNumericValue(c);
            if(i == 9){
                return incrementString(source.substring(0, source.length() - 1)) + "0";
            } else {
                return source.substring(0, source.length() - 1) + (i + 1);
            }
        }

        return source + "1";
    }
}
