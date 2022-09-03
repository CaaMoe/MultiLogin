package moe.caa.multilogin.dataupgrade.oldc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * 老的用户数据
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class OldUserData {
    private UUID onlineUUID;
    private String currentName;
    private UUID redirectUUID;
    private String yggdrasilService;
    private boolean whitelist;
}
