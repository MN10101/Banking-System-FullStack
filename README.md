# Banking System Application  🏦

A secure, feature rich banking application built with Java and Spring Boot.

# Author
-  Mahmoud Najmeh


<img src="https://avatars.githubusercontent.com/u/78208459?u=c3f9c7d6b49fc9726c5ea8bce260656bcb9654b3&v=4" width="200px" style="border-radius: 50%;">

---

## Features 🛠️
🔒 User authentication and role-based access
💸 Money transfer with secure transaction processing
💱 Currency conversion for international transactions
🛍️ In app shopping services with exclusive offers and a wide range of items
📧 Secure communication via email notifications
🌍 Multi currency support
🔐 Identity protection features (tax number and ID verification)
🖥️ IP address tracking for secure logins

## Project Structure 🗂️
- **Controllers**: Manage HTTP requests.
- **DTO (Data Transfer Objects)**: Handle data transfer between layers
- **Entities**: Database entities (e.g., Account, User).
- **Repositories**: Data access layer.
- **Security**: Configuration for authentication and authorization.
- **Services**: Core business logic.
- **Utils**: Utility classes for common functionalities



## Getting Started 🚀
### Prerequisites 📋
- Java 11 or higher.
- Maven 3.x.
- MySQL for the database.

### Installation 🔧
1. Clone the repository:
   ```bash
   git clone <https://github.com/MN10101/Banking-System-Backend.git>
   ```
2. Import the project into your favorite IDE (e.g., IntelliJ, Eclipse).
3. Configure the database:
   - Create a MySQL database using `nexgindb.sql`.
   - Update `application.properties` with database credentials.

4. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Running Tests 🧪
Run unit tests using:
```bash
mvn test
```

Contact: mn.de@outlook.com 📧

## License
This project is licensed under the [MIT License]([LICENSE](https://github.com/MN10101/Banking-System-Backend?tab=MIT-1-ov-file)).
