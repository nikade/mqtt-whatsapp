package `in`.edak.whatsapp

import `in`.edak.props.WhatsappProps
import org.openqa.selenium.Keys
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteExecuteMethod
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.html5.RemoteLocalStorage
import java.net.URL
import java.util.concurrent.TimeUnit

class WhatsappSender(
    seleniumRemoteUrl: String,
    private val props: WhatsappProps,
    localStorageFile: LocalStorageFile
) {
    private val webDriver =
        RemoteWebDriver(URL(seleniumRemoteUrl), DesiredCapabilities.chrome()) // DesiredCapabilities.firefox())
    private val localStorage = RemoteLocalStorage(RemoteExecuteMethod(webDriver))

    companion object {
        val RN_REGEXP = "\r?\n".toRegex()
    }

    init {
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS)
        webDriver.get(props.webUrl)
        localStorageFile.loadLocalStorage(localStorage)
        webDriver.navigate().refresh()
        getOrWaitElementXPath(props.pathFindChatField,60000L) ?: throw ErrorInformException("Bad page or need auth")
        localStorageFile.saveLocalStorage(localStorage)
    }

    fun sendMessage(contact: String, message: String) {
        // <input type="text" class="_2zCfw copyable-text selectable-text" data-tab="2" dir="auto" title="Поиск или новый чат" value="">
        webDriver.navigate().refresh()
        val findChat = getOrWaitElementXPath(props.pathFindChatField) ?: throw ErrorInformException("Не найдена строка поиска чата")
        findChat.click()
        findChat.sendKeys(contact)
        //<span dir="auto" title="Маша" class="_19RFN"><span class="matched-text">Маша</span></span>
        val chat = getOrWaitElementXPath(props.pathContactField.format(contact)) ?: throw ErrorInformException("Не найден чат")
        chat.click()
        //<div class="wjdTm" style="visibility: visible;">Введите сообщение</div>
        val messageInputElement = getOrWaitElementXPath(props.pathMessageField) ?: throw ErrorInformException("Не найдено поле для ввода сообщения")
        val messageToSend = message.replace(RN_REGEXP, "${Keys.SHIFT}${Keys.RETURN}${Keys.SHIFT}") + Keys.RETURN
        messageInputElement.sendKeys(messageToSend)
    }

    private fun getOrWaitElementXPath(xpath: String, timeOutMilis: Long = 10000, pause: Long = 500): WebElement? {
        (0..timeOutMilis/pause).forEach {
            val result = try {
                webDriver.findElementByXPath(xpath)
            } catch (e: NoSuchElementException) {
                null
            }
            if(result != null) return result
            Thread.sleep(pause)
        }
        return null
    }

    fun close() {
        webDriver.close()
    }
}