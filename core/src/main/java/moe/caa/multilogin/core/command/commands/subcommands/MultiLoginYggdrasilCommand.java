package moe.caa.multilogin.core.command.commands.subcommands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiLoginYggdrasilCommand extends BaseSubCommand {
    public MultiLoginYggdrasilCommand(MultiCore core) {
        super(core);
    }

    @Override
    public ArgumentBuilder<ISender, ?> getSubExecutor() {
        return literal("yggdrasil")
                .then(literal("list")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_YGGDRASIL_LIST))
                        .executes(this::executeList)
                );
    }

    private int executeList(CommandContext<ISender> context) {
        Set<YggdrasilService> allServices = getCore().getYggdrasilServicesHandler().getAllServices();
        if(allServices.size() == 0){
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_empty"));
            return 0;
        }
        List<YggdrasilService> enabled = new ArrayList<>();
        List<YggdrasilService> disabled = new ArrayList<>();
        for (YggdrasilService service : allServices) {
            if(service.isEnable()){
                enabled.add(service);
            } else {
                disabled.add(service);
            }
        }
        String patEnabled = "";
        String patDisabled = "";

        if(!enabled.isEmpty()){
            patEnabled = enabled.stream().map(y -> getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_item", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("yggdrasil_name").content(y.getName()).build(),
                    FormatContent.FormatEntry.builder().name("yggdrasil_path").content(y.getName()).build()
            ))).collect(Collectors.joining(", "));

            patEnabled = getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_entry", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("has_enabled").content(getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_has_enable_enabled")).build(),
                    FormatContent.FormatEntry.builder().name("list").content(patEnabled).build()
            ));
        }

        if(!disabled.isEmpty()){
            patDisabled = disabled.stream().map(y -> getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_item", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("yggdrasil_name").content(y.getName()).build(),
                    FormatContent.FormatEntry.builder().name("yggdrasil_path").content(y.getName()).build()
            ))).collect(Collectors.joining(", "));

            patDisabled = getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_entry", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("has_enabled").content(getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list_has_enable_disable")).build(),
                    FormatContent.FormatEntry.builder().name("list").content(patDisabled).build()
            ));
        }

        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_yggdrasil_list", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("count").content(allServices.size()).build(),
                FormatContent.FormatEntry.builder().name("enabled_count").content(enabled.size()).build(),
                FormatContent.FormatEntry.builder().name("list_entries").content(patEnabled + patDisabled).build()
        )));

        return 0;
    }
}
