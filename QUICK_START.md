# Быстрый запуск приложения

## Предварительные требования

1. **Docker Desktop** - установлен и запущен
2. **PowerShell** - запущен от имени администратора
3. **Maven** - для сборки Java приложений (опционально, если используете скрипт)

## Автоматический запуск (рекомендуется)

1. Откройте PowerShell от имени администратора
2. Перейдите в директорию проекта
3. Выполните команду:

```powershell
.\start-app.ps1
```

Скрипт автоматически:
- Проверит наличие Docker
- Соберет все сервисы
- Запустит инфраструктуру (PostgreSQL, RabbitMQ)
- Запустит все микросервисы
- Покажет статус и доступные URL

## Ручной запуск

```bash
# Сборка и запуск всех сервисов
docker-compose up --build -d

# Проверка статуса
docker-compose ps
```

## Доступные сервисы

После успешного запуска будут доступны:

- **API Gateway**: http://localhost:8080
- **Orders Service**: http://localhost:8081
- **Payments Service**: http://localhost:8082
- **RabbitMQ Admin**: http://localhost:15672 (guest/guest)
- **PostgreSQL**: localhost:5432

## Swagger UI для тестирования

- **Orders Service Swagger**: http://localhost:8080/swagger-ui/index.html
- **Payments Service Swagger**: http://localhost:8080/payments-swagger-ui/index.html

## Тестирование

1. Откройте Swagger UI для Orders Service: http://localhost:8080/swagger-ui/index.html
2. Создайте заказ через API
3. Откройте Swagger UI для Payments Service: http://localhost:8080/payments-swagger-ui/index.html
4. Создайте счет и пополните его
5. Наблюдайте за изменением статуса заказа

## Остановка

```powershell
.\stop-app.ps1
```

Или вручную:

```bash
docker-compose down
```

## Troubleshooting

### Проблемы с портами
Если порты заняты, измените их в `docker-compose.yml`

### Проблемы с Docker
Убедитесь, что Docker Desktop запущен и имеет достаточно ресурсов (4GB+ RAM)

### Логи
```bash
# Все логи
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f orders-service
```

## Postman коллекция

Импортируйте `postman-collection.json` в Postman для тестирования API. 