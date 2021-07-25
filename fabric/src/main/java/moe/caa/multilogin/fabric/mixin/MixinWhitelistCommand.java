package moe.caa.multilogin.fabric.mixin;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import moe.caa.multilogin.fabric.impl.FabricSender;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WhitelistCommand.class)
public class MixinWhitelistCommand {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        dispatcher.register(
                CommandManager.literal("whitelist")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                        .then(CommandManager.argument("args", StringArgumentType.greedyString())
                                .executes(context -> {
                                    FabricSender sender = new FabricSender(context.getSource());
                                    MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(sender, "whitelist", StringArgumentType.getString(context, "args"));
                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                CommandManager.literal("multilogin")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                        .then(CommandManager.argument("args", StringArgumentType.greedyString())
                                .executes(context -> {
                                    FabricSender sender = new FabricSender(context.getSource());
                                    MultiLoginFabric.plugin.getMultiCore().getCommandHandler().execute(sender, "multilogin", StringArgumentType.getString(context, "args"));
                                    return 0;
                                })
                        )
        );
        ci.cancel();
    }
}
