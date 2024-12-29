package solar;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import solar.interfaces.SolarChunk;
import solar.phases.SolarPhase;
import solar.phases.SolarPhases;
import solar.world.LoadedChunks;
import solar.world.SolarPersistentState;

import java.util.Set;

public final class SolarManager
{
    private static final Set<Heightmap.Type> HEIGHTMAPS = Set.of(Heightmap.Type.WORLD_SURFACE, Heightmap.Type.MOTION_BLOCKING);
    private static final SolarPhase[] phases = new SolarPhase[]
    {
            SolarPhases.PHASE_1,
            SolarPhases.PHASE_2,
            SolarPhases.PHASE_3,
            SolarPhases.PHASE_4,
            SolarPhases.FINAL_PHASE
    };

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
    private static void onWorldTick(ServerWorld world)
    {
        if (state.isEnabled())
        {
            world.getDimensionEntry().getKey().ifPresent(key ->
            {
                if (key.getValue().equals(Identifier.ofVanilla("overworld"))) tick();
            });
        }
    }
    //endregion
    //region Ticking
    public static void tick()
    {
        try
        {
            if (state.getCurrentTick() % 24000 < 12000) for (WorldChunk chunk : LoadedChunks.loadedChunksIterator()) tickChunk(chunk);
            else for (WorldChunk chunk : LoadedChunks.loadedChunksIterator()) ((SolarChunk)chunk).setLastSolarTick(state.getCurrentTick());
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
        performDecay(chunk, state.getCurrentTick(), chunk.getPos().hashCode());
        ((SolarChunk) chunk).setLastSolarTick(state.getCurrentTick());
    }
    //endregion
    //region Decay
    private static void updateChunkDecay(WorldChunk chunk)
    {
        long tick = ((SolarChunk) chunk).getLastSolarTick();
        if (tick >= state.getCurrentTick())
        {
            ((SolarChunk) chunk).setLastSolarTick(state.getCurrentTick());
            return;
        }

        // While this chunk's solar decay is behind the world's, perform decay operations
        Heightmap.populateHeightmaps(chunk, HEIGHTMAPS);
        while (tick < state.getCurrentTick())
        {
            performDecay(chunk, tick, 0);
            tick += 2 * GlobalConfig.INTERVAL_SIZE;
        }
        ((SolarChunk) chunk).setLastSolarTick(state.getCurrentTick());
    }
    private static void performDecay(WorldChunk chunk, long tick, long tickOffset)
    {
        // Get timing info
        int day = (int)(tick / 24000);
        tick += tickOffset;
        
        // Iterate through phases
        for (SolarPhase phase : phases)
        {
            if (day < phase.getStartingDay()) continue;
            if (tick % (phase.getDecayInterval(day) * GlobalConfig.INTERVAL_SIZE) != 0) continue;
            
            // Pick chunk position
            int x = SolarApocalypseMod.RANDOM.nextInt(16);
            int z = SolarApocalypseMod.RANDOM.nextInt(16);
            int worldSurface = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
            int motionBlocking = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x, z);

            // Decay blocks
            decayBlockPos(chunk, x, worldSurface, z, phase);
            if (worldSurface != motionBlocking) decayBlockPos(chunk, x, motionBlocking, z, phase);
        }
    }
    private static void decayBlockPos(WorldChunk chunk, int x, int y, int z, SolarPhase phase)
    {
        BlockPos pos = new BlockPos(x + chunk.getPos().getStartX(), y, z + chunk.getPos().getStartZ());

        // Apply solar decay
        BlockState block = chunk.getBlockState(new BlockPos(x, pos.getY(), z));
        BlockState decayed = phase.getDecayedBlockState(block);
        if (decayed != null) chunk.getWorld().setBlockState(pos, decayed);
    }
    //endregion

    public static void tickEntity(LivingEntity entity)
    {
        int light = entity.getWorld().getLightLevel(LightType.SKY, entity.getBlockPos());
        if (state.isEnabled() && entity.getWorld().getTimeOfDay() % 24000 < 12000 && light > 0)
        {
            int dayNumber = (int)(state.getCurrentTick() / 24000);
            if (dayNumber >= GlobalConfig.BURN_START_DAY)
            {
                float chance = (dayNumber - GlobalConfig.BURN_START_DAY + 1) * GlobalConfig.BURN_CHANCE_START;
                chance *= (light * light / 225.0f);
                if (SolarApocalypseMod.RANDOM.nextFloat() < chance) entity.setOnFireFor((float)SolarApocalypseMod.RANDOM.nextTriangular(GlobalConfig.BURN_DURATION, GlobalConfig.BURN_DEVIATION));
            }
        }
    }
}
