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
import `in`.edak.props.AllProps
import `in`.edak.props.MainProps
import `in`.edak.props.TelegaProps
import `in`.edak.props.WhatsappProps
import `in`.edak.whatsapp.LocalStorageFile
import `in`.edak.whatsapp.WhatsappSender
import io.moquette.broker.Server
import io.moquette.broker.config.ClasspathResourceLoader
import io.moquette.broker.config.ResourceLoaderConfig

import java.io.IOException

object Main {
    @Throws(InterruptedException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // load properties
        val allProps = AllProps()
        val telegaProps = allProps.getProps(TelegaProps::class, "telega.") as TelegaProps
        val whatsappProps = allProps.getProps(WhatsappProps::class, "whatsapp.") as WhatsappProps
        val mainProps = allProps.getProps(MainProps::class,"main.") as MainProps

        // cookies file loader
        val cookiesFile = LocalStorageFile(mainProps.localStorageFile)

        val telega = Telega(
            telegaProps.proxyHost,
            telegaProps.proxyPort?.toInt(),
            telegaProps.proxyUsername,
            telegaProps.proxyPassword)
        val telegaErrorTopic = TelegaTopic(telegaProps.token,telegaProps.chatId.toLong(),telega)

        val whatsAppSender = WhatsappSender(mainProps.seleniumUrl,whatsappProps,cookiesFile)

        val classpathLoader = ClasspathResourceLoader("moquette.conf")
        val classPathConfig = ResourceLoaderConfig(classpathLoader)
        val mqttBroker = Server()
        val listener = MqttListener(whatsAppSender,telegaErrorTopic)
        mqttBroker.startServer(classPathConfig, listOf(listener))

        println("Broker started press [CTRL+C] to stop")
        //Bind  a shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Stopping broker")
            mqttBroker.stopServer()
            println("Broker stopped")
            whatsAppSender.close()
            println("webdriver closed")
        })
    }
}