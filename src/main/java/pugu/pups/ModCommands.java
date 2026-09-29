package pugu.pups;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class ModCommands {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("pupxp")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            Entity entity = EntityArgument.getEntity(context, "target");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            if (!(entity instanceof Wolf wolf)) {
                                                context.getSource().sendFailure(
                                                        Component.literal("That isn't a dog."));
                                                return 0;
                                            }

                                            DogStats.awardXp(wolf, amount);

                                            context.getSource().sendSuccess(() -> Component.literal(
                                                    "Gave " + amount + " XP. Now level "
                                                            + wolf.getAttachedOrElse(ModAttachments.LEVEL, 1)), false);

                                            return 1;
                                        })))));
    }
}