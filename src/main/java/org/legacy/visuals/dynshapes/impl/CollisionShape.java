package org.legacy.visuals.dynshapes.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3fc;

import java.util.Collections;

public record CollisionShape(VoxelShape shape) {
    public static final Codec<CollisionShape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.VECTOR3F.fieldOf("min").forGetter(null),
            ExtraCodecs.VECTOR3F.fieldOf("max").forGetter(null)
    ).apply(instance, CollisionShape::new));

    public static final CollisionShape EMPTY = new CollisionShape(Shapes.empty());

    public static final Codec<CollisionShape> LIST_CODEC = CODEC.listOf(1, Integer.MAX_VALUE).xmap(shapes -> {
        if (shapes.size() > 1) {
            VoxelShape shape = Shapes.empty();
            for (final CollisionShape collisionShape : shapes) {
                shape = Shapes.join(shape, collisionShape.shape, BooleanOp.OR);
            }

            return new CollisionShape(shape.optimize());
        } else {
            return shapes.getFirst();
        }
    }, Collections::singletonList);

    public CollisionShape(final Vector3fc min, final Vector3fc max) {
        this(Shapes.box(min.x(), min.y(), min.z(), max.x(), max.y(), max.z()));
    }
}