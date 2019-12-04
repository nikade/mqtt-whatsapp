package `in`.edak.main

import `in`.edak.messages.Topic
import `in`.edak.whatsapp.ErrorInformException
import `in`.edak.whatsapp.WhatsappSender
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.InterceptPublishMessage
import io.netty.buffer.ByteBufUtil

class MqttListener(private val whatsApp: WhatsappSender, private val errorTopic: Topic) : AbstractInterceptHandler() {
    override fun getID(): String {
        return "WhatsappSenderMqttListener"
    }

    override fun onPublish(msg: InterceptPublishMessage) {
        val decodedPayload = String(ByteBufUtil.getBytes(msg.payload), Charsets.UTF_8)
        println("Received on topic: " + msg.topicName + " content: " + decodedPayload)
        try {
            whatsApp.sendMessage(msg.topicName, decodedPayload)
        } catch (e: ErrorInformException) {
            if(e.message != null) errorTopic.send(e.message)
        }
    }
}