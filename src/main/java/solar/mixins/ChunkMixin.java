package solar.mixins;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import solar.interfaces.SolarChunk;

@Mixin(Chunk.class)
public class ChunkMixin implements SolarChunk
{
    @Unique private boolean solar$loaded;
    @Unique private long solar$lastSolarChunk;
    
    @Unique @Override public boolean isLoaded() { return solar$loaded; }
    @Unique @Override public void setLoaded(boolean loaded) { solar$loaded = loaded; }
    
    @Unique @Override public long getLastSolarTick() { return solar$lastSolarChunk; }
    @Unique @Override public void setLastSolarTick(long lastSolarTick) { this.solar$lastSolarChunk = lastSolarTick; }
}
