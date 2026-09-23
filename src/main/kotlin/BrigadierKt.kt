@file:Suppress("unused")

package com.github.taskeren.brigadier_kt

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

public inline fun <S> newLiteralArgumentBuilder(
	name: String,
	block: LiteralArgumentBuilder<S>.() -> Unit,
): LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal<S>(name).apply(block)

public inline fun <S, T> newRequiredArgumentBuilder(
	name: String,
	argumentType: ArgumentType<T>,
	block: RequiredArgumentBuilder<S, T>.() -> Unit,
): RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument<S, T>(name, argumentType).apply(block)

/**
 * Start to build a command with the given [name] and register it to the given dispatcher.
 */
public inline fun <S> CommandDispatcher<S>.registerCommand(
	name: String,
	block: LiteralArgumentBuilder<S>.() -> Unit,
): LiteralCommandNode<S> = this.register(newLiteralArgumentBuilder(name, block))

/**
 * Register a [LiteralArgumentBuilder] subcommand with given [name].
 */
public inline fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.literal(
	name: String,
	block: LiteralArgumentBuilder<S>.() -> Unit,
): T = this.then(newLiteralArgumentBuilder(name, block))

/**
 * Register a [RequiredArgumentBuilder] subcommand with given [name] and [argumentType].
 */
public inline fun <S, BuilderT : ArgumentBuilder<S, BuilderT>, ArgumentT> ArgumentBuilder<S, BuilderT>.argument(
	name: String,
	argumentType: ArgumentType<ArgumentT>,
	block: RequiredArgumentBuilder<S, ArgumentT>.() -> Unit,
): BuilderT = this.then(newRequiredArgumentBuilder(name, argumentType, block))

/**
 * Make a command execution block that always returns [Command.SINGLE_SUCCESS].
 */
@Deprecated("Deprecated function.", ReplaceWith("this.executesKt(block)"))
public inline fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesUnit(crossinline block: (CommandContext<S>) -> Unit): T =
	executesKt { block(it) }

/**
 * Provide suggestions with a suspend function.
 */
public inline fun <S, T> RequiredArgumentBuilder<S, T>.suggestsSuspended(
	crossinline block: suspend (CommandContext<S>, SuggestionsBuilder) -> Suggestions,
): RequiredArgumentBuilder<S, T> =
	suggests { ctx, builder ->
		BrigadierKt.SuggestionProviderScope.future { block(ctx, builder) }
	}

/**
 * Provide suggestions with a blocking function.
 */
public inline fun <S, T> RequiredArgumentBuilder<S, T>.suggestsBlocking(
	crossinline block: (CommandContext<S>, SuggestionsBuilder) -> Unit,
): RequiredArgumentBuilder<S, T> =
	suggests { ctx, builder ->
		block(ctx, builder)
		builder.buildFuture()
	}

/**
 * The argument value getter.
 *
 * `val value: TYPE by context`.
 *
 * - value: The name of the argument, case-sensitive.
 * - TYPE: The type of the argument.
 * - context: The [CommandContext] of the command execution.
 */
public inline operator fun <S, reified T> CommandContext<S>.getValue(
	thisRef: Any?,
	prop: KProperty<*>,
): T = getArgument(prop.name)

public object BrigadierKt {
	@PublishedApi
	internal val SuggestionProviderScope: CoroutineScope =
		CoroutineScope(SupervisorJob() + CoroutineName("SuggestionProviderScope"))

	internal val logger = LoggerFactory.getLogger("BrigadierKt")
}

/**
 * Get the stored argument value in the context.
 *
 * @throws IllegalArgumentException if the value is absent or type mismatches.
 */
@Throws(IllegalArgumentException::class)
public inline fun <S, reified T> CommandContext<S>.getArgument(name: String): T = getArgument(name, T::class.java)
