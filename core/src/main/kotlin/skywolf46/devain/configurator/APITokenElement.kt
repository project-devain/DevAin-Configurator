package skywolf46.devain.configurator

import skywolf46.devain.configurator.annotations.ConfigDefault
import skywolf46.devain.configurator.annotations.MarkConfigElement

data class APITokenElement(
    @MarkConfigElement
    @ConfigDefault.String("YOUR-API-TOKEN-HERE")
    val apiToken: String
) : ConfigElement