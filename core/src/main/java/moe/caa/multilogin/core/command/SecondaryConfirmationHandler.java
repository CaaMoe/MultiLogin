package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.impl.CallbackTransmit;
import moe.caa.multilogin.core.impl.ISender;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 二次确认处理
 */
public class SecondaryConfirmationHandler {
    private final ConcurrentHashMap<Sender, Entry> concurrentHashMap = new ConcurrentHashMap<>();

    public void submit(ISender sender, CallbackTransmit<Void> callbackTransmit) {
        concurrentHashMap.put(new Sender(sender), new Entry(callbackTransmit));
    }

    public boolean confirm(ISender sender) throws Exception {
        Entry entry = concurrentHashMap.get(new Sender(sender));
        if (entry == null) return false;
        if (entry.failureTime > System.currentTimeMillis()) {
            entry.confirm();
            remove(sender);
            return true;
        }
        return false;
    }

    public void remove(ISender sender) {
        concurrentHashMap.remove(new Sender(sender));
    }

    private static class Sender {
        private final boolean console;
        private final UUID uuid;

        private Sender(boolean console, UUID uuid) {
            this.console = console;
            this.uuid = uuid;
        }

        private Sender(ISender sender) {
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
            Sender sender = (Sender) o;
            if (console && sender.console) return true;
            return console == sender.console && Objects.equals(uuid, sender.uuid);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    private static class Entry {
        private final long failureTime;
        private final CallbackTransmit<Void> callbackTransmit;

        private Entry(CallbackTransmit<Void> callbackTransmit) {
            this.failureTime = System.currentTimeMillis() + (1000 * 15);
            this.callbackTransmit = callbackTransmit;
        }

        public void confirm() throws Exception {
            callbackTransmit.solve(null);
        }
    }
}
