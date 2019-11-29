package `in`.edak.props

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import java.io.InputStreamReader



class AllProps {
    val PROPERTIES_FILENAME = "mqtt-whatsapp.properties"

    lateinit var allPropsMap: Map<String,Any>

    init {
        loadPropeties()
    }

    private fun loadPropeties() {
        val properties = Properties()
        var propertiesInputSteam: InputStream?
        try {
            propertiesInputSteam = FileInputStream(PROPERTIES_FILENAME)
        } catch (e: FileNotFoundException) {
            propertiesInputSteam = javaClass.classLoader.getResourceAsStream(PROPERTIES_FILENAME)
        }
        if (propertiesInputSteam == null) throw RuntimeException("could not load ${PROPERTIES_FILENAME}")
        val inputStreamReader = InputStreamReader(propertiesInputSteam, "UTF-8")
        properties.load(inputStreamReader)
        allPropsMap = properties.toMap().map { it.key as String to it.value }.toMap()
    }

    private fun filterValues(map: Map<String, Any>, startStr: String): Map<String, Any> {
        val posToSubStr = startStr.length
        return map.map {
            if (it.key.startsWith(startStr))
                it.key.substring(posToSubStr) to it.value
            else null
        }
            .filterNotNull()
            .toMap()
    }

    fun getProps(kClass: KClass<*>,name: String): Any {
        val values = filterValues(allPropsMap, name)
        val constructor = kClass.primaryConstructor!!
        val constructorParams = constructor.parameters.associateBy({ it }, { values.get(it.name) })
        val props = constructor.callBy(constructorParams)
        return props
    }


}
