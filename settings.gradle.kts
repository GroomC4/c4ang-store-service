rootProject.name = "c4ang-store-service"

include("store-api")
include("store-events")

// 모든 서브프로젝트에 공통 저장소 설정 적용
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()

        maven {
            name = "C4angMavenPackages"
            url = uri("https://maven.pkg.github.com/GroomC4/c4ang-maven-packages")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user"))
                    .getOrElse("")
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.token"))
                    .getOrElse("")
            }
        }

        maven {
            name = "ConfluentSchemaRegistry"
            url = uri("https://packages.confluent.io/maven/")
        }

        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
            // JitPack은 contract-hub가 GitHub Packages로 마이그레이션될 때까지 임시 사용
        }
    }
}
