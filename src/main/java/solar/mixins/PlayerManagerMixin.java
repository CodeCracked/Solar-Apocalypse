package solar.mixins;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import solar.SolarManager;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin
{
    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void setupSolarPlayer(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci)
    {
        SolarManager.onPlayerJoin(player);
    }
}
