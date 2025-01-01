package solar.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class NbtUtils
{
    public static <Key, Value> NbtCompound writeMap(Map<Key, Value> map, Function<Key, NbtElement> keySerializer, Function<Value, NbtElement> valueSerializer)
    {
        NbtList keyList = new NbtList();
        NbtList valueList = new NbtList();
        for (var entry : map.entrySet())
        {
            keyList.add(keySerializer.apply(entry.getKey()));
            valueList.add(valueSerializer.apply(entry.getValue()));
        }

        NbtCompound nbt = new NbtCompound();
        nbt.put("Keys", keyList);
        nbt.put("Values", valueList);
        return nbt;
    }

    public static <Key, Value> Map<Key, Value> readMap(NbtCompound nbt, Function<NbtElement, Key> keyDeserializer, Function<NbtElement, Value> valueDeserializer)
    {
        return readMap(nbt, HashMap::new, keyDeserializer, valueDeserializer);
    }
    public static <Key, Value> void loadMap(NbtCompound nbt, Map<Key, Value> map, Function<NbtElement, Key> keyDeserializer, Function<NbtElement, Value> valueDeserializer)
    {
        readMap(nbt, () -> map, keyDeserializer, valueDeserializer);
    }
    public static <Key, Value> Map<Key, Value> readMap(NbtCompound nbt, Supplier<Map<Key, Value>> mapProvider, Function<NbtElement, Key> keyDeserializer, Function<NbtElement, Value> valueDeserializer)
    {
        NbtList keyList = nbt.getList("Keys", NbtElement.LIST_TYPE);
        NbtList valueList = nbt.getList("Values", NbtElement.LIST_TYPE);

        Map<Key, Value> map = mapProvider.get();
        for (int i = 0; i < keyList.size(); i++)
        {
            Key key = keyDeserializer.apply(keyList.get(i));
            Value value = valueDeserializer.apply(valueList.get(i));
            map.put(key, value);
        }
        return map;
    }
}
