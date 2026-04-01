# 💳 SmartBank - Digital Banking Backend System

SmartBank is a full-stack banking application backend built using **Spring Boot** with **JWT authentication**, providing secure and scalable banking operations. It also includes a lightweight integrated UI for testing core functionalities.

---

## 🚀 Features

* 🔐 User Authentication (JWT-based login & registration)
* 🏦 Account Management
* 💰 Check Account Balance
* 💸 Send / Receive Money
* 📊 Transaction Management
* 🧾 RESTful API Design
* 🌐 Integrated UI (HTML, CSS, JavaScript)
* 📘 API Documentation using Swagger

---

## 🛠️ Tech Stack

**Backend:**

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA (Hibernate)

**Database:**

* MySQL

**Frontend (Basic UI):**

* HTML
* CSS
* JavaScript

**Tools & Technologies:**

* Maven
* Postman (API Testing)
* Git & GitHub

---

## 📂 Project Structure

```
com.smartbank
├── controller       # REST Controllers
├── service          # Business Logic
├── repository       # Database Access Layer
├── entity           # JPA Entities
├── config           # Security & Configurations
├── security         # JWT & Authentication
```

---

## ⚙️ Setup & Installation

### 1️⃣ Clone the Repository

```
git clone https://github.com/YOUR_USERNAME/SmartBank.git
cd SmartBank
```

---

### 2️⃣ Configure Database

Update `application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/smartbank
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

---

### 3️⃣ Run the Application

```
mvn spring-boot:run
```

---

### 4️⃣ Access Application

* 🌐 UI:
  http://localhost:9090/index.html

* 📘 Swagger API Docs:
  http://localhost:9090/swagger-ui/index.html

---

## 🔑 API Highlights

| Feature        | Endpoint                        |
| -------------- | ------------------------------- |
| Register       | POST /auth/register             |
| Login          | POST /auth/login                |
| Get Balance    | GET /api/accounts/balance/{id}  |
| Transfer Money | POST /api/transactions/transfer |

---

## 🔒 Security

* JWT-based authentication
* Role-based authorization
* Secure REST endpoints

---

## 📸 Screenshots (Add Later)

* Login Page
* Dashboard
* API Testing (Postman)

---

## 🎯 Future Enhancements

* React-based frontend
* Docker containerization
* Kubernetes deployment
* CI/CD integration
* Cloud deployment (AWS)

---

## 👨‍💻 Author

**Sunil Reddy**

* GitHub: https://github.com/SunilReddy32
* LinkedIn: https://www.linkedin.com/in/csunilreddy

---

## ⭐ If you like this project

Give it a ⭐ on GitHub and feel free to contribute!
