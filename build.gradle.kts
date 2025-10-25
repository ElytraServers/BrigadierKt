import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
	kotlin("jvm") version "2.1.20"
	`maven-publish`
}

group = "cn.taskeren"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven {
		name = "Mojang Maven"
		url = uri("https://libraries.minecraft.net")
	}
}

dependencies {
	api("com.mojang:brigadier:1.0.18")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
	implementation("org.slf4j:slf4j-api:2.0.17")

	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(8)

	explicitApi = ExplicitApiMode.Warning
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group.toString()
			artifactId = project.name
			version = project.version.toString()

			pom {
				name = project.name
				description = project.description
				url = "https://github.com/ElytraServers/BrigadierKt"
				licenses {
					license {
						name = "MIT License"
						url = "https://opensource.org/license/mit"
					}
				}
				developers {
					developer {
						id = "taskeren"
						name = "Taskeren"
						email = "r0yalist^outlook.com"
					}
				}
			}

			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "lwgmr"
			url = uri("https://lwgmr.elytra.cn/")
			credentials {
				username = "Taskeren"
				password = project.findProperty("lwgmr.password") as? String
			}
		}
	}
}
