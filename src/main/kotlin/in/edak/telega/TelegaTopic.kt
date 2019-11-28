package `in`.edak.messages

class TelegaTopic(val token: String, val charId: Long, val telega: Telega): Topic {
    override fun send(msg: String) {
        telega.sendToTopic(msg,charId,token)
    }
}