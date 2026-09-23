package com.github.taskeren.brigadier_kt

import com.mojang.brigadier.Command
import java.util.concurrent.CancellationException

public class ExecuteScope private constructor() {
	public companion object {
		@PublishedApi
		internal val instance: ExecuteScope = ExecuteScope()

		/**
		 * Run the [block] with [ExecuteScope].
		 */
		@PublishedApi
		internal inline fun runScoped(
			defaultValue: Int = Command.SINGLE_SUCCESS,
			block: context(ExecuteScope) () -> Unit,
		): Int =
			try {
				context(instance) { block() }
				defaultValue
			} catch (e: ExecuteRaiseException) {
				e.result
			}
	}
}

/**
 * Used in [result] to stop executing further.
 * @see ExecuteScope.runScoped
 */
@PublishedApi
internal class ExecuteRaiseException internal constructor(
	val result: Int,
) : CancellationException()

/**
 * Terminate the executes block with the given result.
 *
 * ```kt
 * val result =
 *     ExecuteScope.runScoped {
 *         // do some calculation
 *         if (something == null) result(-1)
 *         if (mode == 1) result(42)
 *         // implicitly result in 1 by default.
 *     }
 * ```
 */
context(_: ExecuteScope)
public fun result(result: Int): Nothing = throw ExecuteRaiseException(result)
