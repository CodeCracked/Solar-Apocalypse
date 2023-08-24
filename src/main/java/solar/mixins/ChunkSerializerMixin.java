package solar.mixins;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import solar.interfaces.SolarChunk;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin
{
    @Inject(method = "serialize", at = @At("RETURN"))
    private static void writeCustomData(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> callback)
    {
        callback.getReturnValue().putLong("LastSolarTick", ((SolarChunk)chunk).getLastSolarTick());
    }
    
    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void readCustomData(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> callback)
    {
        if (nbt.contains("LastSolarTick", NbtElement.LONG_TYPE)) ((SolarChunk)callback.getReturnValue()).setLastSolarTick(nbt.getLong("LastSolarTick"));
    }
}
