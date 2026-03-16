package dev.rysin.tametime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameTimeMod implements ModInitializer {
	public static final String MOD_ID = "tame_time";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Tame Time initialized");

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (entity instanceof Rabbit rabbit) {
				return tryTameRabbit(player.getUUID(), player.getAbilities().instabuild, world, stack, rabbit);
			}
			if (entity instanceof Fox fox) {
				return tryTameFox(player.getUUID(), player.getAbilities().instabuild, world, stack, fox);
			}
			return InteractionResult.PASS;
		});
	}

	private static InteractionResult tryTameRabbit(java.util.UUID playerUuid, boolean instabuild, Level world, ItemStack stack, Rabbit rabbit) {
		if (!stack.is(Items.DANDELION)) {
			return InteractionResult.PASS;
		}

		RabbitTamingAccess taming = (RabbitTamingAccess) rabbit;
		if (taming.tameTime$isTamed()) {
			return InteractionResult.PASS;
		}

		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (!instabuild) {
			stack.shrink(1);
		}

		if (rabbit.getRandom().nextInt(3) == 0) {
			taming.tameTime$setTamed(true);
			taming.tameTime$setOwnerUuid(playerUuid);
			rabbit.setPersistenceRequired();
			tameParticles((ServerLevel) world, rabbit, true);
		} else {
			tameParticles((ServerLevel) world, rabbit, false);
		}

		return InteractionResult.CONSUME;
	}

	private static InteractionResult tryTameFox(java.util.UUID playerUuid, boolean instabuild, Level world, ItemStack stack, Fox fox) {
		if (!stack.is(Items.RABBIT_FOOT)) {
			return InteractionResult.PASS;
		}

		FoxTamingAccess taming = (FoxTamingAccess) fox;
		if (taming.tameTime$isTamed()) {
			return InteractionResult.PASS;
		}

		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (!instabuild) {
			stack.shrink(1);
		}

		if (fox.getRandom().nextInt(3) == 0) {
			taming.tameTime$setTamed(true);
			taming.tameTime$setOwnerUuid(playerUuid);
			fox.setPersistenceRequired();
			tameParticles((ServerLevel) world, fox, true);
		} else {
			tameParticles((ServerLevel) world, fox, false);
		}

		return InteractionResult.CONSUME;
	}

	private static void tameParticles(ServerLevel world, Entity entity, boolean success) {
		if (success) {
			world.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + 0.5D, entity.getZ(), 7, 0.35D, 0.35D, 0.35D, 0.0D);
		} else {
			world.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 0.5D, entity.getZ(), 7, 0.35D, 0.35D, 0.35D, 0.01D);
		}
	}
}
