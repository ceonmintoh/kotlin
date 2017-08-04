@file:Suppress("unused") // usages in build scripts are not tracked properly

import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.AbstractTask
import org.gradle.jvm.tasks.Jar
import java.io.File

inline fun <reified T : Task> Project.task(noinline configuration: T.() -> Unit) = tasks.creating(T::class, configuration)


fun AbstractTask.dependsOnTaskIfExists(task: String) {
    project.tasks.firstOrNull { it.name == task }?.let { dependsOn(it) }
}

fun AbstractTask.dependsOnTaskIfExistsRec(task: String, project: Project? = null) {
    dependsOnTaskIfExists(task)
    (project ?: this.project).subprojects.forEach {
        dependsOnTaskIfExistsRec(task, it)
    }
}

fun Project.dist(body: Copy.() -> Unit) {
    task<Copy>("dist") {
        tasks.findByName("assemble")?.let {
            dependsOn(it)
        }
        body()
        rename("-${java.util.regex.Pattern.quote(rootProject.extra["build.number"].toString())}", "")
        into(rootProject.extra["distLibDir"].toString())
    }
}


fun Project.ideaPlugin(subdir: String = "lib", body: Copy.() -> Unit) {
    task<Copy>("idea-plugin") {
        dependsOnTaskIfExists("assemble")
        body()
        into(File(rootProject.extra["ideaPluginDir"].toString(), subdir).path)
    }
}

fun Project.testsJar(body: Jar.() -> Unit): Jar {
    val testsJarCfg = configurations.create("tests-jar").extendsFrom(configurations["testCompile"])

    val testsJar by task<Jar> {
        dependsOn("testClasses")
        pluginManager.withPlugin("java") {
            from(project.the<JavaPluginConvention>().sourceSets.getByName("test").output)
        }
        classifier = "tests"
        body()
    }

    artifacts.add(testsJarCfg.name, testsJar)

    return testsJar
}

fun Jar.setupRuntimeJar(implementationTitle: String): Unit {
    dependsOn(":prepare:build.version:prepare")
    manifest.attributes.apply {
        put("Built-By", project.rootProject.extra["manifest.impl.vendor"])
        put("Implementation-Vendor", project.rootProject.extra["manifest.impl.vendor"])
        put("Implementation-Title", implementationTitle)
        put("Implementation-Version", project.rootProject.extra["build.number"])
    }
//    from(project.configurations.getByName("build-version").files, action = { into("META-INF/") })
}

fun Jar.setupSourceJar(implementationTitle: String): Unit {
    dependsOn("classes")
    setupRuntimeJar(implementationTitle + " Sources")
    project.pluginManager.withPlugin("java") {
        from(project.the<JavaPluginConvention>().sourceSets["main"].allSource)
    }
    classifier = "sources"
    project.artifacts.add("archives", this)
}

fun Project.buildVersion(): Dependency {
    val cfg = configurations.create("build-version")
    return dependencies.add(cfg.name, dependencies.project(":prepare:build.version", configuration = "default"))
}

fun Project.commonDep(coord: String): String {
    val parts = coord.split(':')
    return when (parts.size) {
        1 -> "$coord:$coord:${rootProject.extra["versions.$coord"]}"
        2 -> "${parts[0]}:${parts[1]}:${rootProject.extra["versions.${parts[1]}"]}"
        3 -> coord
        else -> throw IllegalArgumentException("Illegal maven coordinates: $coord")
    }
}

fun Project.commonDep(group: String, artifact: String): String = "$group:$artifact:${rootProject.extra["versions.$artifact"]}"

fun Project.preloadedDeps(vararg artifactBaseNames: String, baseDir: File = File(rootDir, "dependencies"), subdir: String? = null): ConfigurableFileCollection {
    val dir = if (subdir != null) File(baseDir, subdir) else baseDir
    if (!dir.exists() || !dir.isDirectory) throw GradleException("Invalid base directory $dir")
    val matchingFiles = dir.listFiles { file -> artifactBaseNames.any { file.matchMaybeVersionedArtifact(it) } }
    if (matchingFiles == null || matchingFiles.size < artifactBaseNames.size)
        throw GradleException("Not all matching artifacts '${artifactBaseNames.joinToString()}' found in the '$dir' (found: ${matchingFiles?.joinToString { it.name }})")
    return files(*matchingFiles.map { it.canonicalPath }.toTypedArray())
}

fun Project.ideaSdkDeps(vararg artifactBaseNames: String, subdir: String = "lib"): ConfigurableFileCollection =
        preloadedDeps(*artifactBaseNames, baseDir = File(rootDir, "ideaSDK"), subdir = subdir)

fun Project.ideaSdkCoreDeps(vararg artifactBaseNames: String): ConfigurableFileCollection = ideaSdkDeps(*artifactBaseNames, subdir = "core")

fun Project.ideaPluginDeps(vararg artifactBaseNames: String, plugin: String, subdir: String = "lib"): ConfigurableFileCollection =
        preloadedDeps(*artifactBaseNames, baseDir = File(rootDir, "ideaSDK"), subdir = "plugins/$plugin/$subdir")

fun Project.kotlinDep(artifactBaseName: String): String = "org.jetbrains.kotlin:kotlin-$artifactBaseName:${rootProject.extra["kotlinVersion"]}"

fun DependencyHandler.projectDep(name: String): Dependency = project(name, configuration = "default")
fun DependencyHandler.projectDepIntransitive(name: String): Dependency =
        project(name, configuration = "default").apply { isTransitive = false }

