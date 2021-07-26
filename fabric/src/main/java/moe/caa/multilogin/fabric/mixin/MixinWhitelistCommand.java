package moe.caa.multilogin.fabric.mixin;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 过于复杂，单独注册
@Mixin(WhitelistCommand.class)
public class MixinWhitelistCommand {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
//        dispatcher.register(
//                CommandManager.literal("multilogin")
//                        //二级命令
//                        .then(CommandManager.literal("query")
//                                        //三级命令
//                                        .then(CommandManager.literal("name")
//                                                //需求字符串参数
//                                                .then(CommandManager.argument("target", StringArgumentType.greedyString())
//                                                        //执行
//                                                        .executes(context -> {
//                                                            MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(new FabricSender(context.getSource()), "multilogin", "query name " + StringArgumentType.getString(context, "target"));
//                                                            return 0;
//                                                        })
//                                                )
//                                        ).then(CommandManager.literal("onlineuuid")
//                                                .then(CommandManager.argument("target", StringArgumentType.greedyString())
//                                                        .executes(context -> {
//                                                            MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(new FabricSender(context.getSource()), "multilogin", "query onlineuuid " + StringArgumentType.getString(context, "target"));
//                                                            return 0;
//                                                        })
//                                                )
//                                        ).then(CommandManager.literal("redirectuuid")
//                                                .then(CommandManager.argument("target", StringArgumentType.greedyString())
//                                                        .executes(context -> {
//                                                            MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(new FabricSender(context.getSource()), "multilogin", "query redirectuuid " + StringArgumentType.getString(context, "target"));
//                                                            return 0;
//                                                        })
//                                                )
//                                        )
//                                //权限
//                        ).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
//                        .then(CommandManager.literal("reload").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
//                                .executes(context -> {
//                                    MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(new FabricSender(context.getSource()), "multilogin", "reload");
//                                    return 0;
//                                })
//                        )
//        );
//
//        dispatcher.register(
//                //根命令和权限
//                CommandManager.literal("whitelist").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
//                        //二级命令
//                        .then(CommandManager.literal("add")
//                                //需求字符串参数
//                                .then(CommandManager.argument("target", StringArgumentType.greedyString())
//                                        //执行
//                                        .executes(context -> {
//                                            MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(new FabricSender(context.getSource()), "whitelist", "add " + StringArgumentType.getString(context, "target"));
//                                            return 0;
//                                        })
//                                )
//                        )
//                        .then(CommandManager.literal("remove")
//                                .then(CommandManager.argument("target", StringArgumentType.greedyString())
//                                        .executes(context -> {
//                                            MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(new FabricSender(context.getSource()), "whitelist", "remove " + StringArgumentType.getString(context, "target"));
//                                            return 0;
//                                        })
//                                )
//                        )
//        );
        ci.cancel();
    }
}
