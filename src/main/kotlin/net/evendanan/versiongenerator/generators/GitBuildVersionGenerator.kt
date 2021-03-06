package net.evendanan.versiongenerator.generators

import net.evendanan.versiongenerator.GenerationData
import net.evendanan.versiongenerator.VersionGenerator
import java.io.IOException
import java.util.concurrent.TimeUnit

class GitBuildVersionGenerator(private val processRunner: ProcessOutput, private val buildNumberOffset: Int, private val patchNumberOffset: Int)
    : VersionGenerator("GitVersionBuilder") {

    constructor(buildNumberOffset: Int, patchNumberOffset: Int): this(defaultProcessRunner, buildNumberOffset, patchNumberOffset)
    constructor() : this(0, 0)

    override fun isValidForEnvironment(): Boolean {
        return getGitHistorySize() > 0
    }

    override fun getVersionCode(generationData: GenerationData): Int {
        val revCount = getGitHistorySize()
        val tagCount = processRunner.runCommandForOutput("git tag").split("\n").size

        return revCount + tagCount + buildNumberOffset
    }

    override fun getVersionName(generationData: GenerationData): String {
        val patchedGenerationData = GenerationData(generationData.major, generationData.minor, generationData.patchOffset + patchNumberOffset)
        return super.getVersionName(patchedGenerationData)
    }

    private fun getGitHistorySize(): Int {
        try {
            return Integer.parseInt(processRunner.runCommandForOutput("git rev-list --count HEAD --all"))
        } catch (e: Exception) {
            return -1
        }
    }

    private object defaultProcessRunner: ProcessOutput {
        override fun runCommandForOutput(command: String): String {
            try {
                val parts = command.split("\\s".toRegex())
                val proc = ProcessBuilder(*parts.toTypedArray())
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()

                proc.waitFor(60, TimeUnit.MINUTES)
                return proc.inputStream.bufferedReader().readText().trim()
            } catch(e: IOException) {
                println("runCommand IOException %s".format(e))
                e.printStackTrace()
                return ""
            }
        }

    }

    interface ProcessOutput {
        fun runCommandForOutput(command: String): String
    }
}