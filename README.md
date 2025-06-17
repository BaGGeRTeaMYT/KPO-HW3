# Контрольная работа 3 "Асинхронное межсервисное взаимодействие"

## Описание работы

Компоненты приложения:

- **API Gateway** - маршрутизация запросов к микросервисам
- **Orders Service** - управление заказами
- **Payments Service** - управление платежами и счетами
- **PostgreSQL** - база данных
- **Kafka** - брокер сообщений

## Архитектура

### Микросервисы

1. **API Gateway (порт 8080)**

Отвечает за routing запросов.

2. **Orders Service (порт 8081)**

Умеет создавать заказ, получать список заказов, получать конкретный заказ (по Id). Можно потыкаться в APIшке с помощью свагера по ссылке http://localhost:8081/swagger-ui/index.html.

3. **Payments Service (порт 8082)**

Может создать счёт для пользователя (каждый пользователь может иметь только один счёт, при попытке создания второго будет ошибка 500), пополнить счёт пользователя на какую-то величину (больше 0.01), посмотреть баланс конкретного пользователя. В API можно посмотреть по ссылке http://localhost:8082/swagger-ui/index.html.

### Инфраструктура

- **PostgreSQL** - база данных
- **Kafka** - брокер сообщений для асинхронной коммуникации
- **Zookeeper** - управляет Kafka
- **Docker Compose** - оркестрация контейнеров

## API Endpoints

### Orders Service

- `POST /api/users/{userId}/orders` - Создание заказа
- `GET /api/users/{userId}/orders` - Получение списка заказов пользователя
- `GET /api/users/{userId}/orders/{orderId}` - Получение информации о заказе

### Payments Service

- `POST /api/users/{userId}/payments/accounts` - Создание счета
- `POST /api/users/{userId}/payments/accounts/deposit` - Пополнение счета
- `GET /api/users/{userId}/payments/accounts/balance` - Просмотр баланса

## Swagger UI

Для тестирования API доступны Swagger UI интерфейсы:

- **Orders Service Swagger**: http://localhost:8081/swagger-ui/index.html
- **Payments Service Swagger**: http://localhost:8082/swagger-ui/index.html

### Health Checks

- API Gateway: http://localhost:8080/actuator/health
- Orders Service: http://localhost:8081/actuator/health
- Payments Service: http://localhost:8082/actuator/health