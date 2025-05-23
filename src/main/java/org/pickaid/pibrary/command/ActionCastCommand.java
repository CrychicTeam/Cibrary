package org.pickaid.pibrary.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.pickaid.pibrary.content.context.interaction.InteractionActionCastType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.Optional;

public class ActionCastCommand extends MagicCommandEventHandlers {

	private static final DynamicCommandExceptionType ERR_INVALID_NAME =
			new DynamicCommandExceptionType(str -> Component.translatable("commands.actioncast.invalid_action_name", str));


	private static <T> ResourceKey<T> getRegistryKey(
			CommandContext<CommandSourceStack> ctx, String name,
			ResourceKey<Registry<T>> reg, DynamicCommandExceptionType err
	) throws CommandSyntaxException {
		ResourceKey<?> ans = ctx.getArgument(name, ResourceKey.class);
		Optional<ResourceKey<T>> optional = ans.cast(reg);
		return optional.orElseThrow(() -> err.create(ans));
	}

	private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> ctx, ResourceKey<? extends Registry<T>> reg) {
		return ctx.getSource().getServer().registryAccess().registryOrThrow(reg);
	}

	private static <T> Holder.Reference<T> resolveKey(
			CommandContext<CommandSourceStack> ctx, String name,
			ResourceKey<Registry<T>> reg, DynamicCommandExceptionType err
	) throws CommandSyntaxException {
		ResourceKey<T> ans = getRegistryKey(ctx, name, reg, err);
		return getRegistry(ctx, reg).getHolder(ans).orElseThrow(() -> err.create(ans.location()));
	}

	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("cast").requires(e -> e.hasPermission(1))
				.then(argument("user", EntityArgument.entities())
						.then(Commands.argument("interaction", ResourceKeyArgument.key(LibraryRegistries.ACTION))
								.then(literal("instant")
										.executes(ctx -> run(ctx, InteractionActionCastType.INSTANT, false))
										.then(argument("power", DoubleArgumentType.doubleArg(0))
												.executes(ctx -> run(ctx, InteractionActionCastType.INSTANT, true))
										)
								).then(literal("charge")
										.then(argument("chargeTime", IntegerArgumentType.integer(0))
												.executes(ctx -> run(ctx, InteractionActionCastType.CHARGE, false))
												.then(argument("power", DoubleArgumentType.doubleArg(0))
														.executes(ctx -> run(ctx, InteractionActionCastType.CHARGE, true))
												)
										)
								).then(literal("continuous")
										.then(argument("duration", IntegerArgumentType.integer(0))
												.executes(ctx -> run(ctx, InteractionActionCastType.CONTINUOUS, false))
												.then(argument("power", DoubleArgumentType.doubleArg(0))
														.executes(ctx -> run(ctx, InteractionActionCastType.CONTINUOUS, true))
												)
										)
								)
						)
				);
	}

	public void test() {

	}

	private static int run(CommandContext<CommandSourceStack> ctx, InteractionActionCastType type, boolean usePower) throws CommandSyntaxException {
		var list = EntityArgument.getEntities(ctx, "user");
		var holder = resolveKey(ctx, "interaction", LibraryRegistries.ACTION, ERR_INVALID_NAME);
		var spell = holder.get();
		var id = holder.key().location();
		if (type != spell.castType()) {
			ctx.getSource().sendFailure(Component.translatable("command.pibrary.cast.type_mismatch", id, type, spell.castType()  ));
			return 1;
		}
		double power = !usePower ? 1 : DoubleArgumentType.getDouble(ctx, "power");
		int time = 0;
		if (type == InteractionActionCastType.CONTINUOUS) {
			time = IntegerArgumentType.getInteger(ctx, "duration");
		}
		if (type == InteractionActionCastType.CHARGE) {
			time = IntegerArgumentType.getInteger(ctx, "chargeTime");
		}
		int success = 0;
		for (var e : list) {
			if (e instanceof LivingEntity le) {
				if (CommandActionExecutor.execute(le, spell, time, power, 64)) {
					success++;
				}
			}
		}

		if (success == 0) {
			ctx.getSource().sendFailure(Component.translatable("commands.actioncast.invalid_spell_name"));
			return 1;
		}
		Component comp;
		if (list.size() > 1) {
			comp = Component.translatable("", id, success);
		} else {
			comp = Component.translatable("",id);
		}
		ctx.getSource().sendSuccess(() -> comp, false);
		return 0;
	}

}
