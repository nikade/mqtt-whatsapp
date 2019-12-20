package `in`.edak.whatsapp

import `in`.edak.props.WhatsappProps
import org.openqa.selenium.Keys
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteExecuteMethod
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.html5.RemoteLocalStorage
import org.tinylog.kotlin.Logger
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule
import kotlin.contracts.contract

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
        const val REFRESH_WEBDRIVER_PERIOD = 10L*60*1000 // ten minutes
    }

    init {
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS)
        webDriver.get(props.webUrl)
        localStorageFile.loadLocalStorage(localStorage)
        //webDriver.navigate().refresh()
        webDriver.get(webDriver.currentUrl)
        getOrWaitElementXPath(webDriver, props.pathFindChatField, 60000L)
            ?: throw ErrorInformException("Bad page or need auth")
        localStorageFile.saveLocalStorage(localStorage)
        scheduleWebDriverRefresher(REFRESH_WEBDRIVER_PERIOD)
    }

    fun sendMessage(contact: String, message: String) {
        Logger.info { "Contact=$contact message=$message" }
        synchronized(webDriver) {
            // <input type="text" class="_2zCfw copyable-text selectable-text" data-tab="2" dir="auto" title="Поиск или новый чат" value="">
            webDriver.get(webDriver.currentUrl)
            Thread.sleep(10000)
            val findChat = getOrWaitElementXPath(webDriver, props.pathFindChatField)
                ?: throw ErrorInformException("Не найдена строка поиска чата")
            findChat.click()
            //webDriver.executeScript("arguments[0].click();", findChat) не работает
            findChat.sendKeys(contact)
            //<span dir="auto" title="Маша" class="_19RFN"><span class="matched-text">Маша</span></span>
            val chat = getOrWaitElementXPath(webDriver, props.pathContactField.format(contact))
                ?: throw ErrorInformException("Не найден чат")
            chat.click()
            // webDriver.executeScript("arguments[0].click();", chat) не работает
            //<div class="wjdTm" style="visibility: visible;">Введите сообщение</div>
            val messageInputElement = getOrWaitElementXPath(webDriver, props.pathMessageField)
                ?: throw ErrorInformException("Не найдено поле для ввода сообщения")
            val messageToSend = message.replace(RN_REGEXP, "${Keys.SHIFT}${Keys.RETURN}${Keys.SHIFT}") + Keys.RETURN
            messageInputElement.sendKeys(messageToSend)
        }
        Logger.info("sending done")

    }

    private fun getOrWaitElementXPath(
        wd: RemoteWebDriver,
        xpath: String,
        timeOutMilis: Long = 10000,
        pause: Long = 500
    ): WebElement? {
        (0..timeOutMilis / pause).forEach {
            val result = try {
                wd.findElementByXPath(xpath)
            } catch (e: NoSuchElementException) {
                null
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
            if (result != null) return result
            Thread.sleep(pause)
        }
        return null
    }

    fun close() {
        webDriver.close()
    }

    private fun scheduleWebDriverRefresher(periodMillis: Long) {
        Timer().schedule(periodMillis,periodMillis) {
            try {
                Logger.info("refresh start")
                synchronized(webDriver) {
                    getOrWaitElementXPath(webDriver, props.pathFindChatField) ?:
                        ErrorInformException("WebDriver refresher - could not get findChat field")
                }
                Logger.info("refresh done")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}