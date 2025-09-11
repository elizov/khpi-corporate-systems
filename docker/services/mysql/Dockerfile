FROM mysql:8.0

# Config
COPY my.cnf /etc/mysql/my.cnf

# Migrations
COPY ./migrations/*.sql /docker-entrypoint-initdb.d/
