package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NMSHandlerEnum {
    v1_19_r1("moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.NMSInjector");

    private final String nhc;
}
