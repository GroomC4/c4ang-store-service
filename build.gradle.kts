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

    // ✅ repositories 설정은 settings.gradle.kts에서 중앙 관리
    // dependencyResolutionManagement를 통해 모든 프로젝트에 자동 적용됨

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
