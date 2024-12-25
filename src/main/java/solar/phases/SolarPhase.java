package solar.phases;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class SolarPhase
{
    private final SolarPhase parent;
    private final Map<BlockState, BlockState> blockStateMap = new HashMap<>();
    private final Map<Block, BlockState> blockMap = new HashMap<>();
    private final Map<Class<? extends Block>, Function<BlockState, BlockState>> typeMap = new HashMap<>();
    private final Map<Predicate<BlockState>, Function<BlockState, BlockState>> transformationMap = new HashMap<>();
    
    private final int startingDay;
    private final int startingInterval;
    private final int daysPerIntervalDecrease;
    
    public SolarPhase(int startingDay, int startingInterval, int daysPerIntervalDecrease)
    {
        this(null, startingDay, startingInterval, daysPerIntervalDecrease);
    }
    public SolarPhase(SolarPhase parent, int startingDay, int startingInterval, int daysPerIntervalDecrease)
    {
        this.parent = parent;
        this.startingDay = startingDay;
        this.startingInterval = startingInterval;
        this.daysPerIntervalDecrease = daysPerIntervalDecrease;
    }
    
    public long getStartingDay() { return startingDay; }
    public long getDecayInterval(int day)
    {
        int duration = (day - startingDay);
        return startingInterval - (duration / daysPerIntervalDecrease);
    }
    
    public SolarPhase with(BlockState from, BlockState to)
    {
        blockStateMap.put(from, to);
        return this;
    }
    public SolarPhase with(Block from, BlockState to)
    {
        blockMap.put(from, to);
        return this;
    }
    public SolarPhase with(Class<? extends Block> clazz, Function<BlockState, BlockState> mapper)
    {
        typeMap.put(clazz, mapper);
        return this;
    }
    public SolarPhase with(Predicate<BlockState> predicate, Function<BlockState, BlockState> mapper)
    {
        transformationMap.put(predicate, mapper);
        return this;
    }
    
    public BlockState getDecayedBlockState(BlockState original)
    {
        // Check BlockState Map
        BlockState decayed = blockStateMap.get(original);
        if (decayed != null) return decayed;
        
        // Check Block Map
        decayed = blockMap.get(original.getBlock());
        if (decayed != null) return decayed;
        
        // Check Type Map
        for (Map.Entry<Class<? extends Block>, Function<BlockState, BlockState>> typeMapping : typeMap.entrySet())
        {
            if (typeMapping.getKey().isAssignableFrom(original.getBlock().getClass())) decayed = typeMapping.getValue().apply(original);
            if (decayed != null) return decayed;
        }
        
        // Check Transformation Map
        for (Map.Entry<Predicate<BlockState>, Function<BlockState, BlockState>> transformation : transformationMap.entrySet()) if (transformation.getKey().test(original)) return transformation.getValue().apply(original);
        
        // Check Parent
        return parent != null ? parent.getDecayedBlockState(original) : null;
    }
}
