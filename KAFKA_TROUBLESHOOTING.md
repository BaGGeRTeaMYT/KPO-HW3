# Kafka Troubleshooting Guide

## Проблема: InconsistentClusterIdException

Если вы видите ошибку:
```
kafka.common.InconsistentClusterIdException: The Cluster ID wkXnRFDRTauf3PGJ4IfvZQ doesn't match stored clusterId Some(-YOtNsdERmCXphL5WiKFuQ) in meta.properties
```

Это означает, что Kafka пытается подключиться к кластеру с другим Cluster ID, чем тот, который сохранен в файле `meta.properties`.

## Решения

### 1. Быстрая очистка (Рекомендуется)

Запустите скрипт очистки:
```powershell
.\clean-kafka.ps1
```

Или с автоматическим подтверждением:
```powershell
.\clean-kafka.ps1 -Force
```

### 2. Очистка при запуске

Запустите приложение с очисткой данных Kafka:
```powershell
.\start-app.ps1 -CleanKafka
```

### 3. Полная очистка при остановке

Остановите приложение с удалением всех данных:
```powershell
.\stop-app.ps1 -RemoveVolumes -CleanKafka
```

### 4. Ручная очистка

Если скрипты не работают, выполните команды вручную:

```powershell
# Остановить контейнеры
docker-compose down

# Удалить volumes Kafka и ZooKeeper
docker volume rm kpo-hw3_kafka_data
docker volume rm kpo-hw3_zookeeper_data
docker volume rm kpo-hw3_zookeeper_logs

# Запустить заново
.\start-app.ps1
```

## Причины проблемы

1. **Некорректное завершение работы** - Kafka не успел корректно сохранить состояние
2. **Конфликт данных** - Остались данные от предыдущих запусков
3. **Проблемы с Docker volumes** - Повреждение данных в volumes

## Профилактика

1. Всегда используйте `.\stop-app.ps1` для корректной остановки
2. При проблемах используйте `.\clean-kafka.ps1` перед перезапуском
3. Регулярно очищайте данные при разработке

## Проверка состояния

Проверить логи Kafka:
```powershell
docker-compose logs kafka
```

Проверить состояние контейнеров:
```powershell
docker-compose ps
```

Проверить volumes:
```powershell
docker volume ls | findstr kpo-hw3
``` 