
apply { plugin("kotlin") }

jvmTarget = "1.6"

dependencies {
    compile(project(":core:util.runtime"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:frontend.script"))
    if (System.getProperty("build.for.clion") != "true") {
        compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    } else {
        //todo[Alefas]: find substitution
    }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

