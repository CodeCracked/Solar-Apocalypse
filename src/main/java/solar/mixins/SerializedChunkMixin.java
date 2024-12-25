package solar.mixins;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.SerializedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import solar.interfaces.SolarChunk;

@Mixin(SerializedChunk.class)
public class SerializedChunkMixin
{
    @Unique private long solar$lastSolarTick;
    
    @Inject(method = "fromChunk", at = @At("RETURN"))
    private static void injectSolarTicks(ServerWorld world, Chunk chunk, CallbackInfoReturnable<SerializedChunk> cir)
    {
        SerializedChunk ret = cir.getReturnValue();
        ((SerializedChunkMixin) (Object) ret).solar$lastSolarTick = ((SolarChunk) chunk).getLastSolarTick();
    }
    
    @Inject(method = "fromNbt", at = @At("RETURN"))
    private static void readLastSolarTick(HeightLimitView world, DynamicRegistryManager registryManager, NbtCompound nbt, CallbackInfoReturnable<SerializedChunk> cir)
    {
        SerializedChunk ret = cir.getReturnValue();
        ((SerializedChunkMixin) (Object) ret).solar$lastSolarTick = nbt.getLong("LastSolarTick");
    }
    
    @Inject(method = "serialize", at = @At("RETURN"))
    public void writeLastSolarTick(CallbackInfoReturnable<NbtCompound> cir)
    {
        NbtCompound nbt = cir.getReturnValue();
        nbt.putLong("LastSolarTick", solar$lastSolarTick);
    }
}
