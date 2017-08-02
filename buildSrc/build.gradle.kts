buildscript {

    extra["repos"] = listOf(
            "https://dl.bintray.com/kotlin/kotlin-eap",
            "https://dl.bintray.com/kotlin/kotlin-dev",
            "https://repo.gradle.org/gradle/repo",
            "https://plugins.gradle.org/m2")

    project.extra["kotlinVersion"] = file("../kotlin-bootstrap-version.txt").readText().trim()

    configure(listOf(repositories, project.repositories)) {
        gradleKotlinDsl()
    }

    repositories {
        for (repo in (rootProject.extra["repos"] as List<String>)) {
            maven { setUrl(repo) }
        }
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = project.extra["kotlinVersion"] as String))
    }
}

apply {
    plugin("kotlin")
}

repositories {
    for (repo in (rootProject.extra["repos"] as List<String>)) {
        maven { setUrl(repo) }
    }
}

dependencies {
    val compile by configurations
    compile(kotlin("stdlib", version = project.extra["kotlinVersion"] as String))
    compile(gradleApi())
    compile(gradleKotlinDsl())
}
