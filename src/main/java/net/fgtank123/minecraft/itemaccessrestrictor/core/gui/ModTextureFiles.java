package net.fgtank123.minecraft.itemaccessrestrictor.core.gui;

import net.fgtank123.minecraft.itemaccessrestrictor.ModMain;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface ModTextureFiles {
    ResourceLocation MAIN = ModMain.makeId("textures/gui/main.png");
    ResourceLocation SETTING_STATES = ModMain.makeId("textures/gui/setting_states.png");
    Map<ResourceLocation, Pair<Integer, Integer>> WH_MAP = Stream.of(
        Pair.of(MAIN, Pair.of(64, 64)),
        Pair.of(SETTING_STATES, Pair.of(64, 64))
    ).reduce(
        new HashMap<>(), (map, entry) -> {
            map.put(entry.getKey(), entry.getValue());
            return map;
        },
        (map1, map2) -> {
            throw new UnsupportedOperationException();
        }
    );
}
