package moe.caa.multilogin.core.auth.validate.entry;

import lombok.SneakyThrows;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.util.UUID;

/**
 * 重名检查程序
 */
public class DuplicateNamesCheckFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public DuplicateNamesCheckFlows(MultiCore core) {
        this.core = core;
    }

    @SneakyThrows
    @Override
    public Signal run(ValidateContext validateContext) {
        if (core.getPluginConfig().isDisableDuplicateNamesCheck()) {
            core.getSqlManager().getInGameProfileTable().
                    updateUsername(validateContext.getInGameProfile().getId(), validateContext.getInGameProfile().getName());
            return Signal.PASSED;
        }
        UUID inGameUUID = core.getSqlManager().getInGameProfileTable()
                .getInGameUUID(validateContext.getInGameProfile().getName());
        // 如果 数据库中记录的 username 使用者为空，或使用者就是它本身，就更新
        if (inGameUUID == null || validateContext.getInGameProfile().getId().equals(inGameUUID)) {
            inGameUUID = validateContext.getInGameProfile().getId();
            core.getSqlManager().getInGameProfileTable().
                    updateUsername(inGameUUID, validateContext.getInGameProfile().getName());

            // 更新占用前先踢一下
            String kickMsg = core.getLanguageHandler().getMessage("in_game_username_occupy",
                    new Pair<>("current_username", validateContext.getInGameProfile().getName()));

            // 踢出
            for (IPlayer player : core.getPlugin().getRunServer().getPlayerManager().getPlayers(validateContext.getInGameProfile().getName())) {
                // 这里如果是它自己，就不进行操作，抛给 RepeatedLoginCheckFlows 进行处理
                if(player.getUniqueId().equals(inGameUUID)) continue;
                player.kickPlayer(kickMsg);
                validateContext.setNeedWait(true);
            }
            return Signal.PASSED;
        }
        // 否则，看他有没有权限占用这个用户名
        if (validateContext.getYggdrasilAuthenticationResult().getYggdrasilServiceConfig().isCompulsoryUsername()) {
            core.getSqlManager().getInGameProfileTable().eraseUsername(validateContext.getInGameProfile().getName());
            core.getSqlManager().getInGameProfileTable().
                    updateUsername(validateContext.getInGameProfile().getId(), validateContext.getInGameProfile().getName());

            String kickMsg = core.getLanguageHandler().getMessage("in_game_username_occupy",
                    new Pair<>("current_username", validateContext.getInGameProfile().getName()));

            // 踢出
            for (IPlayer player : core.getPlugin().getRunServer().getPlayerManager().getPlayers(validateContext.getInGameProfile().getName())) {
                player.kickPlayer(kickMsg);
                validateContext.setNeedWait(true);
            }
            LoggerProvider.getLogger().info(String.format("The user whose in game uuid is %s forcibly occupies the username %s.",
                    validateContext.getInGameProfile().getId().toString(), validateContext.getInGameProfile().getName()
            ));
            return Signal.PASSED;
        }
        // 没有的话就踹了
        validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_repeated",
                new Pair<>("current_username", validateContext.getInGameProfile().getName())
        ));
        return Signal.TERMINATED;
    }
}
