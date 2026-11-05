package org.legacy.visuals.dynshapes.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.legacy.visuals.dynshapes.DynamicShapes;

import java.util.Map;

public class CollisionShapesReloadListener extends SimpleJsonResourceReloadListener<CollisionShapeSelectors> {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FOLDER_NAME = "blockshapes";
    public static final Identifier ID = Identifier.withDefaultNamespace(FOLDER_NAME);
    public static final FileToIdConverter FILE_TO_ID_CONVERTER = FileToIdConverter.json(FOLDER_NAME);

    public CollisionShapesReloadListener(final Codec<CollisionShapeSelectors> codec) {
        super(codec, FILE_TO_ID_CONVERTER);
    }

    @Override
    public void prepareSharedState(final @NonNull SharedState currentReload) {
    }

    @Override
    protected void apply(final @NonNull Map<Identifier, CollisionShapeSelectors> preparations, final @NonNull ResourceManager manager, final @NonNull ProfilerFiller profiler) {
        final DynamicShapes instance = DynamicShapes.instance();
        instance.reset();
        for (final Map.Entry<Identifier, CollisionShapeSelectors> entry : preparations.entrySet()) {
            final Identifier id = entry.getKey();
            final CollisionShapeSelectors selectors = entry.getValue();
            BuiltInRegistries.BLOCK.get(id).ifPresentOrElse(it -> instance.populate(it.value(), selectors.parse(it.value().getStateDefinition())), () -> LOGGER.warn("Failed to read block '{}' when parsing block mappings.", id));
        }
    }
}