fun DependencyHandler.projectTests(name: String): Dependency = project(name, configuration = "tests-jar").apply { isTransitive = false }

val protobufLiteProject = ":custom-dependencies:protobuf-lite"
fun DependencyHandler.protobufLite(): ProjectDependency =
        project(protobufLiteProject, configuration = "default").apply { isTransitive = false }
val protobufLiteTask = "$protobufLiteProject:prepare"

fun DependencyHandler.protobufFull(): ProjectDependency =
        project(protobufLiteProject, configuration = "relocated").apply { isTransitive = false }
val protobufFullTask = "$protobufLiteProject:prepare-relocated-protobuf"

inline fun<T: Any> Project.withJavaPlugin(crossinline body: () -> T?): T? {
    var res: T? = null
    pluginManager.withPlugin("java") {
        res = body()
    }
    return res
}

fun Project.getCompiledClasses(): SourceSetOutput? = withJavaPlugin {
    the<JavaPluginConvention>().sourceSets.getByName("main").output
}

fun Project.getSources(): SourceDirectorySet? = withJavaPlugin {
    the<JavaPluginConvention>().sourceSets.getByName("main").allSource
}

fun Project.getResourceFiles(): SourceDirectorySet? = withJavaPlugin {
    the<JavaPluginConvention>().sourceSets.getByName("main").resources
}


private fun Project.configureKotlinProjectSourceSet(srcs: Iterable<File>,
                                                    sourceSetName: String,
                                                    getSources: SourceSet.() -> SourceDirectorySet,
                                                    configureSourceDirs: SourceDirectorySet.() -> Unit) =
        configure<JavaPluginConvention> {
//            if (srcs.none()) {
//                sourceSets.removeIf { it.name == sourceSetName }
//            }
//            else {
                sourceSets.matching { it.name == sourceSetName }.forEach { it.getSources().setSrcDirs(srcs).configureSourceDirs() }
//            }
        }

private fun Project.configureKotlinProjectSourceSet(vararg srcs: String, sourceSetName: String,
                                                    getSources: SourceSet.() -> SourceDirectorySet,
                                                    sourcesBaseDir: File? = null,
                                                    configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(srcs.map { File(sourcesBaseDir ?: projectDir, it) }, sourceSetName, getSources, configureSourceDirs)

fun Project.configureKotlinProjectSources(vararg srcs: String,
                                          sourcesBaseDir: File? = null,
                                          configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(*srcs, sourceSetName = "main", getSources = { this.java },
                                        sourcesBaseDir = sourcesBaseDir, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectSources(srcs: Iterable<File>,
                                          configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(srcs, sourceSetName = "main", getSources = { this.java }, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectSourcesDefault(sourcesBaseDir: File? = null,
                                                 configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSources("src", sourcesBaseDir = sourcesBaseDir, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectResources(vararg srcs: String,
                                            sourcesBaseDir: File? = null,
                                            configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(*srcs, sourceSetName = "main", getSources = { this.resources },
                                        sourcesBaseDir = sourcesBaseDir, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectResources(srcs: Iterable<File>,
                                            configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(srcs, sourceSetName = "main", getSources = { this.resources }, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectResourcesDefault(sourcesBaseDir: File? = null) {
    configureKotlinProjectResources("resources", sourcesBaseDir = sourcesBaseDir)
    configureKotlinProjectResources("src", sourcesBaseDir = sourcesBaseDir) { include("META-INF/**", "**/*.properties") }
}

fun Project.configureKotlinProjectNoTests() {
    configureKotlinProjectSourceSet(sourceSetName = "test", getSources = { this.java })
    configureKotlinProjectSourceSet(sourceSetName = "test", getSources = { this.resources })
}

fun Project.configureKotlinProjectTests(vararg srcs: String, sourcesBaseDir: File? = null,
                                        configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(*srcs, sourceSetName = "test", getSources = { this.java },
                                        sourcesBaseDir = sourcesBaseDir, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectTestsDefault(sourcesBaseDir: File? = null,
                                               configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectTests("test", "tests", sourcesBaseDir = sourcesBaseDir, configureSourceDirs = configureSourceDirs)

fun Project.configureKotlinProjectTestResources(vararg srcs: String,
                                                sourcesBaseDir: File? = null,
                                                configureSourceDirs: SourceDirectorySet.() -> Unit = {}) =
        configureKotlinProjectSourceSet(*srcs, sourceSetName = "test", getSources = { this.resources },
                                        sourcesBaseDir = sourcesBaseDir, configureSourceDirs = configureSourceDirs)

private fun File.matchMaybeVersionedArtifact(baseName: String) = name.matches(baseName.toMaybeVersionedJarRegex())

private val wildcardsRe = """[^*?]+|(\*)|(\?)""".toRegex()

private fun String.wildcardsToEscapedRegexString(): String = buildString {
    wildcardsRe.findAll(this@wildcardsToEscapedRegexString).forEach {
        when {
            it.groups[1] != null -> append(".*")
            it.groups[2] != null -> append(".")
            else -> append("\\Q${it.groups[0]!!.value}\\E")
        }
    }
}

private fun String.toMaybeVersionedJarRegex(): Regex {
    val hasJarExtension = endsWith(".jar")
    val escaped = this.wildcardsToEscapedRegexString()
    return Regex(if (hasJarExtension) escaped else "$escaped(-\\d.*)?\\.jar") // TODO: consider more precise version part of the regex
}

fun File(root: File, vararg children: String): File = children.fold(root, { f, c -> File(f, c) })
fun File(root: String, vararg children: String): File = children.fold(File(root), { f, c -> File(f, c) })
