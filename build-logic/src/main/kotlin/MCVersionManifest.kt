import kotlinx.serialization.Serializable

@Serializable
class MCVersionManifest(val latest: Latest, val versions: List<Version>) {
    @Serializable
    class Latest(val release: String, val snapshot: String)
    @Serializable
    class Version(val id: String, val type: String, val url: String, val time: String, val releaseTime: String, val sha1: String, val complianceLevel: Int)
}
