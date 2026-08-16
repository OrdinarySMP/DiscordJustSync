import dev.kikugie.stonecutter.StonecutterExperimentalAPI
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import kotlinx.serialization.json.Json
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@OptIn(StonecutterExperimentalAPI::class)
class Context(
	val project: Project,
	val extension: ModPlatformExtension,
	val loader: Loader,
	val stonecutter: StonecutterBuildExtension
) {
	private fun require(key: String): String =
		runCatching { project.sc.properties.get<String>(key) }.getOrNull()?.takeIf { it.isNotBlank() }
			?: error("Missing required property '$key' in stonecutter.properties.toml")

	private fun optional(key: String, fallback: String = ""): String =
		runCatching { project.sc.properties.get<String>(key) }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback

	val currentMcVersion: String by lazy {
		stonecutter.current.version
	}

	val modId: String by lazy { require("mod.id") }
	val modName: String by lazy { require("mod.name") }
	val modGroup: String by lazy { require("mod.group") }
	val modVersion: String by lazy { require("mod.version") }
	val channelTag: String by lazy { optional("mod.channel_tag") }
	val description: String by lazy { optional("mod.description") }
	val licenseName: String by lazy { require("mod.license.name") }
	val licenseUrl: String by lazy { require("mod.license.url") }
	val licenseDist: String by lazy { optional("mod.license.dist", "repo") }
	val inceptionYear: String by lazy { optional("mod.inception_year") }

	val authors: List<String> by lazy {
		runCatching {
			project.sc.properties.raw("mod", "authors").asList().map { it.toString() }
		}.getOrElse { error("Missing or malformed 'mod.authors' in stonecutter.properties.toml") }
	}

	val contributors: List<String> by lazy {
		runCatching {
			project.sc.properties.raw("mod", "contributors").asList().map { it.toString() }
		}.getOrElse { emptyList() }
	}

	val sourcesUrl: String by lazy { require("mod.sources_url") }
	val homepageUrl: String by lazy { require("mod.homepage_url") }
	val discordUrl: String by lazy { optional("mod.discord_url") }
	val issuesUrl: String by lazy { optional("mod.issues_url", "$sourcesUrl/issues") }

	val isSnapshot: Boolean by lazy { !project.envTrue("MOD_IS_RELEASE") }
	val baseVersion: String by lazy { "$modVersion$channelTag" }
	val snapshotSuffix: String by lazy { if (isSnapshot) "-SNAPSHOT" else "" }
	val fullVersion: String by lazy { "$baseVersion-${loader.id}+$currentMcVersion$snapshotSuffix" }
	val basicVersion: String by lazy { "$baseVersion$snapshotSuffix" }

	val publishAdditionalVersions: List<String> by lazy {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val manifest = Json.decodeFromString(MCVersionManifest.serializer(), response.body())
        val configuredVersions = stonecutter.versions.stream()
                .filter { v -> v.project.endsWith(loader.id) }
                .sorted { v1, v2 -> stonecutter.compare(v1.version, v2.version) }.toList()
        val currentVersionIndex = configuredVersions.indexOf(stonecutter.current)
        val nextConfiguredVersion =
                if (currentVersionIndex == configuredVersions.lastIndex)
                    manifest.latest.release
                else configuredVersions[currentVersionIndex + 1].version

        val additionalVersions: MutableList<String> = ArrayList()
        var collect = false
        for (version in manifest.versions) {
            if (version.type != "release") {
                continue
            }

            if (collect) {
                additionalVersions.add(version.id)
                if (version.id == currentMcVersion) {
                    break
                }
            }
            else if (version.id == nextConfiguredVersion) {
                collect = true
            }
        }
        additionalVersions
	}

	val javaVersion: JavaVersion by lazy {
		when {
			stonecutter.eval(currentMcVersion, ">=26") -> JavaVersion.VERSION_25
			stonecutter.eval(currentMcVersion, ">=1.20.6") -> JavaVersion.VERSION_21
			stonecutter.eval(currentMcVersion, ">=1.18") -> JavaVersion.VERSION_17
			stonecutter.eval(currentMcVersion, ">=1.17") -> JavaVersion.VERSION_16
			else -> JavaVersion.VERSION_1_8
		}
	}
}
