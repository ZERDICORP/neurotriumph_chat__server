# neurotriumph_chat__server :robot: :speech_balloon: :man:
#### Turing test online platform (chat).

## How to start? :eyes:
> Work done in IntellijIDEA

#### 1. Make sure you have mariadb installed
```
$ mysql --version
mysql  Ver 15.1 Distrib 10.7.3-MariaDB, for Linux (x86_64) using readline 5.1
```

#### 2. Create a databases called `neurotriumph` and `neurotriumph_test`
```
$ mysql -u root -p
MariaDB> create database neurotriumph;
MariaDB> create database neurotriumph_test;
MariaDB> exit;
```

#### 3. Add file `src/main/resources/application.properties` with the following content
> Replace all values between <>.
```
# Custom Application Configuration
app.secret=abc123
app.lobby_spent_time=30000
app.chat_messaging_delay=5000

# Database Configuration
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/neurotriumph?autoReconnect=true
spring.datasource.username=<DATABASE_USER>
spring.datasource.password=<DATABASE_USER_PASSWORD>

# Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDB103Dialect

# Spring Server Configuration
server.port=8000
server.error.include-message=always
server.servlet.context-path=/api/v1
```

#### 4. Add file `src/test/resources/test.properties` with the following content
> Replace all values between <>.
```
# Custom Application Configuration
app.secret=abc123
app.lobby_spent_time=0
app.chat_messaging_delay=0

# Database Configuration
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/neurotriumph_test?autoReconnect=true
spring.datasource.username=<DATABASE_USER>
spring.datasource.password=<DATABASE_USER_PASSWORD>

# Hibernate Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDB103Dialect

# Spring Server Configuration
server.port=8000
server.error.include-message=always
server.servlet.context-path=/api/v1
```

#### 5. Now you can run the project :tada:
