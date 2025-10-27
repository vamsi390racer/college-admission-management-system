package com.elevatelabs.admission;

import java.sql.*;
import java.util.Scanner;

public class AdmissionManager {

    // --- JDBC Configuration ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CollegeAdmissions";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "tiger";
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        AdmissionManager manager = new AdmissionManager();
        manager.run();
    }
    public void run() {
        System.out.println("--- College Admission Management System ---");
        
        // Test database connection immediately
        if (getConnection() == null) {
            System.err.println("FATAL: Database connection failed. Please check your JDBC credentials.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an Option:");
            System.out.println("1. Register New Student (Mini Guide Step b)");
            System.out.println("2. Calculate Merit Score & Apply (Mini Guide Step c)");
            System.out.println("3. Admin Approval/Rejection (Mini Guide Step d)");
            System.out.println("4. View All Applications");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        registerStudent();
                        break;
                    case 2:
                        applyForCourse();
                        break;
                    case 3:
                        adminDecision();
                        break;
                    case 4:
                        viewAllApplications();
                        break;
                    case 5:
                        running = false;
                        System.out.println("Exiting application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number.");
            }
        }
        scanner.close();
    }
    
    private Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
            return null;
        }
    }
    private void registerStudent() {
        System.out.println("\n--- Student Registration ---");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();
        
        // Input Scores
        int mathScore = readScore("Math Score (0-100): ");
        int physicsScore = readScore("Physics Score (0-100): ");
        int chemistryScore = readScore("Chemistry Score (0-100): ");

        String sql = "INSERT INTO Students (first_name, last_name, email, phone_number, score_math, score_physics, score_chemistry) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setInt(5, mathScore);
            pstmt.setInt(6, physicsScore);
            pstmt.setInt(7, chemistryScore);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("SUCCESS: Student " + firstName + " registered successfully.");
            } else {
                System.err.println("ERROR: Registration failed.");
            }
            
        } catch (SQLException e) {
            // Check for duplicate email error (SQL State 23000)
            if (e.getSQLState().equals("23000")) {
                System.err.println("ERROR: Email address already exists. Please use a unique email.");
            } else {
                System.err.println("Database Error during registration: " + e.getMessage());
            }
        }
    }
    
    private int readScore(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int score = Integer.parseInt(scanner.nextLine());
                if (score >= 0 && score <= 100) {
                    return score;
                } else {
                    System.err.println("Score must be between 0 and 100.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid number for the score.");
            }
        }
    }
    private void applyForCourse() {
        System.out.println("\n--- Apply for Course ---");
        System.out.print("Enter Student ID: ");
        int studentId;
        try {
            studentId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid Student ID.");
            return;
        }

        // 1. Get student scores
        String studentSql = "SELECT score_math, score_physics, score_chemistry FROM Students WHERE student_id = ?";
        double meritScore = 0.0;
        
        try (Connection conn = getConnection();
             PreparedStatement studentPstmt = conn.prepareStatement(studentSql)) {
            
            studentPstmt.setInt(1, studentId);
            ResultSet rs = studentPstmt.executeQuery();
            
            if (rs.next()) {
                int math = rs.getInt("score_math");
                int physics = rs.getInt("score_physics");
                int chemistry = rs.getInt("score_chemistry");
                
                // Simple Merit Calculation: Average of three subject scores
                meritScore = (double) (math + physics + chemistry) / 3.0;
                System.out.printf("Calculated Merit Score: %.2f\n", meritScore);
                
                // 2. Display available courses
                System.out.println("\nAvailable Courses:");
                String courseSql = "SELECT course_id, course_name, required_score, max_seats, current_enrolled FROM Courses";
                Statement stmt = conn.createStatement();
                ResultSet courseRs = stmt.executeQuery(courseSql);
                
                while (courseRs.next()) {
                    System.out.printf("ID: %d | Course: %-25s | Min Score: %d | Seats Available: %d/%d\n",
                        courseRs.getInt("course_id"),
                        courseRs.getString("course_name"),
                        courseRs.getInt("required_score"),
                        courseRs.getInt("max_seats") - courseRs.getInt("current_enrolled"),
                        courseRs.getInt("max_seats"));
                }
                
                // 3. Select course and submit application
                System.out.print("Enter Course ID to apply: ");
                int courseId = Integer.parseInt(scanner.nextLine());
                
                String applySql = "INSERT INTO Applications (student_id, course_id, merit_score) VALUES (?, ?, ?)";
                try (PreparedStatement applyPstmt = conn.prepareStatement(applySql)) {
                    applyPstmt.setInt(1, studentId);
                    applyPstmt.setInt(2, courseId);
                    applyPstmt.setDouble(3, meritScore);
                    applyPstmt.executeUpdate();
                    System.out.println("SUCCESS: Application submitted with merit score.");
                } catch (SQLException e) {
                    if (e.getSQLState().equals("23000")) {
                        System.err.println("ERROR: Student has already applied for this course.");
                    } else {
                        System.err.println("Database Error during application: " + e.getMessage());
                    }
                }
                
            } else {
                System.err.println("ERROR: Student with ID " + studentId + " not found.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid input for Course ID.");
        }
    }
    private void adminDecision() {
        System.out.println("\n--- Admin Admission Approval ---");
        System.out.print("Enter Course ID for review: ");
        int courseId;
        try {
            courseId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid Course ID.");
            return;
        }

        String courseDetailsSql = "SELECT course_name, max_seats, required_score, current_enrolled FROM Courses WHERE course_id = ?";
        String approveSql = "UPDATE Applications SET status = 'APPROVED' WHERE application_id = ?";
        String enrollSql = "UPDATE Courses SET current_enrolled = current_enrolled + 1 WHERE course_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement coursePstmt = conn.prepareStatement(courseDetailsSql)) {
            
            coursePstmt.setInt(1, courseId);
            ResultSet courseRs = coursePstmt.executeQuery();

            if (!courseRs.next()) {
                System.err.println("Course ID not found.");
                return;
            }

            String courseName = courseRs.getString("course_name");
            int maxSeats = courseRs.getInt("max_seats");
            int minScore = courseRs.getInt("required_score");
            int enrolledBefore = courseRs.getInt("current_enrolled");
            int seatsAvailable = maxSeats - enrolledBefore;

            System.out.printf("\nReviewing %s (Seats: %d/%d | Min Merit Score: %d)\n", 
                              courseName, enrolledBefore, maxSeats, minScore);
            System.out.println("Available seats to fill: " + seatsAvailable);

            if (seatsAvailable <= 0) {
                System.out.println("No seats available for this course.");
                return;
            }

            // Select pending applicants ordered by merit score (highest first) and meeting the min score
            String applicantsSql = "SELECT application_id, merit_score, student_id FROM Applications " +
                                   "WHERE course_id = ? AND status = 'PENDING' AND merit_score >= ? " +
                                   "ORDER BY merit_score DESC LIMIT ?";
            
            try (PreparedStatement applicantsPstmt = conn.prepareStatement(applicantsSql);
                 PreparedStatement approvePstmt = conn.prepareStatement(approveSql);
                 PreparedStatement enrollPstmt = conn.prepareStatement(enrollSql)) {

                applicantsPstmt.setInt(1, courseId);
                applicantsPstmt.setInt(2, minScore);
                applicantsPstmt.setInt(3, seatsAvailable);
                ResultSet applicantsRs = applicantsPstmt.executeQuery();

                int approvedCount = 0;
                while (applicantsRs.next()) {
                    int appId = applicantsRs.getInt("application_id");
                    double merit = applicantsRs.getDouble("merit_score");
                    
                    // Approve the application
                    approvePstmt.setInt(1, appId);
                    approvePstmt.executeUpdate();
                    
                    // Increment enrolled count
                    enrollPstmt.setInt(1, courseId);
                    enrollPstmt.executeUpdate();

                    System.out.printf("APPROVED: App ID %d, Merit: %.2f\n", appId, merit);
                    approvedCount++;
                }

                System.out.println("\n--- Approval Summary ---");
                System.out.printf("Total new approvals for %s: %d\n", courseName, approvedCount);
                
            } catch (SQLException e) {
                System.err.println("Database Error during approval process: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private void viewAllApplications() {
        System.out.println("\n--- All Applications ---");
        String sql = "SELECT a.application_id, s.first_name, s.last_name, c.course_name, a.merit_score, a.status " +
                     "FROM Applications a " +
                     "JOIN Students s ON a.student_id = s.student_id " +
                     "JOIN Courses c ON a.course_id = c.course_id " +
                     "ORDER BY a.application_id DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No applications found.");
                return;
            }

            System.out.printf("%-15s | %-20s | %-25s | %-10s | %-10s\n", 
                              "App ID", "Student Name", "Course", "Merit", "Status");
            System.out.println("-----------------------------------------------------------------------------------");

            while (rs.next()) {
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                System.out.printf("%-15d | %-20s | %-25s | %-10.2f | %-10s\n", 
                                  rs.getInt("application_id"),
                                  name,
                                  rs.getString("course_name"),
                                  rs.getDouble("merit_score"),
                                  rs.getString("status"));
            }

        } catch (SQLException e) {
            System.err.println("Database Error viewing applications: " + e.getMessage());
        }
    }
}
