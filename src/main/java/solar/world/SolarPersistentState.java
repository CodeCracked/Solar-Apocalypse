package solar.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import solar.SolarApocalypseMod;
import solar.utils.NbtUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolarPersistentState extends PersistentState
{
    private static final PersistentState.Type<SolarPersistentState> TYPE = new PersistentState.Type<>(SolarPersistentState::new, SolarPersistentState::fromNbt, null);
    
    private long currentTick;
    private boolean enabled;
    private int defaultLifeCount;
    private final Map<UUID, Integer> playerLives;

    public SolarPersistentState() { this(0, false, 5, new HashMap<>()); }
    public SolarPersistentState(long currentTick, boolean enabled, int defaultLifeCount, Map<UUID, Integer> playerLives)
    {
        this.currentTick = currentTick;
        this.enabled = enabled;
        this.defaultLifeCount = defaultLifeCount;
        this.playerLives = playerLives;
    }
    
    public long getCurrentTick() { return currentTick; }
    public boolean isEnabled() { return enabled; }
    public int getDefaultLifeCount() { return defaultLifeCount; }

    public void incrementTick() { currentTick++; markDirty(); }
    public void setEnabled(boolean enabled) { this.enabled = enabled; markDirty(); }
    public void setDefaultLifeCount(int defaultLifeCount) { this.defaultLifeCount = defaultLifeCount; markDirty(); }

    public void reset()
    {
        currentTick = 0;
        enabled = false;
        defaultLifeCount = 5;
        playerLives.clear();
        markDirty();
    }

    public int getLives(ServerPlayerEntity player)
    {
        return playerLives.computeIfAbsent(player.getUuid(), k -> defaultLifeCount);
    }
    public void removeLife(ServerPlayerEntity player)
    {
        playerLives.put(player.getUuid(), getLives(player) - 1);
        markDirty();
    }
    public void setLives(ServerPlayerEntity player, int lives)
    {
        playerLives.put(player.getUuid(), lives);
        markDirty();
    }

    public static SolarPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries)
    {
        long currentTick = nbt.getLong("CurrentTick");
        boolean enabled = nbt.getBoolean("Enabled");
        int defaultLifeCount = nbt.contains("DefaultLifeCount", NbtElement.BYTE_TYPE) ? nbt.getByte("DefaultLifeCount") : 5;

        Map<UUID, Integer> playerLives = new HashMap<>();
        if (nbt.contains("PlayerLives", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound playerLivesNbt = nbt.getCompound("PlayerLives");
            NbtUtils.loadMap(playerLivesNbt, playerLives, NbtHelper::toUuid, (nbtInt) -> ((NbtInt) nbtInt).intValue());
        }

        return new SolarPersistentState(currentTick, enabled, defaultLifeCount, playerLives);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries)
    {
        nbt.putLong("CurrentTick", currentTick);
        nbt.putBoolean("Enabled", enabled);
        nbt.putByte("DefaultLifeCount", (byte) defaultLifeCount);
        nbt.put("PlayerLives", NbtUtils.writeMap(playerLives, NbtHelper::fromUuid, NbtInt::of));
        return nbt;
    }
    
    public static SolarPersistentState getServerState(MinecraftServer server)
    {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(TYPE, SolarApocalypseMod.MODID);
    }
}
