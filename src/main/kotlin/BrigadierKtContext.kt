package com.github.taskeren.brigadier_kt

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext

/**
 * Register a [RequiredArgumentBuilder] subcommand with given [name] and [argumentType].
 */
public inline fun <S, BuilderT : ArgumentBuilder<S, BuilderT>, reified ArgumentT> ArgumentBuilder<S, BuilderT>.argumentContext(
	name: String,
	argumentType: ArgumentType<ArgumentT>,
	block: RequiredArgumentBuilder<S, ArgumentT>.(value: context(CommandContext<*>) () -> ArgumentT) -> Unit,
): BuilderT =
	then(
		argument<S, ArgumentT>(name, argumentType).apply<RequiredArgumentBuilder<S, ArgumentT>> {
			block { contextOf<CommandContext<*>>().getArgument(name) }
		},
	)

/**
 * Make a command execution block.
 */
public inline fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesKt(
	crossinline block: context(ExecuteScope) (CommandContext<S>) -> Unit,
): T =
	executes {
		ExecuteScope.runScoped {
			block(it)
		}
	}

/**
 * Make a command execution block.
 */
public inline fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesContext(
	crossinline block: context(CommandContext<S>, ExecuteScope) (CommandContext<S>) -> Int,
): T =
	executes {
		ExecuteScope.runScoped {
			context(it) { block(it) }
		}
	}
