package solar;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import solar.interfaces.SolarChunk;
import solar.world.LoadedChunks;

public final class SolarManager
{
    private static boolean active = false;
    private static long currentSolarTick = 0;
    
    public static void init()
    {
        LoadedChunks.CHUNK_LOADED.register(SolarManager::onChunkLoaded);
        ServerTickEvents.START_WORLD_TICK.register(SolarManager::onWorldTick);
    }
    
    public static void enable() { active = true; }
    public static void disable() { active = false; }
    public static void reset() { currentSolarTick = 0; }
    
    //region Event Handlers
    private static void onChunkLoaded(ServerWorld world, WorldChunk chunk) { updateChunkDecay(chunk); }
    private static void onWorldTick(ServerWorld world) { if (active) tick(); }
    //endregion
    //region Ticking
    public static void tick()
    {
        try
        {
            for (WorldChunk chunk : LoadedChunks.loadedChunksIterator()) tickChunk(chunk);
            currentSolarTick++;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private static void tickChunk(WorldChunk chunk)
    {
        if (SolarApocalypseMod.RANDOM.nextInt(20) == 0) performDecay(chunk);
        ((SolarChunk) chunk).incrementSolarTick();
    }
    //endregion
    //region Decay
    private static void updateChunkDecay(WorldChunk chunk)
    {
        long tick = ((SolarChunk) chunk).getLastSolarTick();
        ((SolarChunk) chunk).setLastSolarTick(currentSolarTick);
        
        // While this chunk's solar decay is behind the world's, perform decay operations
        while (tick < currentSolarTick)
        {
            performDecay(chunk);
            tick += SolarApocalypseMod.RANDOM.nextBetween(10, 30);
        }
    }
    private static void performDecay(WorldChunk chunk)
    {
        // Pick chunk position
        //int x = SolarApocalypseMod.RANDOM.nextInt(16);
        //int z = SolarApocalypseMod.RANDOM.nextInt(16);
        int x = 0, z = 0;
        int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        BlockPos pos = new BlockPos(x, y, z);
    
        // Apply solar decay
        BlockState block = chunk.getBlockState(pos);
        BlockState decayed = decayBlockState(block, currentSolarTick);
        chunk.getWorld().setBlockState(pos.add(chunk.getPos().getStartPos()), decayed);
    }
    private static BlockState decayBlockState(BlockState original, long tick)
    {
        return Blocks.WHITE_STAINED_GLASS.getDefaultState();
    }
    //endregion
}
