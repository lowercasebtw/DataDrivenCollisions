package org.legacy.visuals.dynshapes.impl;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class DynamicShapesRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DynamicShapesRegistry INSTANCE = new DynamicShapesRegistry();
    private final Map<Identifier, Map<BlockState, CollisionShape>> shapes = new Object2ReferenceArrayMap<>();

    DynamicShapesRegistry() {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(CollisionShapesReloadListener.ID, new CollisionShapesReloadListener(
                (block, map) -> {
                    final Identifier id = BuiltInRegistries.BLOCK.getKey(block);
                    LOGGER.info("Populated block '{}' with {} mappings", id, map.size());
                    this.shapes.put(id, map);
                },
                this.shapes::clear
        ));
    }

    public static DynamicShapesRegistry instance() {
        return INSTANCE;
    }

    public CollisionShape get(final BlockState state) {
        final Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (this.shapes.containsKey(id)) {
            return this.shapes.get(id).getOrDefault(state, CollisionShape.EMPTY);
        } else {
            return CollisionShape.EMPTY;
        }
    }
}
