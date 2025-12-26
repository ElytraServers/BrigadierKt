import groovy.lang.Closure

plugins {
	kotlin("jvm") version "2.3.0"

	id("org.jetbrains.dokka") version "2.1.0"
	`maven-publish`
	signing
	id("com.vanniktech.maven.publish") version "0.35.0"

	id("com.palantir.git-version") version "4.2.0"
}

group = "cn.taskeren"

val gitVersion: Closure<String> by extra
try {
	version = gitVersion()
} catch (e: Exception) {
	println("Failed to get version from git")
	e.printStackTrace()
}

repositories {
	mavenCentral()
	maven {
		name = "Mojang Maven"
		url = uri("https://libraries.minecraft.net")
	}
}

dependencies {
	api("com.mojang:brigadier:1.0.500")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
	implementation("org.slf4j:slf4j-api:2.0.17")

	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(8)
	explicitApiWarning()
}

mavenPublishing {
	publishToMavenCentral(automaticRelease = true)
	signAllPublications()

	coordinates("cn.elytra", "brigadier-kt", "${project.version}".removePrefix("v"))
	pom {
		name = "Brigadier Kotlin Extension"
		description = "Add extension methods to Brigadier things."
		inceptionYear = "2025"
		url = "https://github.com/ElytraServers/BrigadierKt"
		licenses {
			license {
				name = "MIT License"
				url = "https://github.com/ElytraServers/BrigadierKt/blob/master/LICENSE"
			}
		}
		developers {
			developer {
				id = "taskeren"
				name = "Taskeren"
				url = "https://github.com/Taskeren"
			}
		}
		scm {
			url = "https://github.com/ElytraServers/BrigadierKt"
			connection = "scm:git:git://github.com/ElytraServers/BrigadierKt.git"
			developerConnection = "scm:git:ssh://github.com/ElytraServers/BrigadierKt.git"
		}
	}
}
