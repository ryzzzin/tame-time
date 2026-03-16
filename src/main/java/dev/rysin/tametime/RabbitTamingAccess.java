package dev.rysin.tametime;

import java.util.UUID;

public interface RabbitTamingAccess {
	boolean tameTime$isTamed();

	void tameTime$setTamed(boolean value);

	UUID tameTime$getOwnerUuid();

	void tameTime$setOwnerUuid(UUID ownerUuid);
}
