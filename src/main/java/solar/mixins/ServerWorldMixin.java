package solar.mixins;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import solar.SolarManager;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
    @Shadow @Final List<ServerPlayerEntity> players;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;wakeSleepingPlayers()V"))
    private void onSuccessfulSleep(BooleanSupplier shouldKeepTicking, CallbackInfo ci)
    {
        for (ServerPlayerEntity player : players)
        {
            SolarManager.resetHealthAttributes(player);
            SolarManager.resetHealth(player);
        }
    }
}
