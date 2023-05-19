package moe.caa.multilogin.core.auth.validate.entry;

import lombok.SneakyThrows;
import moe.caa.multilogin.api.util.There;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.database.table.UserDataTableV3;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.util.UUID;

public class InitialLoginDataFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public InitialLoginDataFlows(MultiCore core) {
        this.core = core;
    }

    @SneakyThrows
    @Override
    public Signal run(ValidateContext validateContext) {
        UserDataTableV3 dataTable = core.getSqlManager().getUserDataTable();
        There<String, UUID, Boolean> there = dataTable.get(validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId());
        if (there == null) {
            dataTable.insertNewData(
                    validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                    validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                    validateContext.getBaseServiceAuthenticationResult().getResponse().getName(),
                    null
            );
        } else {
            if (!validateContext.getBaseServiceAuthenticationResult().getResponse().getName().equals(there.getValue1())) {
                dataTable.setOnlineName(
                        validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                        validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                        validateContext.getBaseServiceAuthenticationResult().getResponse().getName()
                );
                validateContext.setOnlineNameUpdated(true);
            }
        }
        return Signal.PASSED;
    }
}
