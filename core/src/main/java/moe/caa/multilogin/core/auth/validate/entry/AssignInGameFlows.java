package moe.caa.multilogin.core.auth.validate.entry;

import lombok.SneakyThrows;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
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

        // 从 temp redirect 中读游戏内 UUID
        UUID inGameUUID = core.getTemplateProfileRedirectHandler().getTemplateProfileRedirectMap().remove(
                new Pair<>(
                        validateContext.getYggdrasilAuthenticationResult().getYggdrasilId(),
                        validateContext.getYggdrasilAuthenticationResult().getResponse().getId()
                )
        );

        if (inGameUUID == null) {
            // 如果不在，那就从数据库里面读登录档案的游戏内 UUID
            inGameUUID = core.getSqlManager().getUserDataTable().getInGameUUID(
                    validateContext.getYggdrasilAuthenticationResult().getResponse().getId(),
                    validateContext.getYggdrasilAuthenticationResult().getYggdrasilId()
            );
        }

        // 如果这个 UUID 不存在，表示是个预新玩家或是档案被清理的新玩家。这时需要分配个全新的身份卡给它。
        if (inGameUUID == null) {

            inGameUUID = validateContext.getYggdrasilAuthenticationResult().getYggdrasilServiceConfig().getInitUUID()
                    .generateUUID(validateContext.getYggdrasilAuthenticationResult().getResponse().getId(),
                            validateContext.getYggdrasilAuthenticationResult().getResponse().getName());

            // 需要线程安全
            synchronized (this) {
                // 取没有被占用的 UUID
                while (core.getSqlManager().getInGameProfileTable().dataExists(inGameUUID)) {
                    LoggerProvider.getLogger().warn(String.format("UUID %s has been used and will take a random value.", inGameUUID.toString()));
                    inGameUUID = UUID.randomUUID();
                }
                // 身份卡UUID数据被确定
                // 更新数据
                core.getSqlManager().getUserDataTable().setInGameUUID(
                        validateContext.getYggdrasilAuthenticationResult().getResponse().getId(),
                        validateContext.getYggdrasilAuthenticationResult().getYggdrasilId(),
                        inGameUUID);
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

        // Username 需要更新
        if (exist) {
            try {
                core.getSqlManager().getInGameProfileTable().updateUsername(inGameUUID,
                        validateContext.getYggdrasilAuthenticationResult().getResponse().getName());
                validateContext.getInGameProfile().setId(inGameUUID);
                return Signal.PASSED;
            } catch (SQLIntegrityConstraintViolationException e) {
                validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_repeated",
                        new Pair<>("current_username", validateContext.getInGameProfile().getName())
                ));
                return Signal.TERMINATED;
            }
        } else {
            try {
                core.getSqlManager().getInGameProfileTable().insertNewData(inGameUUID,
                        validateContext.getYggdrasilAuthenticationResult().getResponse().getName());
                validateContext.getInGameProfile().setId(inGameUUID);
                return Signal.PASSED;
            } catch (SQLIntegrityConstraintViolationException e) {
                validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_repeated",
                        new Pair<>("current_username", validateContext.getInGameProfile().getName())
                ));
                return Signal.TERMINATED;
            }
        }
    }
}
