
description = "Kotlin Preloader"

apply { plugin("kotlin") }

dependencies {
    val compile by configurations
    compile(ideaSdkDeps("asm-all"))
    buildVersion()
}

//configureKotlinProjectSources("src", "instrumentation/src")
sourceSets {
    "main" {
        java {
            srcDirs( "src", "instrumentation/src")
        }
    }
    "test" { none() }
}
//configureKotlinProjectNoTests()

val jar = runtimeJar {
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.preloading.Preloader")
}

dist {
    from(jar)
}

