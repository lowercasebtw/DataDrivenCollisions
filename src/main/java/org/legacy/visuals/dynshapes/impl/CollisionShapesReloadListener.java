package org.legacy.visuals.dynshapes.impl;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.function.BiConsumer;

public class CollisionShapesReloadListener extends SimpleJsonResourceReloadListener<CollisionShapeSelectors> {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FOLDER_NAME = "blockshapes";
    public static final Identifier ID = Identifier.withDefaultNamespace(FOLDER_NAME);
    public static final FileToIdConverter FILE_TO_ID_CONVERTER = FileToIdConverter.json(FOLDER_NAME);

    private final BiConsumer<Block, Map<BlockState, CollisionShape>> populator;
    private final Runnable clear;

    public CollisionShapesReloadListener(final BiConsumer<Block, Map<BlockState, CollisionShape>> populator, final Runnable clear) {
        super(CollisionShapeSelectors.CODEC, FILE_TO_ID_CONVERTER);
        this.populator = populator;
        this.clear = clear;
    }

    @Override
    public void prepareSharedState(final @NonNull SharedState currentReload) {
    }

    @Override
    protected void apply(final @NonNull Map<Identifier, CollisionShapeSelectors> preparations, final @NonNull ResourceManager manager, final @NonNull ProfilerFiller profiler) {
        this.clear.run();
        for (final Map.Entry<Identifier, CollisionShapeSelectors> entry : preparations.entrySet()) {
            final Identifier id = entry.getKey();
            final CollisionShapeSelectors selectors = entry.getValue();
            BuiltInRegistries.BLOCK.get(id).ifPresentOrElse(it -> this.populator.accept(it.value(), selectors.parse(it.value().getStateDefinition())), () -> LOGGER.warn("Failed to read block '{}' when parsing block mappings.", id));
        }
    }
}
