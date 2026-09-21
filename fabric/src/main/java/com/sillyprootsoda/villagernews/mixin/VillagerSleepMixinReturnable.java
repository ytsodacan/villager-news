package com.sillyprootsoda.villagernews.mixin;

import com.sillyprootsoda.villagernews.dialogue.ContextualDialogueController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class VillagerSleepMixinReturnable {
	@Inject(method = "startSleeping", at = @At("HEAD"), cancellable = true)
	private void vnap$delaySleepUntilBedtimeLineFinishes(BlockPos bedPos, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Villager villager && ContextualDialogueController.delayVillagerSleep(villager, bedPos)) cir.setReturnValue(false);
	}
}
