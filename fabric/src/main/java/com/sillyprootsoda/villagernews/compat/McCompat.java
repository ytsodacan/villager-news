package com.sillyprootsoda.villagernews.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.Predicate;

public final class McCompat {
	private McCompat() {
	}

	@SuppressWarnings("unchecked")
	public static final EntityType<Villager> VILLAGER = (EntityType<Villager>) entityType("VILLAGER");
	@SuppressWarnings("unchecked")
	public static final EntityType<Sheep> SHEEP = (EntityType<Sheep>) entityType("SHEEP");
	@SuppressWarnings("unchecked")
	public static final EntityType<WanderingTrader> WANDERING_TRADER = (EntityType<WanderingTrader>) entityType("WANDERING_TRADER");

	private static Object entityType(String fieldName) {
		for (String className : new String[] {"net.minecraft.world.entity.EntityTypes", "net.minecraft.world.entity.EntityType"}) {
			try {
				Class<?> holder = Class.forName(className);
				return holder.getField(fieldName).get(null);
			} catch (ReflectiveOperationException ignored) {
			}
		}
		throw new IllegalStateException("Could not resolve EntityType." + fieldName);
	}

	private static final MethodHandle ROTATE = resolveRotate();

	private static MethodHandle resolveRotate() {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		for (String name : new String[] {"rotate", "mulPose"}) {
			try {
				return lookup.findVirtual(PoseStack.class, name, MethodType.methodType(void.class, Quaternionfc.class));
			} catch (ReflectiveOperationException ignored) {
			}
		}
		throw new IllegalStateException("No compatible PoseStack rotate method found");
	}

	public static void rotateDegrees(PoseStack poseStack, Axis axis, float degrees) {
		try {
			ROTATE.invoke(poseStack, (Quaternionfc) axis.rotationDegrees(degrees));
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	private static final MethodHandle SET_PERMANENTLY_INVULNERABLE = resolveSetInvulnerable();

	private static MethodHandle resolveSetInvulnerable() {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		for (String name : new String[] {"setPermanentlyInvulnerable", "setInvulnerable"}) {
			try {
				return lookup.findVirtual(Entity.class, name, MethodType.methodType(void.class, boolean.class));
			} catch (ReflectiveOperationException ignored) {
			}
		}
		throw new IllegalStateException("No compatible Entity invulnerable setter found");
	}

	public static void setPermanentlyInvulnerable(Entity entity, boolean value) {
		try {
			SET_PERMANENTLY_INVULNERABLE.invoke(entity, value);
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	private static final MethodHandle RANDOM_TELEPORT = resolveRandomTeleport();

	private static MethodHandle resolveRandomTeleport() {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		try {
			MethodHandle handle = lookup.findVirtual(LivingEntity.class, "randomTeleport",
				MethodType.methodType(boolean.class, double.class, double.class, double.class, boolean.class, Predicate.class));
			Predicate<BlockState> alwaysFalse = state -> false;
			return MethodHandles.insertArguments(handle, 5, alwaysFalse);
		} catch (ReflectiveOperationException ignored) {
		}
		try {
			return lookup().findVirtual(LivingEntity.class, "randomTeleport",
				MethodType.methodType(boolean.class, double.class, double.class, double.class, boolean.class));
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("No compatible LivingEntity.randomTeleport found", exception);
		}
	}

	private static MethodHandles.Lookup lookup() {
		return MethodHandles.publicLookup();
	}

	public static boolean randomTeleport(LivingEntity entity, double x, double y, double z, boolean particleEffects) {
		try {
			return (boolean) RANDOM_TELEPORT.invoke(entity, x, y, z, particleEffects);
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean isSulfurCubeHot(DamageSource source) {
		try {
			Field field = DamageTypes.class.getField("SULFUR_CUBE_HOT");
			ResourceKey<DamageType> key = (ResourceKey<DamageType>) field.get(null);
			return source.is(key);
		} catch (ReflectiveOperationException exception) {
			return false;
		}
	}
}
