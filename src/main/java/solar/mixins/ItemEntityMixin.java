package solar.mixins;

import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import solar.SolarManager;
import solar.interfaces.SolarItemEntity;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements SolarItemEntity
{
    @Shadow private int itemAge;

    @Unique private boolean solar$isEmber = false;
    @Unique private boolean solar$isFlare = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void createParticles(CallbackInfo ci)
    {
        World world = ((ItemEntity)(Object)this).getWorld();
        Vec3d pos = ((ItemEntity)(Object)this).getPos();
        boolean onGround = ((ItemEntity)(Object)this).isOnGround();

        if (world.isClient) return;
        ServerWorld serverWorld = (ServerWorld)world;

        SolarManager.createParticles(serverWorld, pos, solar$isEmber, solar$isFlare, onGround ? 1 : 10);
    }

    @Override
    public void setDespawnTime(int ticks)
    {
        itemAge = 6000 - ticks;
    }

    @Override
    public void markAsEmber()
    {
        solar$isEmber = true;
    }

    @Override
    public void markAsFlare()
    {
        solar$isFlare = true;
    }
}
