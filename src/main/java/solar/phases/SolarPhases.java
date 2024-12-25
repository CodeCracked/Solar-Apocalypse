package solar.phases;

import net.minecraft.block.*;
import solar.SolarApocalypseMod;

public class SolarPhases
{
    public static final SolarPhase PHASE_1 = new SolarPhase(0, 5, 2)
            .with(Blocks.GRASS_BLOCK, Blocks.DIRT.getDefaultState())
            .with(Blocks.PODZOL, Blocks.DIRT.getDefaultState())
            .with(Blocks.MYCELIUM, Blocks.DIRT.getDefaultState())
            
            .with(Blocks.SNOW, Blocks.AIR.getDefaultState())
            .with(Blocks.SNOW_BLOCK, Blocks.AIR.getDefaultState())
            .with(Blocks.POWDER_SNOW, Blocks.AIR.getDefaultState())
            
            .with(Blocks.BLUE_ICE, Blocks.PACKED_ICE.getDefaultState())
            .with(Blocks.PACKED_ICE, Blocks.ICE.getDefaultState())
            .with(Blocks.ICE, Blocks.WATER.getDefaultState())
            
            .with(Blocks.SUGAR_CANE, Blocks.AIR.getDefaultState())
            .with(Blocks.CACTUS, Blocks.AIR.getDefaultState())
            
            .with(TallPlantBlock.class, original -> Blocks.AIR.getDefaultState())
            .with(PlantBlock.class, original ->
            {
                if (SolarApocalypseMod.RANDOM.nextFloat() < 0.05f) return Blocks.DEAD_BUSH.getDefaultState();
                else return Blocks.AIR.getDefaultState();
            })
            .with(LeavesBlock.class, original -> Blocks.AIR.getDefaultState());
    
    public static final SolarPhase PHASE_2 = new SolarPhase(1, 5, 2)
            .with(Blocks.DIRT, Blocks.COARSE_DIRT.getDefaultState())
            .with(Blocks.DIRT_PATH, Blocks.COARSE_DIRT.getDefaultState())
            .with(Blocks.WATER, Blocks.AIR.getDefaultState())
            .with(AbstractBlock.AbstractBlockState::isBurnable, original ->
            {
                if (SolarApocalypseMod.RANDOM.nextFloat() < 0.1f) return Blocks.FIRE.getDefaultState();
                else return null;
            });
    
    public static final SolarPhase PHASE_3 = new SolarPhase(2, 5, 2)
            .with(Blocks.COARSE_DIRT, Blocks.AIR.getDefaultState())
            .with(Blocks.SAND, Blocks.GLASS.getDefaultState())
            .with(Blocks.STONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState())
            .with(Blocks.GRANITE, Blocks.COBBLESTONE.getDefaultState())
            .with(Blocks.DIORITE, Blocks.COBBLESTONE.getDefaultState())
            .with(Blocks.ANDESITE, Blocks.COBBLESTONE.getDefaultState())
            .with(AbstractBlock.AbstractBlockState::isBurnable, original ->
            {
                if (SolarApocalypseMod.RANDOM.nextFloat() < 0.25f) return Blocks.FIRE.getDefaultState();
                else return null;
            });
    
    public static final SolarPhase PHASE_4 = new SolarPhase(3, 5, 2)
            .with(Blocks.COBBLESTONE, Blocks.GRAVEL.getDefaultState())
            .with(Blocks.GLASS, Blocks.AIR.getDefaultState())
            .with(Blocks.SANDSTONE, Blocks.AIR.getDefaultState())
            .with(AbstractBlock.AbstractBlockState::isBurnable, original ->
            {
                if (SolarApocalypseMod.RANDOM.nextFloat() < 0.5f) return Blocks.FIRE.getDefaultState();
                else return null;
            });
    
    public static final SolarPhase FINAL_PHASE = new SolarPhase(5, 7, 1)
            .with(Blocks.BEDROCK, Blocks.BEDROCK.getDefaultState())
            .with(Blocks.BARRIER, Blocks.BARRIER.getDefaultState())
            .with(Blocks.END_PORTAL, Blocks.END_PORTAL.getDefaultState())
            .with(Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL_FRAME.getDefaultState())
            .with(Blocks.END_GATEWAY, Blocks.END_GATEWAY.getDefaultState())
            .with(CommandBlock.class, original -> original)
            .with(test -> true, original -> Blocks.AIR.getDefaultState());
}
