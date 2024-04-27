package skywolf46.devain.configurator

import skywolf46.devain.configurator.annotations.ConfigDefault
import skywolf46.devain.configurator.annotations.MarkConfigElement
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class ConfigDocumentRoot(private val configRoot: File, private val configFileName: String) {
    private val expectedDocuments = mutableListOf<ExpectedDocument>()

    init {
        if (!configRoot.exists())
            configRoot.mkdirs()
    }

    fun loadSharedDocument(adaptor: ConfigAdaptor) {
        val file = configRoot.resolve(configFileName)
        if (!file.exists()) {
            file.createNewFile()
            file.outputStream().use {
                adaptor.serializeTo(it, expectedDocuments.associate { expected -> expected.key to expected.extractDefaultValue() })
            }
        }
        val loaded = file.inputStream().use {
            adaptor.serializeFrom(it)
        }
        expectedDocuments.forEach {
            if (loaded[it.key] == null)
                return@forEach
            it.callFromMap(loaded[it.key] as Map<String, Any>)
        }

    }

    fun <T : ConfigElement> fetchDocument(expected: KClass<T>, adaptor: ConfigAdaptor, key: String): T {
        return configRoot.resolve(key).inputStream().use {
            adaptor.serializeFrom(it)
        } as T
    }

    fun <T : ConfigElement> fetchSharedDocument(expected: KClass<T>, key: String, unit: (T) -> Unit) : ConfigDocumentRoot {
        val expectedDocument = expectedDocuments.firstOrNull { it.key == key }
            ?: ExpectedDocument(key, expected).also {
                expectedDocuments.add(it)
            }
        expectedDocument.addListener {
            unit(it as T)
        }
        return this
    }

    fun newDocument(fileName: String): ConfigDocumentRoot {
        return ConfigDocumentRoot(configRoot.resolve(fileName), fileName)
    }

    class ExpectedDocument(val key: String, val cls: KClass<out Any>) {
        private val listeners = mutableListOf<(Any) -> Unit>()

        private val lock = ReentrantReadWriteLock()

        fun addListener(unit: (Any) -> Unit) {
            lock.write {
                listeners.add(unit)
            }
        }

        fun callListener(data: Any) {
            lock.read {
                listeners.forEach {
                    it(data)
                }
            }
        }

        fun constructFromMap(map: Map<String, Any>): Any {
            return cls.constructors.first().callBy(
                cls.constructors.first().parameters.filter { it.hasAnnotation<MarkConfigElement>() }.map {
                    it to (map[it.findAnnotation<MarkConfigElement>()!!.name.ifBlank { it.name!! }]
                        ?: if (it.isOptional) null else throw IllegalStateException("Missing key: ${it.name}"))
                }.associate { it }
            )
        }

        fun callFromMap(map: Map<String, Any>) {
            callListener(constructFromMap(map))
        }

        fun extractDefaultValue(): Map<String, Any?> {
            return cls.constructors.first().parameters.filter { it.hasAnnotation<MarkConfigElement>() }.map {
                (it.findAnnotation<MarkConfigElement>()!!.name.ifBlank { it.name!! }) to (it.findAnnotation<ConfigDefault.String>()?.default
                    ?: it.findAnnotation<ConfigDefault.Int>()?.default)
            }.filter { it.second != null }.associate { it }
        }
    }
}

inline fun <reified T : ConfigElement> ConfigDocumentRoot.fetchDocument(adaptor: ConfigAdaptor, key: String): T {
    return fetchDocument(T::class, adaptor, key)
}


inline fun <reified T : ConfigElement> ConfigDocumentRoot.fetchSharedDocument(key: String, noinline unit: (T) -> Unit) : ConfigDocumentRoot {
    return fetchSharedDocument(T::class, key, unit)
}

