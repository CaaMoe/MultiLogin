package moe.caa.multilogin.core.auth.validate;

import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.SequenceFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.util.Arrays;

public class ValidateAuthenticationService {
    private final MultiCore core;
    private final SequenceFlows<ValidateContext> sequenceFlows;

    public ValidateAuthenticationService(MultiCore core) {
        this.core = core;
        this.sequenceFlows = new SequenceFlows<>(Arrays.asList(

        ));
    }

    public ValidateAuthenticationResult checkIn(String username, String serverId, String ip,
                                                YggdrasilAuthenticationResult yggdrasilAuthenticationResult) {
        ValidateContext context = new ValidateContext(username, serverId, ip, yggdrasilAuthenticationResult);
        Signal run = sequenceFlows.run(context);
        if (run == Signal.PASSED)
            return ValidateAuthenticationResult.ofAllowed(context.getYggdrasilAuthenticationResult().getResponse());
        return ValidateAuthenticationResult.ofDisallowed(context.getDisallowMessage());
    }
}
