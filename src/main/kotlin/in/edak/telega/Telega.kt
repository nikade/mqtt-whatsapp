package `in`.edak.messages

import okhttp3.*
import java.net.InetSocketAddress
import java.net.Proxy


class Telega(
    proxyHost: String? = null,
    proxyPort: Int? = null,
    private val proxyUsername: String? = null,
    private val proxyPassword: String? = null
) {
    private var proxyAuthenticator: Authenticator? = null
    private var okHttpClient: OkHttpClient

    constructor() : this(null,null,null,null) {
        if (proxyUsername != null && this.proxyPassword != null) {
            this.proxyAuthenticator = object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    val credential = Credentials.basic(proxyUsername, proxyPassword)
                    return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build()
                }
            }
        }
    }

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        if (proxyHost != null && proxyPort != null)
            okHttpClientBuilder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort)))

        if (proxyAuthenticator != null) {
            okHttpClientBuilder.proxyAuthenticator(proxyAuthenticator!!)
        }
        okHttpClient = okHttpClientBuilder.build()
    }

    companion object {
        val telegaUrl = "https://api.telegram.org"
    }

    fun sendToTopic(
        msg: String,
        chatId: Long,
        token: String
    ) {
        val url = "$telegaUrl/bot$token/sendMessage"
        val params = mapOf(
            "chat_id" to chatId.toString(),
            "text" to msg
        )
        doRequest(url, params)
    }

    fun doRequest(
        url: String,
        params: Map<String, String>? = null
    ): Response {
        val formBodyBuilder = FormBody.Builder()
        params?.forEach {
            formBodyBuilder.add(it.key, it.value)
        }
        val formBody = formBodyBuilder.build()
        val builder = Request.Builder().url(url)
        builder.post(formBody)
        val request = builder.build()
        val httpCall = okHttpClient.newCall(request)
        return httpCall.execute()
    }
}