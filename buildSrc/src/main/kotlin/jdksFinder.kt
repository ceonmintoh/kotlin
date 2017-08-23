/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.GradleException
import java.io.File
import java.util.*

private val macOsJavaHomeOutRegexes = listOf(Regex("""\s+(\S+),\s+(\S+):\s+".*?"\s+(.+)"""),
                                             Regex("""\s+(\S+)\s+\((.*?)\):\s+(.+)"""))

fun findJdksOnMacOS(): SortedMap<String, File> {
    val procBuilder = ProcessBuilder("/usr/libexec/java_home", "-V").redirectErrorStream(true)
    val process = procBuilder.start()
    val retCode = process.waitFor()
    if (retCode != 0) throw GradleException("Unable to run 'java_home', return code $retCode")
    val res = sortedMapOf<String, File>()
    process.inputStream.bufferedReader().forEachLine { line ->
        for (rex in macOsJavaHomeOutRegexes) {
            val matchResult = rex.matchEntire(line)
            if (matchResult != null) {
                res[matchResult.groupValues[1]] = File(matchResult.groupValues[3])
                break
            }
        }
    }
    return res
}
