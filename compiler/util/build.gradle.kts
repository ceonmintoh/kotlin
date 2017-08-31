
apply { plugin("kotlin") }

jvmTarget = "1.6"

dependencies {
    compile(project(":core"))
    if (System.getProperty("build.for.clion") != "true") {
        compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    } else {
        compile(clionSdkDeps("openapi", "util", "annotations", "log4j", "trove4j", "guava"))
        //todo[Alefas]: find substitution
    }
}

sourceSets {
    "main" {
        projectDefault()
        resources.srcDir(File(rootDir, "resources")).apply { include("**") }
    }
    "test" {}
}

