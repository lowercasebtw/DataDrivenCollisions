package org.legacy.visuals.dynshapes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class DumpCollisionShapes {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path OUTPUT_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("shapes_output");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static void dump() {
        try {
            if (Files.exists(OUTPUT_PATH)) {
                Files.deleteIfExists(OUTPUT_PATH);
            }

            Files.createDirectories(OUTPUT_PATH);
        } catch (final IOException ignored) {
            LOGGER.error("Failed to dump collisions, unable to create/delete states folder!");
            return;
        }

        for (final Block block : BuiltInRegistries.BLOCK) {
            final Identifier id = BuiltInRegistries.BLOCK.getKey(block);

            final JsonObject definition = new JsonObject();
            for (final BlockState state : block.getStateDefinition().getPossibleStates()) {
                final VoxelShape collision = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (collision.isEmpty()) continue;

                final JsonArray boxes = new JsonArray();
                collision.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> boxes.add(encode(minX, minY, minZ, maxX, maxY, maxZ)));

                final String key = state.getValues().map(Property.Value::toString).collect(Collectors.joining(","));
                if (boxes.size() > 1) {
                    definition.add(key, boxes);
                } else {
                    definition.add(key, boxes.get(0));
                }
            }

            if (!definition.isEmpty()) {
                final Path filePath = OUTPUT_PATH.resolve(id.getNamespace() + "/" + id.getPath() + ".json");
                try {
                    Files.createDirectories(filePath.getParent());

                    LOGGER.info("Saved '{}' to file", id);
                    Files.writeString(filePath, GSON.toJson(definition));
                } catch (final IOException exception) {
                    LOGGER.error("Failed to save definition for '{}'", id);
                    exception.printStackTrace();
                }
            }
        }
    }

    private static JsonObject encode(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final JsonObject output = new JsonObject();

        {
            final JsonArray data = new JsonArray();
            data.add(minX);
            data.add(minY);
            data.add(minZ);
            output.add("min", data);
        }

        {
            final JsonArray data = new JsonArray();
            data.add(maxX);
            data.add(maxY);
            data.add(maxZ);
            output.add("max", data);
        }

        return output;
    }
}
