[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=badge&logo=linkedin&logoColor=white)](http://linkedin.com/in/dmytro-trotsenko-97a6211a5)

# Reactive E-Store App with PostgreSQL and Spring Data R2DBC

This project is a fully Reactive application with **Spring Boot**, **Spring Webflux**, **Spring Security**, **PostgreSQL**, 
**Spring Data R2DBC**, **Flyway**, **Thymeleaf (HTML, CSS)**, **Bootstrap** and **JavaScript (a little)**.

The project includes API endpoint testing using a **PostgreSQL** test **Docker container**.

It is deployed on **AWS Elastic Beanstalk** and can be accessed at the following URL:<br/>
http://estore.us-east-1.elasticbeanstalk.com/

### Project structure


| Folder                   | Description                                                   |
|--------------------------|---------------------------------------------------------------|
| deploy                   | Contains the docker-compose.yml used to setup the application |
| src/main/java/com/estore | Spring boot application EStoreApp.java                        |

### How to build and run

In order to build the application you need to have the following software products installed:
- JDK >= 17
- Docker & Docker Compose

### How to login as Superuser

- Username -> admin
- Password -> admin

### Database Entity Diagram
![e_store_SQL](readme_img/e_store_SQL_table.png)

### Products landing page
![Home](readme_img/home.png)

### Add product
![Add_product](readme_img/add_product.png)

### Shopping cart
![Cart](readme_img/cart.png)

### Admin panel
![Admin](readme_img/admin.png)

### Account
![Account](readme_img/account.png)

### Address
![Address](readme_img/address.png)

### Login
![Login](readme_img/login.png)

### Registration
![Registr](readme_img/registration.png)

### Errors handling
![Err](readme_img/products_err.png)

### Exploring the Rest APIs

The server will start at <http://localhost:8080>.
You can also use the Swagger-UI to test the application.

The Swagger UI will open at : <http://localhost:8080/webjars/swagger-ui/index.html>
![users](readme_img/Users.png)
![products](readme_img/products.png)
![orders](readme_img/Orders.png)

You can test API endpoints using a test container with a PostgreSQL database.

ProductController tests result:
![Product_tests](readme_img/Product_tests.png)
OrderController tests result:
![Order_tests](readme_img/Order_tests.png)
UserController tests result:
![User_tests](readme_img/User_tests.png)

