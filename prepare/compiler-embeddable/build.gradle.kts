
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.3")
    }
}

val archives by configurations.creating

val compilerJar by configurations.creating

val embeddableCompilerJar by configurations.creating

val embeddableCompilerBaseName: String by rootProject.extra

val kotlinEmbeddableRootPackage = "org.jetbrains.kotlin"

val packagesToRelocate =
        listOf("com.intellij",
               "com.google",
               "com.sampullara",
               "org.apache",
               "org.jdom",
               "org.picocontainer",
               "jline",
               "gnu",
               "javax.inject",
               "org.fusesource")

dependencies {
    compilerJar(project(":prepare:compiler", configuration = "compilerJar"))
}

val embeddable by task<ShadowJar> {
    destinationDir = File(buildDir, "libs")
    baseName = embeddableCompilerBaseName
    classifier = ""
    version = rootProject.extra["build.number"] as String
    configurations = listOf(embeddableCompilerJar)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(":prepare:compiler:proguard")
    from(compilerJar)
    relocate("com.google.protobuf", "org.jetbrains.kotlin.protobuf")
    packagesToRelocate.forEach {
        relocate(it, "$kotlinEmbeddableRootPackage.$it")
    }
    relocate("org.fusesource", "$kotlinEmbeddableRootPackage.org.fusesource") {
        // TODO: remove "it." after #KT-12848 get addressed
        exclude("org.fusesource.jansi.internal.CLibrary")
    }
}

defaultTasks(embeddable.name)

artifacts.add("archives", embeddable.outputs.files.singleFile) {
    builtBy(embeddable)
}

apply<plugins.PublishedKotlinModule>()

