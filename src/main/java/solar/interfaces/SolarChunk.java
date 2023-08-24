package solar.interfaces;

public interface SolarChunk
{
    boolean isLoaded();
    void setLoaded(boolean loaded);
    
    long getLastSolarTick();
    void setLastSolarTick(long lastSolarTick);
    void incrementSolarTick();
}
