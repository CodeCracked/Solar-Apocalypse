package solar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import solar.SolarManager;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SolarCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(literal("solar").requires(source -> source.hasPermissionLevel(2))
                .then(literal("enable")
                        .executes(ctx -> enable(ctx.getSource())))
                .then(literal("disable")
                        .executes(ctx -> disable(ctx.getSource())))
                .then(literal("tick")
                        .then(argument("count", IntegerArgumentType.integer(1))
                                .executes(ctx -> tick(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count"))))
                        .executes(ctx -> tick(ctx.getSource(), 1)))
                .then(literal("reset")
                        .executes(ctx -> reset(ctx.getSource())))
        );
    }
    
    private static int enable(ServerCommandSource source)
    {
        source.getServer().getPlayerManager().broadcast(Text.literal("Starting Solar Apocalypse").formatted(Formatting.RED), true);
        SolarManager.enable();
        return 1;
    }
    private static int disable(ServerCommandSource source)
    {
        source.getServer().getPlayerManager().broadcast(Text.literal("Ending Solar Apocalypse").formatted(Formatting.GREEN), true);
        SolarManager.disable();
        return 1;
    }
    private static int tick(ServerCommandSource source, int amount)
    {
        source.getServer().getPlayerManager().broadcast(Text.literal("Advancing " + amount + " Solar Ticks...").formatted(Formatting.RED), false);
        for (int i = 0; i < amount; i++) SolarManager.tick();
        source.getServer().getPlayerManager().broadcast(Text.literal("Finished Solar Ticks.").formatted(Formatting.GREEN), false);
        return amount;
    }
    private static int reset(ServerCommandSource source)
    {
        source.getServer().getPlayerManager().broadcast(Text.literal("Resetting Solar Apocalypse Timer").formatted(Formatting.GREEN), false);
        SolarManager.reset();
        return 1;
    }
}
