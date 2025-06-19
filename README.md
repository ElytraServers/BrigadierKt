# BrigadierKt

BrigadierKt is a DSL library for Mojang [Brigadier](https://github.com/Mojang/brigadier), providing simplified functions
for registering commands, handling commands, and reading arguments from contexts.

## Include

[![](https://jitpack.io/v/ElytraServers/BrigadierKt.svg)](https://jitpack.io/#ElytraServers/BrigadierKt)

BrigadierKt is distributed by JitPack.

```gradle
repositories {
    // for Brigadier
    maven { url = "https://libraries.minecraft.net" }
    // for BrigadierKt
    maven { url = "https://jitpack.io" }
}

dependencies {
    // don't forget to replace the VERSION with the valid version shown above in the label
    implementation("com.github.ElytraServers:BrigadierKt:VERSION")
}
```

## Usage

```kotlin
val dispatcher: CommandDispatcher<*> = TODO()

dispatcher.registerCommand("test") {
    // ... add more subcommands or executor here

    executesUnit {
        // executesUnit is an extension of executes, where it always return SINGLE_SUCCESS
        println("Hello World!")
    }
    // equivalent to
    // executes {
    //     println("Hello World!")
    //     Command.SINGLE_SUCCESS
    // }

    // add a literal subcommand
    literal("foo") {
        executesUnit {
            println("foo")
        }
    }

    // add an argument subcommand
    argument("stringValue", StringArgumentType.string()) {
        // the first argument is the name of the argument, where you'll get its value from it.
        // the second argument is the type of the argument.

        executesUnit { ctx ->
            val stringValue: String by ctx
            // you can get the values directly from ctx like this for support types
            // (String, Int, Long, Float, Double, Boolean),
            // where the variable name is equal to the argument name.

            // you can register the getter for a custom type by [BrigadierKt.registerCommandContextValueProvider]

            // equivalent to
            // val str = StringArgumentType.getString(ctx, "stringValue")

            println(stringValue)
            
            // if the type is not supported, you can get it by [via],
            // where it accepts a function with 2 parameters,
            // the 1st parameter is [CommandContext], and the 2nd is the name of the argument (the variable name)
            val stringValue by ctx.via(StringArgumentType::getString)
        }
    }
    
    // you can also see the test codes.
}
```
