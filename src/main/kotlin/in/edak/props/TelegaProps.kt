package `in`.edak.props

data class TelegaProps(
    val token: String,
    val chatId: String,
    val proxyHost: String?,
    val proxyPort: String?,
    val proxyUsername: String?,
    val proxyPassword: String?
)