```mermaid
flowchart TD
    Client[Client Service<br/>React-клієнт]
    Gateway[API Gateway<br/>Маршрутизація запитів]

    Auth[Auth Service<br/>Авторизація та JWT]
    Product[Product Service<br/>Каталог товарів]
    Order[Order Service<br/>Створення замовлень]
    Admin[Admin Service<br/>Адмін-панель]

    AuthDB[(auth_db)]
    ProductDB[(product_db)]
    OrderDB[(order_db)]

    MQ[(RabbitMQ<br/>Черги повідомлень)]

    Client --> Gateway
    Gateway --> Auth
    Gateway --> Product
    Gateway --> Order
    Gateway --> Admin

    Auth --> AuthDB
    Product --> ProductDB
    Order --> OrderDB

    Order --> MQ
    Admin --> MQ
