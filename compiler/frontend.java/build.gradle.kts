
apply { plugin("kotlin") }

jvmTarget = "1.6"

dependencies {
    compile(project(":core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
    if (System.getProperty("build.for.clion") == "true") {
        compile(ideaSdkCoreDeps("asm-all"))
    }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

