
# Banking System Application  

A secure, feature-rich banking application built with Java and Spring Boot.  

## Features
- User authentication and role-based access.
- Account management and transaction features.
- Currency conversion and online shopping services.
- Secure communication via email notifications.

## Project Structure
- **Controllers**: Manage HTTP requests.
- **Services**: Core business logic.
- **Entities**: Database entities (e.g., Account, User).
- **Repositories**: Data access layer.

## Getting Started
### Prerequisites
- Java 11 or higher.
- Maven 3.x.
- MySQL for the database.

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd banking-system
   ```
2. Import the project into your favorite IDE (e.g., IntelliJ, Eclipse).
3. Configure the database:
   - Create a MySQL database using `bankingdb.sql`.
   - Update `application.properties` with database credentials.

4. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Running Tests
Run unit tests using:
```bash
mvn test
```

## Contributing
Feel free to fork and create pull requests. For significant changes, please open an issue first to discuss.

## License
This project is licensed under the [MIT License](LICENSE).
