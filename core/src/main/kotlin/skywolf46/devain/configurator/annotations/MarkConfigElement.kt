package skywolf46.devain.configurator.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MarkConfigElement(val name: String = "")