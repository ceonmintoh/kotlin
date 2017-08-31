
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:frontend"))
    if (System.getProperty("build.for.clion") != "true") {
        compile(ideaSdkDeps("openapi"))
    } else {
        compile(clionSdkDeps("openapi"))
        compile(preloadedDeps("java-api", "java-impl"))
    }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

