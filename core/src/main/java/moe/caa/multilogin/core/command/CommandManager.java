package moe.caa.multilogin.core.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.caa.multilogin.core.command.handler.AbstractHandler;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 命令处理程序
 */
@AllArgsConstructor
@Getter
public class CommandManager {
    private final MultiCore core;
    private final Set<AbstractHandler> handlers = Collections.synchronizedSet(new HashSet<>());


}
