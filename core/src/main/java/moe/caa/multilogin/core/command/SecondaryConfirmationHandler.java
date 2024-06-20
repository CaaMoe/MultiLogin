package moe.caa.multilogin.core.command;

import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 二次确认快处工具
 */
public class SecondaryConfirmationHandler {
    private final Map<IPlayer, ConfirmEntry> concurrentHashMap = new ConcurrentHashMap<>();
    private final AtomicReference<ConfirmEntry> consoleConfirm = new AtomicReference<>();

    /**
     * 提交一个风险指令
     */
    public void submit(
            ISender sender,
            CallbackConfirmCommand callbackConfirmCommand,
            String desc, String consequences
    ) {
        if (sender.isPlayer()) {
            concurrentHashMap.put(sender.getAsPlayer(), new ConfirmEntry(callbackConfirmCommand));
        } else if (sender.isConsole()) {
            consoleConfirm.set(new ConfirmEntry(callbackConfirmCommand));
        } else {
            sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_unidentified"));
            return;
        }

        sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_warning",
                new Pair<>("desc", desc),
                new Pair<>("consequences", consequences)
        ));
    }

    /**
     * 对风险指令进行确认
     */
    public void confirm(ISender sender) throws Exception {
        concurrentHashMap.values().removeIf(ConfirmEntry::isInvalid);
        consoleConfirm.updateAndGet(confirmEntry -> {
            if (confirmEntry == null) return null;
            if (confirmEntry.isInvalid()) return null;
            return confirmEntry;
        });

        if (sender.isPlayer()) {
            ConfirmEntry entry = concurrentHashMap.remove(sender.getAsPlayer());
            if (entry == null) {
                sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_not_found"));
                return;
            }
            entry.confirm();
        } else if (sender.isConsole()) {
            ConfirmEntry entry = consoleConfirm.getAndSet(null);
            if (entry == null) {
                sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_not_found"));
                return;
            }
            entry.confirm();
        } else {
            sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_unidentified"));
        }
    }

    public interface CallbackConfirmCommand {
        void confirm() throws Exception;
    }

    private static class ConfirmEntry {
        private final long subTime;
        private final CallbackConfirmCommand callbackConfirmCommand;

        private ConfirmEntry(CallbackConfirmCommand callbackConfirmCommand) {
            this.subTime = System.currentTimeMillis();
            this.callbackConfirmCommand = callbackConfirmCommand;
        }

        private boolean isInvalid() {
            return subTime + CommandHandler.getCore().getPluginConfig().getConfirmCommandValidTimeMills() < System.currentTimeMillis();
        }

        public void confirm() throws Exception {
            callbackConfirmCommand.confirm();
        }
    }
}
