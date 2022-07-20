package moe.caa.multilogin.core.command;

import moe.caa.multilogin.api.plugin.ISender;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 二次确认快处工具
 */
public class SecondaryConfirmationHandler {
    private static final long confirmValidTimeMillis = 1000 * 15;
    private final Map<ConfirmSender, ConfirmEntry> concurrentHashMap = new ConcurrentHashMap<>();

    public void submit(ISender sender, CallbackConfirmCommand callbackConfirmCommand) {
        concurrentHashMap.put(new ConfirmSender(sender), new ConfirmEntry(callbackConfirmCommand));
    }

    public boolean confirm(ISender sender) throws Exception {
        concurrentHashMap.values().removeIf(confirmEntry -> !confirmEntry.valid());
        ConfirmEntry entry = concurrentHashMap.remove(new ConfirmSender(sender));
        if (entry == null) return false;
        entry.confirm();
        return true;
    }

    public interface CallbackConfirmCommand {
        void confirm() throws Exception;
    }

    private static class ConfirmSender {
        private final boolean console;
        private final UUID uuid;

        private ConfirmSender(ISender sender) {
            if (!sender.isPlayer()) {
                console = true;
                uuid = null;
            } else {
                console = false;
                uuid = sender.getAsPlayer().getUniqueId();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfirmSender sender = (ConfirmSender) o;
            if (console && sender.console) return true;
            return console == sender.console && Objects.equals(uuid, sender.uuid);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    private static class ConfirmEntry {
        private final long subTime;
        private final CallbackConfirmCommand callbackConfirmCommand;

        private ConfirmEntry(CallbackConfirmCommand callbackConfirmCommand) {
            this.subTime = System.currentTimeMillis();
            this.callbackConfirmCommand = callbackConfirmCommand;
        }

        private boolean valid() {
            return subTime + confirmValidTimeMillis >= System.currentTimeMillis();
        }

        public void confirm() throws Exception {
            callbackConfirmCommand.confirm();
        }
    }
}
