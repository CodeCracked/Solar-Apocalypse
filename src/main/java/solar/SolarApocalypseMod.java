package solar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solar.commands.SolarCommand;
import solar.utils.VersionChecker;

public class SolarApocalypseMod implements ModInitializer, ClientModInitializer
{
    public static final String MODID = "solar";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Random RANDOM = Random.createThreadSafe();

    private boolean ranVersionCheck;
    
    @Override
    public void onInitialize()
    {
        SolarManager.init();
    
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SolarCommand.register(dispatcher));
    }
    @Override
    public void onInitializeClient()
    {
        ClientEntityEvents.ENTITY_LOAD.register(this::onWorldLoaded);
    }
    
    private void onWorldLoaded(Entity entity, ClientWorld world)
    {
        if (entity instanceof PlayerEntity)
        {
            if (!ranVersionCheck)
            {
                ranVersionCheck = true;
                VersionChecker.doVersionCheck();
            }
        }
    }
}
