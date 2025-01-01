package solar;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TitleCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import solar.interfaces.SolarChunk;
import solar.interfaces.SolarItemEntity;
import solar.phases.SolarPhase;
import solar.phases.SolarPhases;
import solar.world.LoadedChunks;
import solar.world.SolarPersistentState;

import java.util.Set;

public final class SolarManager
{
    private static final ItemStack SOLAR_EMBER = new ItemStack(Registries.ITEM.getEntry(Identifier.ofVanilla("blaze_powder")).get(), 1, ComponentChanges.builder()
            .add(DataComponentTypes.ITEM_NAME, Text.of("Solar Ember"))
            .add(DataComponentTypes.RARITY, Rarity.RARE)
            .build());
    private static final ItemStack FLARE_SHARD = new ItemStack(Registries.ITEM.getEntry(Identifier.ofVanilla("fire_charge")).get(), 1, ComponentChanges.builder()
            .add(DataComponentTypes.ITEM_NAME, Text.of("Flare Shard"))
            .add(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .add(DataComponentTypes.RARITY, Rarity.EPIC)
            .build());

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
        ServerPlayerEvents.AFTER_RESPAWN.register(SolarManager::onPlayerRespawn);
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
                if (key.getValue().equals(Identifier.ofVanilla("overworld"))) tick(world);
            });
        }
    }
    public static void onPlayerJoin(ServerPlayerEntity player)
    {
        if (player.getMaxAbsorption() == 0.0)
        {
            resetHealthAttributes(player);
            resetHealth(player);
        }
    }
    public static void onPlayerRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive)
    {
        state.removeLife(newPlayer);
        if (state.getLives(newPlayer) <= 0)
        {
            newPlayer.changeGameMode(GameMode.SPECTATOR);
            newPlayer.sendMessage(Text.of("You have run out of lives!").copy().formatted(Formatting.DARK_RED), true);
        }
        else
        {
            resetHealthAttributes(newPlayer);
            resetHealth(newPlayer);
        }
    }
    public static void resetHealthAttributes(ServerPlayerEntity player)
    {
        int lives = state.getLives(player);
        player.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH).setBaseValue(lives * 2.0);
        player.getAttributes().getCustomInstance(EntityAttributes.MAX_ABSORPTION).setBaseValue(2.0 * (20 - lives));
    }
    public static void resetHealth(ServerPlayerEntity player)
    {
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(player.getMaxAbsorption());
    }
    //endregion
    //region Ticking
    public static void tick(ServerWorld world)
    {
        try
        {
            // Day Tick
            if (state.getCurrentTick() % 24000 < 12000)
            {
                // Chunk Decay
                for (WorldChunk chunk : LoadedChunks.loadedChunksIterator()) tickChunk(chunk);

                // Solar Embers
                if (SolarApocalypseMod.RANDOM.nextFloat() < GlobalConfig.EMBER_CHANCE) spawnEmber(world, true);
            }

            // Night Tick
            else
            {
                // Update Chunk Tick Field
                for (WorldChunk chunk : LoadedChunks.loadedChunksIterator()) ((SolarChunk)chunk).setLastSolarTick(state.getCurrentTick());
            }

            // Increment Solar Tick
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
    //region Embers
    public static void spawnEmber(ServerWorld world, boolean isFlare)
    {
        ServerPlayerEntity nearPlayer = world.getRandomAlivePlayer();
        if (nearPlayer == null) return;

        int dx = SolarApocalypseMod.RANDOM.nextBetween(-GlobalConfig.EMBER_SPAWN_RADIUS, GlobalConfig.EMBER_SPAWN_RADIUS);
        int dz = SolarApocalypseMod.RANDOM.nextBetween(-GlobalConfig.EMBER_SPAWN_RADIUS, GlobalConfig.EMBER_SPAWN_RADIUS);
        int y = world.getTopYInclusive() + GlobalConfig.EMBER_SPAWN_HEIGHT;

        ItemStack stack = SOLAR_EMBER;
        if (isFlare && SolarApocalypseMod.RANDOM.nextFloat() < GlobalConfig.FLARE_SHARD_CHANCE) stack = FLARE_SHARD;
        else isFlare = false;

        ItemEntity ember = new ItemEntity(world, nearPlayer.getX() + dx, y, nearPlayer.getZ() + dz, stack.copy());
        SolarItemEntity solarItem = (SolarItemEntity) ember;
        solarItem.setDespawnTime(GlobalConfig.EMBER_DESPAWN_TIME);
        if (isFlare) solarItem.markAsFlare();
        else solarItem.markAsEmber();

        ember.setVelocity(0, 0, 0);
        world.spawnEntity(ember);
    }
    public static void createParticles(ServerWorld world, Vec3d pos, boolean ember, boolean flare, int vigorMultiplier)
    {
        if (ember || flare)
        {
            // Create Smoke Particles
            ParticleEffect particle = ParticleTypes.SMOKE;
            world.spawnParticles(particle, true, false, pos.x, pos.y, pos.z, 10 * vigorMultiplier, 0.0D, 0.0D, 0.0D, 0.01D * vigorMultiplier);
        }
        if (ember)
        {
            // Create Ember Particles
            ParticleEffect particle = ParticleTypes.LAVA;
            world.spawnParticles(particle, true, false, pos.x, pos.y, pos.z, 3 * vigorMultiplier, 0.1D * vigorMultiplier, 0.1D * vigorMultiplier, 0.1D * vigorMultiplier, 0.01D * vigorMultiplier * vigorMultiplier);
        }
        if (flare)
        {
            // Create Flare Particles
            ParticleEffect particle = ParticleTypes.FLAME;
            world.spawnParticles(particle, true, true, pos.x, pos.y, pos.z, 20 * vigorMultiplier, 0.0D, 0.0D, 0.0D, 0.06 * vigorMultiplier);
        }
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
