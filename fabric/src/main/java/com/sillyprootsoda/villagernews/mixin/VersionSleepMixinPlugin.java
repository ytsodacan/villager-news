package com.sillyprootsoda.villagernews.mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VersionSleepMixinPlugin implements IMixinConfigPlugin {
	private static final String RETURNABLE = "com.sillyprootsoda.villagernews.mixin.VillagerSleepMixinReturnable";
	private static final String VOID = "com.sillyprootsoda.villagernews.mixin.VillagerSleepMixinVoid";

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (!mixinClassName.equals(RETURNABLE) && !mixinClassName.equals(VOID)) return true;
		boolean startSleepingReturnsBoolean = startSleepingReturnsBoolean();
		if (mixinClassName.equals(RETURNABLE)) return startSleepingReturnsBoolean;
		return !startSleepingReturnsBoolean;
	}

	// Reads the raw class bytes with ASM instead of Class.forName - actually
	// loading LivingEntity here (even without initializing it) would mark it
	// "already loaded" before Mixin gets a chance to transform it.
	private static boolean startSleepingReturnsBoolean() {
		String resource = "net/minecraft/world/entity/LivingEntity.class";
		try (InputStream stream = VersionSleepMixinPlugin.class.getClassLoader().getResourceAsStream(resource)) {
			if (stream == null) return false;
			AtomicBoolean returnsBoolean = new AtomicBoolean(false);
			new ClassReader(stream).accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					if (name.equals("startSleeping") && descriptor.equals("(Lnet/minecraft/core/BlockPos;)Z")) {
						returnsBoolean.set(true);
					}
					return null;
				}
			}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return returnsBoolean.get();
		} catch (IOException exception) {
			return false;
		}
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
