package solar.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

public final class LoadedChunks
{
    public static final Event<Loaded> CHUNK_LOADED = EventFactory.createArrayBacked(Loaded.class, listeners -> (world, chunk) -> { for (final Loaded listener : listeners) listener.onChunkLoaded(world, chunk); });
    
    private static final Map<Long, WorldChunk> LOADED_CHUNKS = new HashMap<>();
    
    public static void updateChunk(ServerWorld world, ChunkPos chunkPos, ChunkLevelType loadType)
    {
        long posLong = chunkPos.toLong();
        
        if (loadType.isAfter(ChunkLevelType.ENTITY_TICKING))
        {
            if (!LOADED_CHUNKS.containsKey(posLong))
            {
                WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
                LOADED_CHUNKS.put(posLong, chunk);
                CHUNK_LOADED.invoker().onChunkLoaded(world, chunk);
            }
        }
        else LOADED_CHUNKS.remove(posLong);
    }
    public static Iterable<WorldChunk> loadedChunksIterator() { return LOADED_CHUNKS.values(); }
    
    //region Event Types
    public interface Loaded
    {
        void onChunkLoaded(ServerWorld world, WorldChunk chunk);
    }
    //endregion
}
