package solar.mixins;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.NetherPortal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(NetherPortal.class)
public class NetherPortalMixin
{
    @Inject(method = "getOrEmpty", at = @At("HEAD"), cancellable = true)
    private static void disableNetherPortals(WorldAccess world, BlockPos pos, Predicate<NetherPortal> validator, Direction.Axis firstCheckedAxis, CallbackInfoReturnable<Optional<NetherPortal>> cir)
    {
        cir.setReturnValue(Optional.empty());
    }
}
