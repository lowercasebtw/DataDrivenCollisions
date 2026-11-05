package org.legacy.visuals.dynshapes;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legacy.visuals.dynshapes.impl.CollisionShape;
import org.legacy.visuals.dynshapes.impl.CollisionShapeSelectors;
import org.legacy.visuals.dynshapes.impl.CollisionShapesReloadListener;

import java.util.Map;

public class DynamicShapes implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    private static DynamicShapes INSTANCE;
    private final Map<Identifier, Map<BlockState, CollisionShape>> shapes = new Object2ReferenceArrayMap<>();

    public static DynamicShapes instance() {
        if (INSTANCE == null) {
            INSTANCE = new DynamicShapes();
        }

        return INSTANCE;
    }

    @Override
    public void onInitialize() {
//        DumpCollisionShapes.dump(); // UNCOMMENT to dump to ".minecraft/shapes_output"
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(CollisionShapesReloadListener.ID, new CollisionShapesReloadListener(CollisionShapeSelectors.CODEC));
    }

    public void populate(final Block block, final Map<BlockState, CollisionShape> map) {
        final Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        LOGGER.info("Populated block '{}' with {} mappings", id, map.size());
        this.shapes.put(id, map);
    }

    public VoxelShape get(final BlockState state) {
        final Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (this.shapes.containsKey(id)) {
            return this.shapes.get(id).getOrDefault(state, CollisionShape.EMPTY).shape();
        } else {
            return Shapes.empty();
        }
    }

    public void reset() {
        this.shapes.clear();
    }
}
