package moe.caa.multilogin.dataupgrade.oldc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 老的用户数据
 */
@Getter
@AllArgsConstructor
@ToString
public class OldUserData {
    private final UUID onlineUUID;
    private final String currentName;
    private final UUID redirectUUID;
    private final String yggdrasilService;
    private final boolean whitelist;
}
