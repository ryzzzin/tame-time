package dev.rysin.tametime.mixin;

import dev.rysin.tametime.RabbitTamingAccess;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Rabbit.class)
public abstract class RabbitTamingMixin extends Animal implements RabbitTamingAccess {
	@Unique
	private static final String TAME_TIME_TAMED_TAG = "TameTimeTamed";
	@Unique
	private static final String TAME_TIME_OWNER_TAG = "TameTimeOwner";
	@Unique
	private static final int TELEPORT_DISTANCE_SQ = 12 * 12;
	@Unique
	private static final int FOLLOW_DISTANCE_SQ = 3 * 3;

	@Unique
	private boolean tameTime$tamed;
	@Unique
	private UUID tameTime$ownerUuid;

	protected RabbitTamingMixin(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
	private void tameTime$preventDandelionBreeding(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.is(Items.DANDELION)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void tameTime$followOwner(CallbackInfo ci) {
		if (this.level().isClientSide() || !this.tameTime$tamed || this.tameTime$ownerUuid == null) {
			return;
		}

		Player owner = this.level().getPlayerInAnyDimension(this.tameTime$ownerUuid);
		if (owner == null || owner.isSpectator()) {
			return;
		}

		double distanceSq = this.distanceToSqr(owner);
		if (distanceSq >= TELEPORT_DISTANCE_SQ && !this.isPassenger() && !this.isLeashed()) {
			if (this.tameTime$teleportNear(owner)) {
				return;
			}
		}

		if (distanceSq >= FOLLOW_DISTANCE_SQ) {
			this.getNavigation().moveTo(owner, 1.2D);
		} else {
			this.getNavigation().stop();
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void tameTime$writeTameData(ValueOutput tag, CallbackInfo ci) {
		tag.putBoolean(TAME_TIME_TAMED_TAG, this.tameTime$tamed);
		tag.storeNullable(TAME_TIME_OWNER_TAG, UUIDUtil.CODEC, this.tameTime$ownerUuid);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void tameTime$readTameData(ValueInput tag, CallbackInfo ci) {
		this.tameTime$tamed = tag.getBooleanOr(TAME_TIME_TAMED_TAG, false);
		this.tameTime$ownerUuid = tag.read(TAME_TIME_OWNER_TAG, UUIDUtil.CODEC).orElse(null);
	}

	@Unique
	private boolean tameTime$teleportNear(Player owner) {
		BlockPos ownerPos = owner.blockPosition();
		for (int i = 0; i < 10; i++) {
			int x = ownerPos.getX() + this.random.nextInt(7) - 3;
			int y = ownerPos.getY() + this.random.nextInt(3) - 1;
			int z = ownerPos.getZ() + this.random.nextInt(7) - 3;

			if (Math.abs(x - ownerPos.getX()) <= 1 && Math.abs(z - ownerPos.getZ()) <= 1) {
				continue;
			}

			BlockPos targetPos = new BlockPos(x, y, z);
			BlockPos belowPos = targetPos.below();
			if (!this.level().getBlockState(belowPos).isSolidRender()) {
				continue;
			}

			double targetX = x + 0.5D;
			double targetY = y;
			double targetZ = z + 0.5D;
			if (!this.level().noCollision(this, this.getBoundingBox().move(
				targetX - this.getX(),
				targetY - this.getY(),
				targetZ - this.getZ()
			))) {
				continue;
			}

			this.teleportTo(targetX, targetY, targetZ);
			this.getNavigation().stop();
			return true;
		}

		return false;
	}

	@Override
	public boolean tameTime$isTamed() {
		return this.tameTime$tamed;
	}

	@Override
	public void tameTime$setTamed(boolean value) {
		this.tameTime$tamed = value;
	}

	@Override
	public UUID tameTime$getOwnerUuid() {
		return this.tameTime$ownerUuid;
	}

	@Override
	public void tameTime$setOwnerUuid(UUID ownerUuid) {
		this.tameTime$ownerUuid = ownerUuid;
	}
}
