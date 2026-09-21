package com.sillyprootsoda.villagernews.network;

import com.sillyprootsoda.villagernews.VillagerNewsAddonPort;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record HurtEffectPayload(UUID entityId, String effectId) implements CustomPacketPayload {
	public static final Type<HurtEffectPayload> TYPE = new Type<>(VillagerNewsAddonPort.id("hurt_effect"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HurtEffectPayload> CODEC = new StreamCodec<>() {
		@Override
		public HurtEffectPayload decode(RegistryFriendlyByteBuf buffer) {
			return new HurtEffectPayload(buffer.readUUID(), buffer.readUtf(2));
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buffer, HurtEffectPayload payload) {
			buffer.writeUUID(payload.entityId());
			buffer.writeUtf(payload.effectId(), 2);
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
