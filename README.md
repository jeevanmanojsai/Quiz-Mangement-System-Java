

# Quiz Application

A Java-based quiz management system that allows students to take exams and administrators to manage questions, users, and results.

## Features

- **User Authentication**: Secure login for students and administrators with hashed passwords.
- **Role-Based Access**: Separate dashboards for students and admins.
- **Exam Management**: Create and manage quizzes with multiple-choice questions.
- **Results Tracking**: Store and view exam results and feedback.
- **Password Reset**: Admin-approved password reset requests with temporary passwords.
- **Database Integration**: Uses a relational database for data persistence.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- A relational database (e.g., MySQL, PostgreSQL) compatible with JDBC

## Project Structure

```
src/
├── com/quiz/
│   ├── Main.java              # Application entry point
│   ├── TestRunner.java        # Test runner (if applicable)
│   ├── dao/                   # Data Access Objects
│   │   ├── ExamDAO.java
│   │   ├── FeedbackDAO.java
│   │   ├── QuestionDAO.java
│   │   ├── ResultDAO.java
│   │   └── UserDAO.java
│   ├── model/                 # Data models
│   │   ├── Exam.java
│   │   ├── Feedback.java
│   │   ├── PasswordResetRequest.java
│   │   ├── Question.java
│   │   ├── Result.java
│   │   └── User.java
│   ├── ui/                    # User Interface components
│   │   ├── AdminDashboard.java
│   │   ├── ExamFrame.java
│   │   ├── LoginFrame.java
│   │   └── StudentDashboard.java
│   └── util/                  # Utility classes
│       ├── DatabaseConnection.java
│       ├── PasswordUtils.java
│       └── UIUtils.java
bin/                           # Compiled classes
config/                        # Configuration files
db/
└── schema.sql                 # Database schema
lib/                           # External libraries (JAR files)
```

## Setup

1. **Clone or Download the Project**:
   - Ensure the project directory is set up as shown in the structure above.

2. **Database Setup**:
   - Create a database using the schema provided in `db/schema.sql`.
   - Update database connection details in `src/com/quiz/util/DatabaseConnection.java` if necessary.

3. **Dependencies**:
   - Place any required JAR files (e.g., JDBC drivers) in the `lib/` directory.

## Compilation

Compile the Java source files using the following command:

```
javac -cp "lib/*" -d bin src/com/quiz/util/*.java src/com/quiz/model/*.java src/com/quiz/dao/*.java src/com/quiz/ui/*.java src/com/quiz/Main.java
```

## Running the Application

Run the application with:

```
java -cp "bin;lib/*" com.quiz.Main
```

## Usage

- **Login**: Use the login screen to access as a student or admin.
- **Student Dashboard**: Take exams and view results.
- **Admin Dashboard**: Manage users, questions, exams, and password reset requests.

## Contributing

Feel free to contribute by submitting issues or pull requests.




