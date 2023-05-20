package moe.caa.multilogin.core.auth.validate.entry;

import lombok.SneakyThrows;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.database.table.UserDataTableV3;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

public class InitialLoginDataFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public InitialLoginDataFlows(MultiCore core) {
        this.core = core;
    }

    @SneakyThrows
    @Override
    public Signal run(ValidateContext validateContext) {
        UserDataTableV3 dataTable = core.getSqlManager().getUserDataTable();
        if (!dataTable.dataExists(
                validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
        )) {
            dataTable.insertNewData(
                    validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                    validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                    validateContext.getBaseServiceAuthenticationResult().getResponse().getName(),
                    null
            );
        } else {
            String currentName = dataTable.getOnlineName(validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                    validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId());
            if(!validateContext.getBaseServiceAuthenticationResult().getResponse().getName().equals(currentName)){
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
