package skywolf46.devain.configurator.yaml

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import skywolf46.devain.configurator.ConfigAdaptor
import java.io.*
import java.nio.charset.StandardCharsets

class YamlAdaptor(
    private val yaml: Yaml = Yaml(DumperOptions().apply {
        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    })
) : ConfigAdaptor {

    companion object {
        val DEFAULT = YamlAdaptor()
    }

    override fun serializeTo(stream: OutputStream, data: Map<String, Any>) {
        BufferedWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8)).use {
            it.append(yaml.dump(data))
        }
    }

    override fun serializeFrom(stream: InputStream): Map<String, Any> {
        val data = BufferedReader(InputStreamReader(stream)).use {
            yaml.load<Any>(it)
        }
        if (data is Map<*, *>) {
            return data as Map<String, Any>
        }
        return mapOf("root" to data)
    }
}