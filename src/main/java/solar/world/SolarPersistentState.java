package solar.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import solar.SolarApocalypseMod;

public class SolarPersistentState extends PersistentState
{
    private static final PersistentState.Type<SolarPersistentState> TYPE = new PersistentState.Type<>(SolarPersistentState::new, SolarPersistentState::fromNbt, null);
    
    private long currentTick;
    private boolean enabled;
    
    public SolarPersistentState() { this(0, false); }
    public SolarPersistentState(long currentTick, boolean enabled)
    {
        this.currentTick = currentTick;
        this.enabled = enabled;
    }
    
    public long getCurrentTick() { return currentTick; }
    public boolean isEnabled() { return enabled; }
    public void incrementTick() { currentTick++; markDirty(); }
    public void setEnabled(boolean enabled) { this.enabled = enabled; markDirty(); }
    public void reset()
    {
        currentTick = 0;
        enabled = false;
        markDirty();
    }
    
    public static SolarPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries)
    {
        long currentTick = nbt.getLong("CurrentTick");
        boolean enabled = nbt.getBoolean("Enabled");
        return new SolarPersistentState(currentTick, enabled);
    }
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries)
    {
        nbt.putLong("CurrentTick", currentTick);
        nbt.putBoolean("Enabled", enabled);
        return nbt;
    }
    
    public static SolarPersistentState getServerState(MinecraftServer server)
    {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(TYPE, SolarApocalypseMod.MODID);
    }
}
