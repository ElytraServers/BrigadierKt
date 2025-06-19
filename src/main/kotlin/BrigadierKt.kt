@file:Suppress("unused")

package com.github.taskeren.brigadier_kt

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.*
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
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public fun <S> newLiteralArgumentBuilder(
	name: String,
	block: LiteralArgumentBuilder<S>.() -> Unit,
): LiteralArgumentBuilder<S> =
	LiteralArgumentBuilder.literal<S>(name).apply(block)

public fun <S, T> newRequiredArgumentBuilder(
	name: String,
	argumentType: ArgumentType<T>,
	block: RequiredArgumentBuilder<S, T>.() -> Unit,
): RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument<S, T>(name, argumentType).apply(block)

/**
 * Start to build a command with the given [name] and register it to the given dispatcher.
 */
public fun <S> CommandDispatcher<S>.registerCommand(
	name: String,
	block: LiteralArgumentBuilder<S>.() -> Unit,
): LiteralCommandNode<S> = this.register(newLiteralArgumentBuilder(name, block))

/**
 * Register a [LiteralArgumentBuilder] subcommand with given [name].
 */
public fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.literal(
	name: String,
	block: LiteralArgumentBuilder<S>.() -> Unit,
): T = this.then(newLiteralArgumentBuilder(name, block))

/**
 * Register a [RequiredArgumentBuilder] subcommand with given [name] and [argumentType].
 */
public fun <S, BuilderT : ArgumentBuilder<S, BuilderT>, ArgumentT> ArgumentBuilder<S, BuilderT>.argument(
	name: String,
	argumentType: ArgumentType<ArgumentT>,
	block: RequiredArgumentBuilder<S, ArgumentT>.() -> Unit,
): BuilderT = this.then(newRequiredArgumentBuilder(name, argumentType, block))

/**
 * Make a command execution block that always returns [Command.SINGLE_SUCCESS].
 */
public fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesUnit(block: (CommandContext<S>) -> Unit): T =
	this.executes { block(it); Command.SINGLE_SUCCESS }

/**
 * Provide suggestions with a suspend function.
 */
public fun <S, T> RequiredArgumentBuilder<S, T>.suggestsSuspended(
	block: suspend (CommandContext<S>, SuggestionsBuilder) -> Suggestions,
): RequiredArgumentBuilder<S, T> = suggests { ctx, builder ->
	BrigadierKt.SuggestionProviderScope.future { block(ctx, builder) }
}

/**
 * Provide suggestions with a blocking function.
 */
public fun <S, T> RequiredArgumentBuilder<S, T>.suggestsBlocking(
	block: (CommandContext<S>, SuggestionsBuilder) -> Unit,
): RequiredArgumentBuilder<S, T> = suggests { ctx, builder ->
	block(ctx, builder)
	builder.buildFuture()
}

/**
 * A typealias for the functions that accepts the [CommandContext] and the argument name to retrieve the value of the argument.
 *
 * For example: [StringArgumentType.getString], [IntegerArgumentType.getInteger], etc.
 */
internal typealias ContextValueGetter<S, T> = (CommandContext<S>, String) -> T

/**
 * The argument value getter.
 *
 * `val value: TYPE by context`.
 *
 * - value: The name of the argument, case-sensitive.
 * - TYPE: The type of the argument. Note that the custom [ArgumentTypes][ArgumentType] are not supported by default, you must register its getter by [BrigadierKt.registerCommandContextValueProvider].
 * - context: The [CommandContext] of the command execution.
 */
public inline operator fun <S, reified T> CommandContext<S>.getValue(thisRef: Any?, prop: KProperty<*>): T {
	return when(typeOf<T>()) {
		typeOf<String>() -> StringArgumentType.getString(this, prop.name) as T
		typeOf<Int>() -> IntegerArgumentType.getInteger(this, prop.name) as T
		typeOf<Long>() -> LongArgumentType.getLong(this, prop.name) as T
		typeOf<Float>() -> FloatArgumentType.getFloat(this, prop.name) as T
		typeOf<Double>() -> DoubleArgumentType.getDouble(this, prop.name) as T
		typeOf<Boolean>() -> BoolArgumentType.getBool(this, prop.name) as T
		else -> {
			// handle non-built-in types
			val getter = BrigadierKt.CommandContextValueGetters[typeOf<T>()]
			if(getter != null) {
				return getter(this, prop.name) as T
			} else {
				throw IllegalArgumentException("Unsupported type: ${typeOf<T>()}")
			}
		}
	}
}

/**
 * Get the values from [CommandContext] via [getter].
 */
public infix fun <S, T> CommandContext<S>.via(getter: ContextValueGetter<S, T>): ReadOnlyProperty<Any?, T> {
	return ReadOnlyProperty { _, prop -> getter(this@via, prop.name) }
}

public object BrigadierKt {

	internal val SuggestionProviderScope = CoroutineScope(SupervisorJob() + CoroutineName("SuggestionProviderScope"))

	@PublishedApi
	internal val CommandContextValueGetters: MutableMap<KType, ContextValueGetter<*, *>> = mutableMapOf()

	internal val logger = LoggerFactory.getLogger("BrigadierKt")

	/**
	 * Register a custom [ArgumentType] with its value getter to make it available in `by` syntax.
	 */
	public inline fun <reified T> registerCommandContextValueProvider(noinline getter: (CommandContext<*>, String) -> T) {
		val type = typeOf<T>()
		CommandContextValueGetters[type] = getter
	}

}
