package com.sillyprootsoda.villagernews;

import com.sillyprootsoda.villagernews.command.DialogueTestCommand;
import com.sillyprootsoda.villagernews.config.VillagerNewsBuildSettings;
import com.sillyprootsoda.villagernews.config.VillagerNewsSettings;
import com.sillyprootsoda.villagernews.dialogue.ContextualDialogueController;
import com.sillyprootsoda.villagernews.dialogue.DialogueCatalog;
import com.sillyprootsoda.villagernews.item.VillagerNewsItems;
import com.sillyprootsoda.villagernews.network.DialogueAnimationPayload;
import com.sillyprootsoda.villagernews.network.HurtEffectPayload;
import com.sillyprootsoda.villagernews.network.VillagerNewsSettingsNetwork;
import com.sillyprootsoda.villagernews.network.VillagerNewsSettingsPayload;
import com.sillyprootsoda.villagernews.sound.SupplementalSoundCatalog;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerNewsAddonPort implements ModInitializer {
	public static final String MOD_ID = "villagernews";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		VillagerNewsItems.register();
		PayloadTypeRegistry.clientboundPlay().register(DialogueAnimationPayload.TYPE, DialogueAnimationPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HurtEffectPayload.TYPE, HurtEffectPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(VillagerNewsSettingsPayload.TYPE, VillagerNewsSettingsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(VillagerNewsSettingsPayload.TYPE, VillagerNewsSettingsPayload.CODEC);
		VillagerNewsSettings.load();
		VillagerNewsSettingsNetwork.register();
		SupplementalSoundCatalog.register();
		DialogueCatalog.register();
		ContextualDialogueController.register();
		if (VillagerNewsBuildSettings.dialogueTestCommand()) DialogueTestCommand.register();
		LOGGER.info("Villager News models, textures, and contextual dialogue are ready.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
