
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend.java"))
    compile(project(":js:js.frontend"))
    compile(ideaSdkCoreDeps("annotations", "guava", "intellij-core"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

