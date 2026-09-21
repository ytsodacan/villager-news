package com.sillyprootsoda.villagernews.sound;

import com.sillyprootsoda.villagernews.VillagerNewsAddonPort;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class SupplementalSoundCatalog {
	private static final List<String> ADULT_HURT_EFFECTS = List.of("a", "d", "g", "j", "l", "n", "r", "t", "v");
	private static final List<String> BABY_HURT_EFFECTS = List.of("b", "e", "h", "k", "m", "o", "s", "u");
	private static final List<String> EFFECTS = List.of(
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v"
	);
	private static final Map<String, SoundEvent> REGISTERED = new LinkedHashMap<>();

	private SupplementalSoundCatalog() {
	}

	public static void register() {
		for (String effect : EFFECTS) {
			Identifier id = VillagerNewsAddonPort.id("effect." + effect);
			REGISTERED.put(effect, Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
		}
	}

	public static String chooseHurtEffect(boolean baby) {
		List<String> effects = baby ? BABY_HURT_EFFECTS : ADULT_HURT_EFFECTS;
		return effects.get(ThreadLocalRandom.current().nextInt(effects.size()));
	}

	public static SoundEvent byId(String effect) {
		return REGISTERED.get(effect);
	}
}
