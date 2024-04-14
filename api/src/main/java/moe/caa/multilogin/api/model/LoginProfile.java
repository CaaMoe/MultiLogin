package moe.caa.multilogin.api.model;

import java.net.InetAddress;

public record LoginProfile(String username, String serverId, InetAddress playerIp) {
}
