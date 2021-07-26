package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.*;

public abstract class SubCommand {
    protected final MultiCore core;
    protected final Map<String, SubCommand> subCommandMap = new Hashtable<>();
    private final Permission permission;

    protected SubCommand(MultiCore core, Permission permission) {
        this.core = core;
        this.permission = permission;
    }

    public final boolean hasPermission(ISender sender) {
        if (permission == null) return true;
        return permission.hasPermission(sender);
    }

    /**
     * 执行这个命令
     *
     * @param sender 命令执行者
     * @param args   命令参数
     * @return 命令是否匹配成功（是否是无效的命令）
     */
    protected boolean execute(ISender sender, String[] args) {
        SubCommand sub = subCommandMap.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) return false;

        if (sub.hasPermission(sender)) {
            // 截取头部后向下传递
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            return sub.execute(sender, newArgs);
        }
        return false;
    }

    /**
     * 执行子命令命令补全
     *
     * @param sender 命令执行者
     * @param args   参数
     * @return 建议
     */
    protected List<String> tabCompete(ISender sender, String[] args) {
        List<String> ret = new ArrayList<>();
        if (args.length <= 0) return Collections.emptyList();
        SubCommand sub = subCommandMap.get(args[0].toLowerCase(Locale.ROOT));

        // 补全子命令提供的补全
        if (sub != null && sub.hasPermission(sender)) {
            // 截取头部后向下传递
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            ret.addAll(sub.tabCompete(sender, newArgs));
        }

        if (args.length != 1) return ret;
        // 补全子命令全称
        for (Map.Entry<String, SubCommand> entry : subCommandMap.entrySet()) {
            if (entry.getKey().startsWith(args[0].toLowerCase(Locale.ROOT))) {
                if (entry.getValue().hasPermission(sender)) {
                    ret.add(entry.getKey());
                }
            }
        }
        return ret;
    }
}
