This is a console-based application developed in Java to automate and manage the core processes of college admissions. The system is designed to handle student registration, calculate merit scores based on subject performance, and provide administrative tools to approve applicants according to their merit and available course capacity.

The project relies on JDBC (Java Database Connectivity) to establish a connection between the core Java application logic and a MySQL database for persistent data storage. Maven is used as the build tool for dependency and project management.

🚀 Technologies Used

The following tools and technologies are essential for running this project:

Core Language: Java (JDK 17 or higher)

Build Tool: Maven (Latest stable version)

Database: MySQL Server (A running instance on the default port 3306)

Connectivity: JDBC (mysql-connector-java is managed automatically by Maven)

⚙️ Setup and Installation Guide

Follow these steps precisely to set up and run the project locally.

Step 1: Database Setup

Ensure your MySQL Server is running.

Locate the database setup script: src/main/resources/DB_Setup.sql.

Execute the entire SQL script in your MySQL client (e.g., MySQL Workbench or DBeaver). This action creates the CollegeAdmissions database and the necessary tables (Students, Courses, and Applications).

Step 2: Configure Credentials

The application needs your database login details to establish a connection.

Open the main application file: src/main/java/AdmissionManager.java.

Update the DB_USER and DB_PASSWORD variables in the code with your actual local MySQL credentials:

// Update these values in AdmissionManager.java
private static final String DB_USER = "YOUR_DB_USERNAME";       
private static final String DB_PASSWORD = "YOUR_DB_PASSWORD"; 


Step 3: Build and Run

Open your terminal or command prompt and navigate to the project's root directory (where the pom.xml is located).

Use Maven to compile the project and download the required MySQL JDBC driver:

mvn clean install


Run the console application:

mvn exec:java -Dexec.mainClass="AdmissionManager"


✅ Key Features and Logic

The application provides a command-line interface with the following functions:

Student Registration: Allows the user to input personal details and subject scores (Maths, Physics, Chemistry). This data is saved to the Students table.

Course Application: Automatically calculates the student's Merit Score (the average of their three subject scores) and saves the application record to the Applications table with a 'PENDING' status.

Administrative Approval: Simulates the final admission process by reviewing all 'PENDING' applicants for a selected course. It approves the highest-scoring applicants first until the maximum seat capacity is reached, updating the application status to 'APPROVED' and managing the seat count in the Courses table.
