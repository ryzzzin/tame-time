package dev.rysin.tametime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
			if (!(entity instanceof Rabbit rabbit)) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);
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

			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}

			if (rabbit.getRandom().nextInt(3) == 0) {
				taming.tameTime$setTamed(true);
				taming.tameTime$setOwnerUuid(player.getUUID());
				rabbit.setPersistenceRequired();
				tameParticles((ServerLevel) world, rabbit, true);
			} else {
				tameParticles((ServerLevel) world, rabbit, false);
			}

			return InteractionResult.CONSUME;
		});
	}

	private static void tameParticles(ServerLevel world, Rabbit rabbit, boolean success) {
		if (success) {
			world.sendParticles(ParticleTypes.HEART, rabbit.getX(), rabbit.getY() + 0.5D, rabbit.getZ(), 7, 0.35D, 0.35D, 0.35D, 0.0D);
		} else {
			world.sendParticles(ParticleTypes.SMOKE, rabbit.getX(), rabbit.getY() + 0.5D, rabbit.getZ(), 7, 0.35D, 0.35D, 0.35D, 0.01D);
		}
	}
}
