package `in`.edak.main

/*
 * Copyright (c) 2012-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

import `in`.edak.messages.Telega
import `in`.edak.messages.TelegaTopic
import `in`.edak.mqtt.WhatsappMqttMessageListener
import `in`.edak.props.AllProps
import `in`.edak.props.MainProps
import `in`.edak.props.TelegaProps
import `in`.edak.props.WhatsappProps
import `in`.edak.whatsapp.LocalStorageFile
import `in`.edak.whatsapp.WhatsappSender
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.tinylog.kotlin.Logger

import java.io.IOException

object MainScheduler {
    @Throws(InterruptedException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Logger.info("load properties")
        // load properties
        val allProps = AllProps()
        val telegaProps = allProps.getProps(TelegaProps::class, "telega.") as TelegaProps
        val whatsappProps = allProps.getProps(WhatsappProps::class, "whatsapp.") as WhatsappProps
        val mainProps = allProps.getProps(MainProps::class,"main.") as MainProps
        val telega = Telega(
            telegaProps.proxyHost,
            telegaProps.proxyPort?.toInt(),
            telegaProps.proxyUsername,
            telegaProps.proxyPassword)
        val telegaErrorTopic = TelegaTopic(telegaProps.token,telegaProps.chatId.toLong(),telega)

        Logger.info("init WhatsappWeb")
        // cookies file loader
        val cookiesFile = LocalStorageFile(mainProps.localStorageFile)
        val whatsAppSender = WhatsappSender(mainProps.seleniumUrl,whatsappProps,cookiesFile)

        Logger.info("init mqttClient")
        MqttClient(mainProps.mqttBroker,mainProps.mqttClientId).let { mqttClient ->
            mqttClient.connect(MqttConnectOptions().apply {
                isCleanSession = true
                userName = mainProps.mqttUsername
                password = mainProps.mqttPassword.toCharArray()
            })
            mqttClient.subscribe(
                mainProps.mqttQueue,
                WhatsappMqttMessageListener(whatsAppSender, telegaErrorTopic)
            )
        }

        Logger.info("Join thread, forever sleep")
        Thread().join()
    }
}