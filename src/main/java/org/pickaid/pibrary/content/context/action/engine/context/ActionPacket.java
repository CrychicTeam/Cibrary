package org.pickaid.pibrary.content.context.action.engine.context;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.pibrary.content.context.action.engine.helper.Orientation;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.function.Supplier;

public class ActionPacket {
	public int user;
	public long seed;
	public ResourceLocation action;
	public Vec3 origin, facing, normal;
	public double tickUsing, power;

	public ActionPacket(InteractionAction action, ActionContext ctx) {
		this.user = ctx.user().getId();
		this.origin = ctx.origin();
		this.facing = ctx.facing().forward();
		this.normal = ctx.facing().normal();
		this.tickUsing = ctx.tickUsing();
		this.power = ctx.power();
		this.seed = ctx.seed();
		this.action = ctx.user().level().registryAccess().registryOrThrow(LibraryRegistries.ACTION).getKey(action);
	}

	public ActionPacket(int user, long seed, ResourceLocation action, Vec3 origin, Vec3 facing, Vec3 normal, double tickUsing, double power) {
		this.user = user;
		this.seed = seed;
		this.action = action;
		this.origin = origin;
		this.facing = facing;
		this.normal = normal;
		this.tickUsing = tickUsing;
		this.power = power;
	}

	public static void encode(ActionPacket packet, FriendlyByteBuf buffer) {
		buffer.writeInt(packet.user);
		buffer.writeLong(packet.seed);
		buffer.writeResourceLocation(packet.action);

		buffer.writeDouble(packet.origin.x);
		buffer.writeDouble(packet.origin.y);
		buffer.writeDouble(packet.origin.z);

		buffer.writeDouble(packet.facing.x);
		buffer.writeDouble(packet.facing.y);
		buffer.writeDouble(packet.facing.z);

		buffer.writeDouble(packet.normal.x);
		buffer.writeDouble(packet.normal.y);
		buffer.writeDouble(packet.normal.z);

		buffer.writeDouble(packet.tickUsing);
		buffer.writeDouble(packet.power);
	}

	public static ActionPacket decode(FriendlyByteBuf buffer) {
		int user = buffer.readInt();
		long seed = buffer.readLong();
		ResourceLocation action = buffer.readResourceLocation();

		double originX = buffer.readDouble();
		double originY = buffer.readDouble();
		double originZ = buffer.readDouble();
		Vec3 origin = new Vec3(originX, originY, originZ);

		double facingX = buffer.readDouble();
		double facingY = buffer.readDouble();
		double facingZ = buffer.readDouble();
		Vec3 facing = new Vec3(facingX, facingY, facingZ);

		double normalX = buffer.readDouble();
		double normalY = buffer.readDouble();
		double normalZ = buffer.readDouble();
		Vec3 normal = new Vec3(normalX, normalY, normalZ);

		double tickUsing = buffer.readDouble();
		double power = buffer.readDouble();

		return new ActionPacket(user, seed, action, origin, facing, normal, tickUsing, power);
	}

	public static void handle(ActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ClientActionHandler.useAction(packet.user, packet.action, packet.origin,
					Orientation.of(packet.facing, packet.normal),
					packet.seed, packet.tickUsing, packet.power);
		});
		context.setPacketHandled(true);
	}
}