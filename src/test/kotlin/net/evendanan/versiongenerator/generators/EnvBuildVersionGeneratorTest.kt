package net.evendanan.versiongenerator.generators

import net.evendanan.versiongenerator.GenerationData
import net.evendanan.versiongenerator.VersionGenerator
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables

class EnvBuildVersionGeneratorTest {

    @Rule @JvmField val environmentVariables = EnvironmentVariables()

    val generationData = GenerationData(1, 2, 23)
    val reportedBuildNumber = 500

    private fun verifyHappyPaths(envKey: String, generator: VersionGenerator, expectedVersionCode: Int, patchOffset: Int) {
        environmentVariables.set(envKey, null)
        Assert.assertNull(System.getenv(envKey))
        Assert.assertFalse(generator.isValidForEnvironment())

        environmentVariables.set(envKey, "")
        Assert.assertEquals("", System.getenv(envKey))
        Assert.assertFalse(generator.isValidForEnvironment())

        environmentVariables.set(envKey, reportedBuildNumber.toString())
        Assert.assertEquals(reportedBuildNumber.toString(), System.getenv(envKey))
        Assert.assertTrue(generator.isValidForEnvironment())

        val versionData = generator.generate(generationData)
        Assert.assertNotNull(versionData)

        Assert.assertEquals(expectedVersionCode, versionData.versionCode)
        Assert.assertEquals("1.2." + (expectedVersionCode + generationData.patchOffset + patchOffset), versionData.versionName)
    }

    @Test
    fun testCircleCi() {
        verifyHappyPaths("CIRCLE_BUILD_NUM", EnvBuildVersionGenerator.CircleCi(), reportedBuildNumber, 0)
    }

    @Test
    fun testCircleCiWithOffset() {
        verifyHappyPaths("CIRCLE_BUILD_NUM", EnvBuildVersionGenerator.CircleCi(-10, 9), reportedBuildNumber - 10, 9)
    }

    @Test
    fun testShippable() {
        verifyHappyPaths("BUILD_NUMBER", EnvBuildVersionGenerator.Shippable(), reportedBuildNumber, 0)
    }

    @Test
    fun testShippableWithOffset() {
        verifyHappyPaths("BUILD_NUMBER", EnvBuildVersionGenerator.Shippable(-20, 100), reportedBuildNumber - 20, 100)
    }
}