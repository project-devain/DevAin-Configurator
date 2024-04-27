# Devain Configurator

Devain Configurator은 Devain 프로젝트에서의 설정 파일 데이터를 관리하는 클래스입니다.

맵 기반 설정 파일에 대해 호환성을 가집니다.

## 종속성 추가
build.gradle에 다음과 같이 추가합니다 :

```groovy
repositories {
   maven {
     url "https://repo.trinarywolf.net/releases" 
   }
}

depdendencies {
    // YAML 설정 파일을 사용하는 경우
    implementation "skywolf46:devain-configurator-yaml:1.3.0"
}
```

## 사용법

### REST 호출

1. ConfigDocumentRoot 인스턴스를 생성합니다. 각 ConfigDocumentRoot는 하나의 설정 파일과 하나의 설정 파일 경로를 가집니다.

```kotlin
  val document = ConfigDocumentRoot(File("configRoot"), "config.yml")
```

2. 설정 파일에 대응하는 클래스를 선언합니다. 모든 설정 파일에 대응되는 클래스는 ConfigElement를 상속받아야 합니다. 

    @MarkConfigElement는 이 변수에 할당된 이름을 변경하기 위해 쓰입니다.

    쓰이지 않았을 경우, 변수의 이름이 키 값으로 쓰입니다.

    만약 ConfigDefault로 기본 값을 설정하지 않았을 경우, 반드시 읽어올 값이 존재하지 않는 경우를 대비한 기본 값을 설정해야 합니다.

    ConfigDefault가 아닌 기본 값은 설정 파일에 쓰이지 않습니다.

```kotlin
data class TestConfigElement(
    @MarkConfigElement("Test Value")
    @ConfigDefault.String("Hello World")
    val testValue: String,
    @MarkConfigElement("Test Value 2")
    @ConfigDefault.Int(1)
    val testValue2: Int,
    @MarkConfigElement("Hidden Value 3")
    val hiddenValue3: String = "Hidden Value"
) : ConfigElement
```

3. 미리 생성한 ConfigDocumentRoot에 리스너를 추가합니다. 리스너는 파일을 읽어올때마다 호출됩니다.

```kotlin
document.fetchSharedDocument<TestConfigElement>("Test Key") { config ->
    println(config.testValue)
    println(config.testValue2)
    println(config.hiddenValue3)
}
```


4. 최종적으로, 설정 파일을 불러옵니다. 이 예제에서는 Yaml을 예제로 사용합니다.

```kotlin
document.loadSharedDocument(ConfigDocument.yaml())
```

5. 기본 설정 파일이 "configRoot/config.yml"에 자동 생성됩니다.

```yaml
Test Key:
  Test Value: "Hello World"
  Test Value 2: 1
```

6. 모든 리스너가 호출되었습니다.