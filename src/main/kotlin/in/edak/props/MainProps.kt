package `in`.edak.props

data class MainProps(
    val seleniumUrl: String,
    val localStorageFile: String,
    val mqttBroker: String,
    val mqttQueue: String,
    val mqttUsername: String,
    val mqttPassword: String,
    val mqttClientId: String
)