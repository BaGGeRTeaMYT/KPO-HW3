# Проблемы с Kafka
Если с первого раза всё хорошо запустилось, но сюда можно не смотреть (скорее всего так и будет, но на всякий случай решил оставить этот файлик).
## Проблема: InconsistentClusterIdException

У меня как-то раз вылезла вот такая гадость:
```
kafka.common.InconsistentClusterIdException: The Cluster ID wkXnRFDRTauf3PGJ4IfvZQ doesn't match stored clusterId Some(-YOtNsdERmCXphL5WiKFuQ) in meta.properties
```

То есть Kafka хочет залезть не туда.

## Решение

### Выключи и включи

Я попросил ИИшку нагенерить мне крутой скрипт, который чистит данные Kafka и Zookeeper и они начинают жизнь с чистого листа:
```powershell
.\clean-kafka.ps1
```

Или с автоматическим подтверждением:
```powershell
.\clean-kafka.ps1 -Force
```

### Ручками

Если талант искусственного интеллекта в написании скриптов всё-таки подвёл (или если вы не на винде сидите), то придётся следующие команды написать (как хакеры жёсткие)

```powershell
docker-compose down

docker volume rm kpo-hw3_kafka_data
docker volume rm kpo-hw3_zookeeper_data
docker volume rm kpo-hw3_zookeeper_logs
```

После успешного исполнения приложение должно корректного запускаться.