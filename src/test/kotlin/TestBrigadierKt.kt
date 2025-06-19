import com.github.taskeren.brigadier_kt.*
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TestBrigadierKt {

	object CommandSource {
		var vip: Boolean = true
	}

	@Test
	fun `Test Simple DSLs`() {
		val dispatcher = CommandDispatcher<CommandSource>()

		var valueHolder = ""

		dispatcher.registerCommand("test") {
			literal("test") {
				executesUnit {
					valueHolder = "test"
				}
			}

			argument("hello", StringArgumentType.string()) {

				executesUnit { ctx ->
					val hello: String by ctx
					valueHolder = hello
				}

				requires { it.vip }
			}
		}

		dispatcher.execute("test test", CommandSource)
		assertEquals("test", valueHolder)

		dispatcher.execute("test \"world\"", CommandSource)
		assertEquals("world", valueHolder)

		CommandSource.vip = false
		assertThrows<CommandSyntaxException> {
			dispatcher.execute("test \"world\"", CommandSource)
		}

	}

	@Test
	fun `Test via`() {
		val dispatcher = CommandDispatcher<CommandSource>()

		var valueHolder = ""

		dispatcher.registerCommand("test") {
			argument("value", StringArgumentType.string()) {
				executesUnit { ctx ->
					val value by ctx.via(StringArgumentType::getString)
					valueHolder = value
				}
			}
		}

		dispatcher.execute("test \"hello\"", CommandSource)
		assertEquals("hello", valueHolder)
	}

}
