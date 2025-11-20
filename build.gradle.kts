import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.jpa") version "2.2.20" apply false
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}

allprojects {
    group = "com.groom"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenLocal() // Contract Stub을 로컬에서 가져오기 위함
        mavenCentral()
        maven {
            name = "스키마 레지스트리"
            url = uri("https://packages.confluent.io/maven/")
        }
        maven {
            name = "c4ang-platform-core github packages"
            url = uri("https://maven.pkg.github.com/GroomC4/c4ang-platform-core")
            credentials {
                username = (System.getenv("GITHUB_ACTOR") ?: findProperty("gpr.user"))?.toString()
                password = (System.getenv("GITHUB_TOKEN") ?: findProperty("gpr.token"))?.toString()
            }
        }
        maven {
            name = "c4ang-customer-service contract stub github packages"
            url = uri("https://maven.pkg.github.com/GroomC4/c4ang-customer-service")
            credentials {
                username = (System.getenv("GITHUB_ACTOR") ?: findProperty("gpr.user"))?.toString()
                password = (System.getenv("GITHUB_TOKEN") ?: findProperty("gpr.token"))?.toString()
            }
        }
        maven {
            name = "c4ang-contract-hub jitpack"
            url = uri("https://jitpack.io")
        }
    }

    extensions.findByType<KotlinJvmProjectExtension>()?.apply {
        jvmToolchain(21)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.7.1")
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(true)
    }
}

tasks.register("bootRun") {
    dependsOn(":store-api:bootRun")
}
