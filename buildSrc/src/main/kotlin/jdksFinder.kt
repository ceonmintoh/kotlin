@file:Suppress("unused") // usages in build scripts are not tracked properly

import net.rubygrapefruit.platform.WindowsRegistry
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import net.rubygrapefruit.platform.WindowsRegistry.Key.HKEY_LOCAL_MACHINE
import org.gradle.internal.nativeintegration.services.NativeServices
import org.gradle.internal.os.OperatingSystem

enum class JdkMajorVersion {
    JDK_16, JDK_17, JDK_18, JDK_9
}

data class JdkId(val explicit: Boolean, val majorVersion: JdkMajorVersion, var version: String, var homeDir: File)

fun Project.getConfiguredJdks(): List<JdkId> {
    val res = arrayListOf<JdkId>()
    for (jdkMajorVersion in JdkMajorVersion.values()) {
        val explicitJdk = System.getenv(jdkMajorVersion.name)?.let { File(it)} ?: continue
        if (!explicitJdk.isDirectory) {
            throw GradleException("Invalid environment value $jdkMajorVersion: $explicitJdk, expecting JDK home path")
        }
        res.add(JdkId(true, jdkMajorVersion, "X", explicitJdk))
    }
    if (res.size < JdkMajorVersion.values().size) {
        res.discoverJdks(this)
    }
    return res
}

// see JEP 223
private val javaMajorVersionRegex = Regex("""(?:1\.)?(\d+).*""")
private val javaVersionRegex = Regex("""(?:1\.)?(\d+)(?:\.\d+)?(?:[+-_]\w+){0,3}""")

fun MutableCollection<JdkId>.addIfBetter(project: Project, version: String, id: String, homeDir: File): Boolean {
    val matchString = javaMajorVersionRegex.matchEntire(version)?.groupValues?.get(1)
    val majorJersion = when (matchString) {
        "6" -> JdkMajorVersion.JDK_16
        "7" -> JdkMajorVersion.JDK_17
        "8" -> JdkMajorVersion.JDK_18
        "9" -> JdkMajorVersion.JDK_9
        else -> {
            project.logger.info("Cannot recognize version string '$version' (found version '$matchString')")
            return false
        }
    }
    val prev = find { it.majorVersion == majorJersion }
    if (prev == null) {
        add(JdkId(false, majorJersion, version, homeDir))
        return true
    }
    if (prev.explicit) return false
    if (prev.version < version || (prev.version == version && id.contains("64"))) { // prefer 64-bit
        prev.version = version
        prev.homeDir = homeDir
        return true
    }
    return false
}

fun MutableCollection<JdkId>.discoverJdks(project: Project) {
    val os = OperatingSystem.current()
    when {
        os.isWindows -> discoverJdksOnWindows(project)
        os.isMacOsX -> discoverJdksOnMacOS(project)
        else -> discoverJdksOnUnix(project)
    }
}

private val macOsJavaHomeOutRegexes = listOf(Regex("""\s+(\S+),\s+(\S+):\s+".*?"\s+(.+)"""),
                                             Regex("""\s+(\S+)\s+\((.*?)\):\s+(.+)"""))

fun MutableCollection<JdkId>.discoverJdksOnMacOS(project: Project) {
    val procBuilder = ProcessBuilder("/usr/libexec/java_home", "-V").redirectErrorStream(true)
    val process = procBuilder.start()
    val retCode = process.waitFor()
    if (retCode != 0) throw GradleException("Unable to run 'java_home', return code $retCode")
    process.inputStream.bufferedReader().forEachLine { line ->
        for (rex in macOsJavaHomeOutRegexes) {
            val matchResult = rex.matchEntire(line)
            if (matchResult != null) {
                addIfBetter(project, matchResult.groupValues[1], matchResult.groupValues[0], File(matchResult.groupValues[3]))
                break
            }
        }
    }
}

private val unixConventionalJdkLocations = listOf(
        "/usr/lib/jvm",       // *deb, Arch
        "/opt",               // *rpm, Gentoo, HP/UX
        "/usr/lib",           // Slackware 32
        "/usr/lib64",         // Slackware 64
        "/usr/local",         // OpenBSD, FreeBSD
        "/usr/pkg/java",      // NetBSD
        "/usr/jdk/instances") // Solaris

private val unixConventionalJdkDirRex = Regex("jdk|jre|java|zulu")

fun MutableCollection<JdkId>.discoverJdksOnUnix(project: Project) {
    for (loc in unixConventionalJdkLocations) {
        val installedJdks = File(loc).listFiles { dir ->
            dir.isDirectory &&
            unixConventionalJdkDirRex.containsMatchIn(dir.name) &&
            File(dir, "bin", "java").isFile
        }
        for (dir in installedJdks) {
            val versionMatch = javaVersionRegex.find(dir.name)
            if (versionMatch == null) {
                project.logger.info("Unable to extract version from possible JDK dir: $dir")
            }
            else {
                addIfBetter(project, versionMatch.value, dir.name, dir)
            }
        }
    }
}

private val windowsConventionalJdkRegistryPaths = listOf(
        "SOFTWARE\\JavaSoft\\Java Development Kit",
        "SOFTWARE\\JavaSoft\\Java Runtime Environment",
        "SOFTWARE\\Wow6432Node\\JavaSoft\\Java Development Kit",
        "SOFTWARE\\Wow6432Node\\JavaSoft\\Java Runtime Environment")

fun MutableCollection<JdkId>.discoverJdksOnWindows(project: Project) {
    val nativeServices = NativeServices.getInstance()
    val registry = nativeServices.get(WindowsRegistry::class.java)
    for (regPath in windowsConventionalJdkRegistryPaths) {
        val jdkKeys = try {
            registry.getSubkeys(HKEY_LOCAL_MACHINE, regPath)
        } catch (e: RuntimeException) {
            // ignore missing nodes
            continue
        }
        for (jdkKey in jdkKeys) {
            try {
                val javaHome = registry.getStringValue(HKEY_LOCAL_MACHINE, regPath + "\\" + jdkKey, "JavaHome")
                val versionMatch = javaVersionRegex.find(jdkKey)
                if (versionMatch == null) {
                    project.logger.info("Unable to extract version from possible JDK location: $javaHome ($jdkKey)")
                }
                else {
                    javaHome.takeIf { it.isNotEmpty() }
                            ?.let { File(it) }
                            ?.takeIf { it.isDirectory && File(it, "bin", "java.exe").isFile }
                            ?.let {
                                addIfBetter(project, versionMatch.value, jdkKey, it)
                            }
                }
            }
            catch (e: RuntimeException) {
                // Ignore
            }
        }
    }
}
