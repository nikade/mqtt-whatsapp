package `in`.edak.mqtt

import `in`.edak.messages.Topic
import `in`.edak.whatsapp.ErrorInformException
import `in`.edak.whatsapp.WhatsappSender
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.tinylog.kotlin.Logger

class WhatsappMqttMessageListener(private val whatsApp: WhatsappSender, private val errorTopic: Topic) : IMqttMessageListener {
    companion object {
        val topicFilter = "^[^/]+/".toRegex()
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if(topic == null || message == null) {
            Logger.info("skip message")
            return
        }
        val decodedPayload = String(message.payload, Charsets.UTF_8)
        Logger.info("Received on topic: $topic content: $decodedPayload")
        try {
            val chat = topic.replace(topicFilter,"")
            whatsApp.sendMessage(chat, decodedPayload)
        } catch (e: Throwable) { // ErrorInformException) {
            if(e.message != null) errorTopic.send(e.message ?: "")
        }
    }
}