package solar.mixins;

import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import solar.world.LoadedChunks;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin
{
    @Shadow @Final ServerWorld world;
    
    @Inject(method = "onChunkStatusChange", at = @At("RETURN"))
    public void updateChunkState(ChunkPos chunkPos, ChunkLevelType levelType, CallbackInfo ci)
    {
        LoadedChunks.updateChunk(world, chunkPos, levelType);
    }
}
