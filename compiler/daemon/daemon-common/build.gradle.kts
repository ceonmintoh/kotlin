
apply { plugin("kotlin") }

jvmTarget = "1.6"

dependencies {
    compile(project(":core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:cli-common"))
    if (System.getProperty("build.for.clion") != "true") {
        compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    } else {
        //todo[Alefas]: find substitution?
    }
    compile(project(":kotlin-stdlib"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

