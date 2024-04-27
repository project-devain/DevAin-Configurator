package skywolf46.devain.configurator

import java.io.InputStream
import java.io.OutputStream

interface ConfigAdaptor {
    companion object

    fun serializeTo(stream: OutputStream, data: Map<String, Any>)

    fun serializeFrom(stream: InputStream): Map<String, Any>
}