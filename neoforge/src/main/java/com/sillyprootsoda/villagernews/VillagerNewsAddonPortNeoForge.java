package com.sillyprootsoda.villagernews;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(VillagerNewsAddonPortNeoForge.MOD_ID)
public final class VillagerNewsAddonPortNeoForge {
	public static final String MOD_ID = "villagernews";
	public static final Logger LOGGER = LoggerFactory.getLogger("Villager News Addon Port");

	public VillagerNewsAddonPortNeoForge() {
		LOGGER.warn("Villager News (NeoForge) is a stub build - dialogue, voices, and sounds are not implemented "
			+ "on NeoForge yet. Install the Fabric build instead for the full mod.");
	}
}
