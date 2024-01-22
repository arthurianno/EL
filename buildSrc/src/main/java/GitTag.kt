import java.util.Locale
import java.util.regex.Pattern

class GitTag private constructor(
    val majorVersion: String,
    val minorVersion: String,
    val hotfixVersion: String,
    val buildNumber: Int,
    val buildType: String
) {
    companion object {
        const val TAG_REGEX = "v(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)[-.](debug|release)"
        const val DEFAULT = "v1.0.0.1-debug"
        fun fromString(string: String): GitTag {
            val matcher = Pattern.compile(TAG_REGEX).matcher(string)
            return if (matcher.matches()) {
                GitTag(
                    majorVersion = matcher.group(1),
                    minorVersion = matcher.group(2),
                    hotfixVersion = matcher.group(3),
                    buildNumber = matcher.group(4).toInt(),
                    buildType = matcher.group(5)
                )
            } else {
                throw RuntimeException("$string tag does not match version scheme")
            }
        }
    }

    override fun toString(): String {
        return "GitTag {" +
                "majorVersion='" + majorVersion + '\'' +
                ", minorVersion='" + minorVersion + '\'' +
                ", hotfixVersion='" + hotfixVersion + '\'' +
                ", buildNumber='" + buildNumber + '\'' +
                ", buildType='" + buildType + '\'' +
                '}'
    }

    val versionName: String
        get() = String.format(
            Locale.US,
            "%s.%s.%s",
            majorVersion,
            minorVersion,
            hotfixVersion
        )


}
