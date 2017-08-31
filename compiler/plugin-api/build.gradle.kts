
apply { plugin("kotlin") }

jvmTarget = "1.6"

dependencies {
    if (System.getProperty("build.for.clion") != "true") {
        compile(ideaSdkCoreDeps("intellij-core"))
    } else {
        //todo[Alefas]: find substitution?
    }
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

