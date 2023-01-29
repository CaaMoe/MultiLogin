package moe.caa.multilogin.core.command.argument.suggestion;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.command.CommandHandler;

import java.util.Collection;

/**
 * 在线玩家名字补全器
 */
public class OnlinePlayerNameSuggestion extends BaseLimitSuggestion<IPlayer> {
    protected OnlinePlayerNameSuggestion() {
        super(ISender::getName);
    }

    public static OnlinePlayerNameSuggestion suggestion(){
        return new OnlinePlayerNameSuggestion();
    }

    @Override
    public Collection<IPlayer> getSource() {
        return CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers();
    }
}
