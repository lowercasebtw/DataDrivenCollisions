package org.legacy.visuals.dynshapes.impl;

import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public record CollisionShapeSelectors(Map<String, CollisionShape> mappings) {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Codec<CollisionShapeSelectors> CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap(Codec.STRING, Codec.withAlternative(CollisionShape.CODEC, CollisionShape.LIST_CODEC)))
            .xmap(CollisionShapeSelectors::new, CollisionShapeSelectors::mappings);

    public Map<BlockState, CollisionShape> parse(final StateDefinition<Block, BlockState> stateDefinition) {
        final Map<BlockState, CollisionShape> shapes = new HashMap<>();
        this.mappings.forEach((selectorString, either) -> {
            try {
                final Predicate<StateHolder<Block, BlockState>> selector = VariantSelector.predicate(stateDefinition, selectorString);
                for (final BlockState state : stateDefinition.getPossibleStates()) {
                    if (selector.test(state)) {
                        shapes.put(state, either);
                    }
                }
            } catch (final Exception exception) {
                LOGGER.warn("Exception loading blockshapes definition for variant: '{}'", selectorString);
                exception.printStackTrace();
            }
        });
        return shapes;
    }
}