package solar;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import solar.interfaces.SolarChunk;
import solar.phases.SolarPhase;
import solar.phases.SolarPhases;
import solar.world.LoadedChunks;
import solar.world.SolarPersistentState;

public final class SolarManager
{
    private static final SolarPhase[] phases = new SolarPhase[]
    {
            SolarPhases.PHASE_1,
            SolarPhases.PHASE_2,
            SolarPhases.PHASE_3,
            SolarPhases.PHASE_4,
            SolarPhases.FINAL_PHASE
    };
    private static final long intervalSize = 20;
    
    private static SolarPersistentState state;
    
    public static void init()
    {
        ServerWorldEvents.LOAD.register(SolarManager::onWorldLoaded);
        LoadedChunks.CHUNK_LOADED.register(SolarManager::onChunkLoaded);
        ServerTickEvents.START_WORLD_TICK.register(SolarManager::onWorldTick);
    }
    
    public static void enable() { state.setEnabled(true); }
    public static void disable() { state.setEnabled(false); }
    public static void reset() { state.reset(); }
    
    //region Event Handlers
    private static void onWorldLoaded(MinecraftServer server, ServerWorld serverWorld)
    {
        state = SolarPersistentState.getServerState(server);
    }
    private static void onChunkLoaded(ServerWorld world, WorldChunk chunk) { updateChunkDecay(chunk); }
    private static void onWorldTick(ServerWorld world) { if (state.isEnabled()) tick(); }
    //endregion
    //region Ticking
    public static void tick()
    {
        try
        {
            for (WorldChunk chunk : LoadedChunks.loadedChunksIterator()) tickChunk(chunk);
            state.incrementTick();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private static void tickChunk(WorldChunk chunk)
    {
        performDecay(chunk, state.getCurrentTick());
        ((SolarChunk) chunk).setLastSolarTick(state.getCurrentTick());
    }
    //endregion
    //region Decay
    private static void updateChunkDecay(WorldChunk chunk)
    {
        long tick = ((SolarChunk) chunk).getLastSolarTick();
        ((SolarChunk) chunk).setLastSolarTick(state.getCurrentTick());
        
        // While this chunk's solar decay is behind the world's, perform decay operations
        while (tick < state.getCurrentTick())
        {
            performDecay(chunk, tick);
            tick += intervalSize;
        }
    }
    private static void performDecay(WorldChunk chunk, long tick)
    {
        // Get timing info
        int day = (int)(tick / 24000);
        tick += chunk.getPos().hashCode();
        
        // Iterate through phases
        for (SolarPhase phase : phases)
        {
            if (day < phase.getStartingDay()) continue;
            if (tick % (phase.getDecayInterval(day) * intervalSize) != 0) continue;
            
            // Pick chunk position
            int x = SolarApocalypseMod.RANDOM.nextInt(16);
            int z = SolarApocalypseMod.RANDOM.nextInt(16);
            int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos pos = new BlockPos(x + chunk.getPos().getStartX(), y, z + chunk.getPos().getStartZ());
            
            // Apply solar decay
            BlockState block = chunk.getBlockState(new BlockPos(x, pos.getY(), z));
            BlockState decayed = phase.getDecayedBlockState(block);
            if (decayed != null)
            {
                chunk.getWorld().setBlockState(pos, decayed);
                return;
            }
        }
    }
    //endregion
}
