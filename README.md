# Интернет-магазин - Микросервисная архитектура

## Описание проекта

Данный проект представляет собой микросервисную архитектуру интернет-магазина, состоящую из следующих компонентов:

- **API Gateway** - маршрутизация запросов к микросервисам
- **Orders Service** - управление заказами
- **Payments Service** - управление платежами и счетами
- **PostgreSQL** - база данных
- **Kafka** - брокер сообщений

## Архитектура

### Микросервисы

1. **API Gateway (порт 8080)**
   - Роутинг запросов к микросервисам
   - Единая точка входа для всех API
   - Проксирование Swagger UI

2. **Orders Service (порт 8081)**
   - Создание заказов
   - Просмотр списка заказов
   - Просмотр статуса заказа
   - WebSocket уведомления об изменении статуса
   - Transactional Outbox паттерн
   - Swagger UI для тестирования API

3. **Payments Service (порт 8082)**
   - Создание счетов пользователей
   - Пополнение счетов
   - Просмотр баланса
   - Обработка платежей за заказы
   - Transactional Inbox/Outbox паттерны
   - Exactly Once семантика
   - Swagger UI для тестирования API

### Инфраструктура

- **PostgreSQL** - основная база данных
- **Kafka** - брокер сообщений для асинхронной коммуникации
- **Zookeeper** - управляет Kafka
- **Docker Compose** - оркестрация контейнеров

## Требования

- Docker Desktop
- Docker Compose
- PowerShell (для Windows)
- Минимум 4GB RAM
- 10GB свободного места на диске

## Быстрый запуск

### Вариант 1: Автоматический запуск (Windows)

1. Откройте PowerShell от имени администратора
2. Перейдите в директорию проекта
3. Выполните команду:

```powershell
.\start-app.ps1
```

### Вариант 2: Ручной запуск

1. Убедитесь, что Docker Desktop запущен
2. Откройте терминал в корневой директории проекта
3. Выполните команды:

```bash
# Сборка и запуск всех сервисов
docker-compose up --build -d

# Проверка статуса сервисов
docker-compose ps
```

## API Endpoints

### Orders Service

- `POST /api/users/{userId}/orders` - Создание заказа
- `GET /api/users/{userId}/orders` - Получение списка заказов пользователя
- `GET /api/users/{userId}/orders/{orderId}` - Получение информации о заказе

### Payments Service

- `POST /api/users/{userId}/payments/accounts` - Создание счета
- `POST /api/users/{userId}/payments/accounts/deposit` - Пополнение счета
- `GET /api/users/{userId}/payments/accounts/balance` - Просмотр баланса

### WebSocket

- `ws://localhost:8080/ws/orders` - WebSocket для уведомлений о статусе заказов

## Swagger UI

Для тестирования API доступны Swagger UI интерфейсы:

- **Orders Service Swagger**: http://localhost:8080/swagger-ui/index.html
- **Payments Service Swagger**: http://localhost:8080/payments-swagger-ui/index.html

## Тестирование API

### Создание заказа

```bash
curl -X POST http://localhost:8080/api/users/1/orders \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.50}'
```

### Создание счета

```bash
curl -X POST http://localhost:8080/api/users/1/payments/accounts \
  -H "Content-Type: application/json"
```

### Пополнение счета

```bash
curl -X POST http://localhost:8080/api/users/1/payments/accounts/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}'
```

### Просмотр баланса

```bash
curl -X GET http://localhost:8080/api/users/1/payments/accounts/balance
```

### Получение списка заказов

```bash
curl -X GET http://localhost:8080/api/users/1/orders
```

## Мониторинг

### Логи сервисов

```bash
# Логи всех сервисов
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f orders-service
docker-compose logs -f payments-service
docker-compose logs -f api-gateway
```

### Health Checks

- API Gateway: http://localhost:8080/actuator/health
- Orders Service: http://localhost:8081/actuator/health
- Payments Service: http://localhost:8082/actuator/health

## Остановка приложения

### Автоматический способ

```powershell
.\stop-app.ps1
```

### Ручной способ

```bash
docker-compose down
```

Для полной очистки (включая данные):

```bash
docker-compose down -v
```

## Разработка

### Структура проекта

```
├── api-gateway/           # API Gateway сервис
├── orders-service/        # Сервис заказов
├── payments-service/      # Сервис платежей
├── docker-compose.yml    # Конфигурация Docker Compose
├── init-db.sql          # Инициализация базы данных
├── start-app.ps1        # Скрипт запуска (Windows)
├── stop-app.ps1         # Скрипт остановки (Windows)
├── postman-collection.json # Коллекция для тестирования
├── README.md            # Документация
├── QUICK_START.md       # Быстрый старт
└── .gitignore           # Исключения Git
```

### Локальная разработка

Для разработки отдельных сервисов:

1. Запустите инфраструктуру:
```bash
docker-compose up postgres zookeeper kafka -d
```

2. Запустите нужный сервис локально через IDE

### Тестирование

```bash
# Запуск тестов для всех сервисов
mvn test

# Проверка покрытия кода
mvn jacoco:report
```

## Troubleshooting

### Проблемы с Kafka

Если вы видите ошибку `InconsistentClusterIdException`, это означает конфликт Cluster ID в Kafka. Для решения:

#### Быстрое решение:
```powershell
.\clean-kafka.ps1
```

#### Запуск с очисткой:
```powershell
.\start-app.ps1 -CleanKafka
```

#### Подробная инструкция:
См. файл [KAFKA_TROUBLESHOOTING.md](KAFKA_TROUBLESHOOTING.md)

### Проблемы с портами

Если порты заняты, измените их в `docker-compose.yml`:

```yaml
ports:
  - "8081:8081"  # Измените на другой порт
```

### Проблемы с базой данных

```bash
# Пересоздание базы данных
docker-compose down -v
docker-compose up --build -d
```

### Проверка состояния сервисов

```powershell
# Статус контейнеров
docker-compose ps

# Логи Kafka
docker-compose logs kafka

# Логи всех сервисов
docker-compose logs -f
```

## Производительность

### Рекомендуемые настройки Docker

- Memory: 4GB+
- CPUs: 2+
- Swap: 1GB+

### Масштабирование

Для увеличения производительности можно масштабировать сервисы:

```bash
docker-compose up --scale orders-service=3 --scale payments-service=2
```

## Безопасность

- Все API запросы используют userId в URL для идентификации пользователя
- База данных изолирована в Docker сети
- Kafka доступен только внутри контейнеров

## Лицензия

MIT License 