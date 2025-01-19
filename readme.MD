# ModuleCI

## Описание

`ModuleCI` - это проект на Kotlin, который позволяет загружать файлы на удаленный сервер через SFTP, используя SSH-ключи для аутентификации. Конфигурация проекта хранится в файле конфигурации, который читается с помощью библиотеки `typesafe-config`.

## Требования

- JDK 17 - явных зависимостей от версии нет
- Gradle
- Интернет-соединение для загрузки зависимостей

## Установка

1. Клонируйте репозиторий:
    ```sh
    git clone <URL репозитория>
    cd ModuleCI
    ```

2. Синхронизируйте проект с Gradle:
    ```sh
    ./gradlew build
    ```

## Использование

1. Создайте файл конфигурации `config.conf` со следующим содержимым:
    ```hocon
    CI {
        host = "example.com"
        port = 22
        user = "username"
        privateKey = "path/to/private/key"
        remoteDir = "/remote/directory"
    }
    ```
где `host` - адрес удаленного сервера, `port` - порт, `user` - имя пользователя, `privateKey` - путь к приватному ключу в формате OpenSSH, `remoteDir` - путь к директории на удаленном сервере куда будет сохраняться файл.
Подробнее о имени дериктории на удаленном сервере можно узнать в описании [ModuleCD](https://github.com/ikermy/ModuleCD).
2. Добавьте в build.gradle целевого проекта следующее:
    ```sh
   tasks.register("runCmdScript") {
   dependsOn("test") // Опционально, задача shadowJar зависит от задачи test
        doLast {
            val jarFile = project.extra["jarFile"] as File // Передача параметров в bat-файл
            exec {
                commandLine("cmd", "/c", "<patch to scrypt file>/CI-SCRYPT.bat ${project.projectDir}/build/libs/${jarFile.name} ${project.projectDir}")
            }
        }
    }
    ```
   где `<localFile>` - путь к локальному файлу, который нужно загрузить на сервер, а `<configPath>` - путь к файлу конфигурации.

## Зависимости

Проект использует следующие библиотеки:

- `com.typesafe:config:1.4.2` - для работы с конфигурационными файлами
- `com.hierynomus:sshj:0.39.0` - для работы с SSH и SFTP
- `org.slf4j:slf4j-api:2.0.16` - для логирования
- `ch.qos.logback:logback-classic:1.5.12` - реализация логирования

## Авторы

- [kermy](https://github.com/ikermy)

## Лицензия

Этот проект лицензирован под лицензией MIT. Подробности см. в файле `LICENSE`.