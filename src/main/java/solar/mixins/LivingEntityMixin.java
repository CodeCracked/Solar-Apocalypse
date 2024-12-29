package solar.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import solar.SolarManager;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "tick", at = @At("RETURN"))
    private void solarTick(CallbackInfo ci)
    {
        SolarManager.tickEntity((LivingEntity) (Object) this);
    }
}
