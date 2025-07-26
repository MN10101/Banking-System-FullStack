
# Banking System - FullStack 🏦

A secure, full-featured banking system with both backend and frontend components.

## Author
**Mahmoud Najmeh**  
<img src="https://avatars.githubusercontent.com/u/78208459?u=c3f9c7d6b49fc9726c5ea8bce260656bcb9654b3&v=4" width="200px" style="border-radius: 50%;">

---

# 🛠 Backend - Banking System Application

Built with Java and Spring Boot

### Features
- 🔒 User authentication and role-based access  
- 💸 Money transfer with secure transaction processing  
- 💱 Currency conversion for international transactions  
- 🛍️ In-app shopping services with exclusive offers and a wide range of items  
- 📧 Secure communication via email notifications  
- 🌍 Multi-currency support  
- 🔐 Identity protection features (tax number and ID verification)  
- 🖥️ IP address tracking for secure logins  

### Project Structure
- **Controllers**: Manage HTTP requests  
- **DTO (Data Transfer Objects)**: Handle data transfer between layers  
- **Entities**: Database entities (e.g., Account, User)  
- **Repositories**: Data access layer  
- **Security**: Configuration for authentication and authorization  
- **Services**: Core business logic  
- **Utils**: Utility classes for common functionalities  

### Getting Started 🚀
#### Prerequisites
- Java 11 or higher  
- Maven 3.x  
- MySQL

#### Installation
```bash
git clone https://github.com/MN10101/Banking-System-Backend.git
```
1. Import the project into your IDE (e.g., IntelliJ, Eclipse)  
2. Create a MySQL database using `nexgindb.sql`  
3. Update `application.properties` with your DB credentials  
4. Run the project:
```bash
mvn clean install
mvn spring-boot:run
```

#### Running Tests 🧪
```bash
mvn test
```

#### License
MIT License - [View Backend License](https://github.com/MN10101/Banking-System-Backend?tab=MIT-1-ov-file)

---

# 💻 Frontend - Banking System App

Built with React.js

### Features
- 🧩 Reusable components for modular development  
- 🖼 Assets management for images and static files  
- 🎨 Custom styling via dedicated CSS folders  
- 🌐 API integration handled via `api.js`  

### Project Structure
```
src/
├── api.js               # Handles API requests and responses
├── App.css              # Styles for the main App component
├── App.js               # Main entry point of the application
├── App.test.js          # Test cases for the App
├── assets/              # Static assets (images, icons, etc.)
├── components/          # Reusable React components
├── index.css            # Global CSS styles
├── index.js             # Application entry file
├── reportWebVitals.js   # Performance monitoring
├── setupTests.js        # Configuration for tests
```

### Installation
```bash
git clone https://github.com/MN10101/Banking-System-Frontend.git
cd banking-system
npm install
npm start
```

App runs at `http://localhost:3000`

### Available Scripts
- `npm start` - Run app in dev mode  
- `npm test` - Launch test runner  
- `npm run build` - Build app for production  

#### License
MIT License - [View Frontend License](https://github.com/MN10101/Banking-System-Frontend?tab=MIT-1-ov-file)

---

📧 **Contact**: mn.de@outlook.com

---
https://github.com/user-attachments/assets/8c7daa72-876d-478d-8481-f6ed37f96f4a
