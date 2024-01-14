package com.example.restapi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.PathParam;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class RecordDAO {

    private static RecordDAO instance;
    private static final String URL = "jdbc:mysql://localhost:3306/diabetes";
    private static final String USER = "root";
    private static final String PASSWORD = "mysql";
    private Connection connection; // Declare a Connection instance

    // JDBC SQL statements for CRUD operations
    private static final String SELECT_ALL_PATIENTS = "SELECT * FROM Patients";
    private static final String ADD_PATIENT = "INSERT INTO Patients (name, surname) VALUES (?, ?)";
    private static final String UPDATE_PATIENT = "UPDATE Patients SET name = ?, surname = ? WHERE patient_id = ?";
    private static final String DELETE_PATIENT = "DELETE FROM Patients WHERE patient_id = ?";
    private static final String INSERT_DAILY_RECORD = "INSERT INTO DailyRecord ( glucoseLevel, carbIntake, medicationDose, entryDate, patient_id) VALUES ( ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_RECORDS = "SELECT * FROM DailyRecord WHERE patient_id = ?";
    private static final String DELETE_RECORD = "DELETE FROM DailyRecord WHERE id = ?";
    private static final String SELECT_ALL_RECORDS_WITHIN_PERIOD = "SELECT * FROM DailyRecord WHERE patient_id = ? AND entryDate BETWEEN ? AND ?";
    private static final String GET_AVERAGE_VALUES = 
            "SELECT AVG(glucoseLevel) AS averageGlucose, AVG(carbIntake) AS averageCarbIntake FROM DailyRecord WHERE patient_id = ?";
    private static final String GET_AVERAGE_VALUES_ALL = "SELECT AVG(glucoseLevel) AS averageGlucose, AVG(carbIntake) AS averageCarbIntake FROM DailyRecord WHERE patient_id = ?";
    private static final String SELECT_RECORDS_WITHIN_PERIOD = 
            "SELECT entryDate, glucoseLevel, carbIntake FROM DailyRecord WHERE patient_id = ?";
    // Private constructor to prevent instantiation from outside
    private RecordDAO() {
        try {
            // Explicitly load the JDBC driver (optional in JDBC 4.0+)
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the database connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
        }
    }

    public static RecordDAO getInstance() {
        if (instance == null) {
            instance = new RecordDAO(); // Call the private constructor
        }
        return instance;
    }
    
    public boolean addPatient(Patient patient) {
    	 try (PreparedStatement statement = connection.prepareStatement(ADD_PATIENT, PreparedStatement.RETURN_GENERATED_KEYS)) {
             
             statement.setString(1, patient.getName());
             statement.setString(2, patient.getSurname());
             
             // Assuming id is auto-incremented

             int affectedRows = statement.executeUpdate();

             return affectedRows > 0;

         } catch (SQLException e) {
             e.printStackTrace(); // Handle the exception according to your application's error handling strategy
             // Return a special value (e.g., -1) to indicate failure
         }
    	 return false;
    }
    
    public boolean updatePatient(int id, Patient updatedPatient) {
    	 try (PreparedStatement statement = connection.prepareStatement(UPDATE_PATIENT)) {

             statement.setString(1, updatedPatient.getName());
             statement.setString(2, updatedPatient.getSurname());
             statement.setInt(3, id);

             int affectedRows = statement.executeUpdate();

             return affectedRows > 0; // Return true if at least one row was updated
         } catch (SQLException e) {
             e.printStackTrace(); // Handle the exception according to your application's error handling strategy
             // Return false to indicate failure
         }
    	 return false;
    }
    
    public boolean deleteRecord(long id) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_RECORD)) {
            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace(); 
            // Handle the exception according to your application's error handling strategy
        }
        return false;
    }
    public boolean deleteUserById(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace(); 
            // Handle the exception according to your application's error handling strategy
        }
        return false;
    }

    
    public boolean deletePatient(int id) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_PATIENT)) {
            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace(); 
            // Handle the exception according to your application's error handling strategy
        }
        return false;
    }
    
    
    public boolean addDailyRecord(Record record) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_DAILY_RECORD, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, record.getGlucoseLevel());
            statement.setInt(2, record.getCarbIntake());
            statement.setString(3, record.getMedicationDose());
            statement.setString(4, record.getEntryDate());
            statement.setInt(5, record.getPatientId());
            // Assuming id is auto-incremented

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

          

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
            // Return a special value (e.g., -1) to indicate failure
        } 
        return false;
    }
    public Record getRecordById(long id) {
        Record record = null;

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM DailyRecord WHERE id = ?")) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    record = new Record();
                    record.setId(id);
                    record.setGlucoseLevel(resultSet.getInt("glucoseLevel"));
                    record.setCarbIntake(resultSet.getInt("carbIntake"));
                    record.setMedicationDose(resultSet.getString("medicationDose"));
                    record.setEntryDate(resultSet.getDate("entryDate").toString());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return null or throw an exception if needed
        }

        return record;
    }
    
    public User getUserById(int id) {
        User user = null;

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setId(id);
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setRole(resultSet.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return null or throw an exception if needed
        }

        return user;
    }
    
    public Patient getPatientById(int id) {
        Patient patient = null;

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Patients WHERE patient_id = ?")) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                	patient = new Patient();
                	patient.setPatientId(id);
                	patient.setName(resultSet.getString("name"));
                	patient.setSurname(resultSet.getString("surname"));
                   
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return null or throw an exception if needed
        }

        return patient;
    }
    
    public boolean updateRecord(long id, Record updatedRecord) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE DailyRecord SET glucoseLevel=?, carbIntake=?, medicationDose=?, entryDate=? WHERE id=?")) {

            statement.setInt(1, updatedRecord.getGlucoseLevel());
            statement.setInt(2, updatedRecord.getCarbIntake());
            statement.setString(3, updatedRecord.getMedicationDose());
            statement.setString(4, updatedRecord.getEntryDate());
            statement.setLong(5, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
             // Return false to indicate failure
        }
        return false;
    }
   
    public List<Record> getRecordsWithinPeriod(int patientId, String startDate, String endDate) {
        List<Record> records = new ArrayList<>();

        try {
            StringBuilder queryBuilder = new StringBuilder(SELECT_RECORDS_WITHIN_PERIOD);
            List<Object> parameters = new ArrayList<>();
            parameters.add(patientId);

            if (startDate != null && !startDate.trim().isEmpty() || endDate != null && !endDate.trim().isEmpty()) {
                if (startDate != null && !startDate.trim().isEmpty()) {
                    queryBuilder.append(" AND entryDate >= ?");
                    parameters.add(startDate);
                }

                if (endDate != null && !endDate.trim().isEmpty()) {
                    queryBuilder.append(" AND entryDate <= ?");
                    parameters.add(endDate);
                }
            }
            queryBuilder.append(" ORDER BY entryDate DESC");

            try (PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 1;
                for (Object parameter : parameters) {
                    statement.setObject(parameterIndex++, parameter);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Record record = new Record();
                        record.setEntryDate(resultSet.getDate("entryDate").toString());
                        record.setGlucoseLevel(resultSet.getInt("glucoseLevel"));
                        record.setCarbIntake(resultSet.getInt("carbIntake"));
                        records.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle exceptions appropriately
        }

        return records;
    }

    
    public List<Record> getAllRecordsWithinTheSpecifiedPeriod(int patientID, String startDate, String endDate) {
        List<Record> records = new ArrayList<>();


        try {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL_RECORDS);
            List<Object> parameters = new ArrayList<>();
            parameters.add(patientID);

            if (startDate != null && !startDate.trim().isEmpty() || endDate != null && !endDate.trim().isEmpty()) {

            	
                if (startDate != null && !startDate.trim().isEmpty()) {
                    queryBuilder.append(" AND entryDate >= ?");
                    parameters.add(startDate);
                }

                if (endDate != null && !endDate.trim().isEmpty()) {
                    queryBuilder.append(" AND entryDate <= ?");
                    parameters.add(endDate);
                }
            }
            queryBuilder.append(" ORDER BY entryDate DESC");
            try (PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 1;
                for (Object parameter : parameters) {
                    statement.setObject(parameterIndex++, parameter);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Record record = new Record();
                        record.setId((long) resultSet.getInt("id"));
                        record.setGlucoseLevel(resultSet.getInt("glucoseLevel"));
                        record.setCarbIntake(resultSet.getInt("carbIntake"));
                        record.setMedicationDose(resultSet.getString("medicationDose"));
                        record.setEntryDate(resultSet.getDate("entryDate").toString());
                        record.setPatientId(resultSet.getInt("patient_id"));
                        records.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return an empty list or throw an exception if needed
        }

        return records;
    }
    public List<Record> getAllRecords(int patientId) {
        List<Record> records = new ArrayList<>();

        try {
            // Execute the query with patient_id filtering
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_RECORDS)) {
                statement.setInt(1, patientId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Record record = new Record();
                        record.setId((long) resultSet.getInt("id"));
                        record.setGlucoseLevel(resultSet.getInt("glucoseLevel"));
                        record.setCarbIntake(resultSet.getInt("carbIntake"));
                        record.setMedicationDose(resultSet.getString("medicationDose"));
                        record.setEntryDate(resultSet.getDate("entryDate").toString());
                        record.setPatientId(resultSet.getInt("patient_id"));

                        records.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return an empty list or throw an exception if needed
        }

        return records;
    }
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try {
            // Execute the query to retrieve all users
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        User user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setUsername(resultSet.getString("username"));
                        user.setRole(resultSet.getString("role"));

                        users.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return an empty list or throw an exception if needed
        }

        return users;
    }


    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(SELECT_ALL_PATIENTS);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Patient patient = new Patient();
                    patient.setPatientId((long) resultSet.getInt("patient_id"));
                    patient.setName(resultSet.getString("name"));
                    patient.setSurname(resultSet.getString("surname"));

                    patients.add(patient);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions and return an empty list or throw an exception if needed
        }

        return patients;
    }


    public Map<String, Double> getAverageValues(int id, String startDate, String endDate) {
        Map<String, Double> averageValues = new HashMap<>();

        try {
            StringBuilder queryBuilder = new StringBuilder(GET_AVERAGE_VALUES);
            List<Object> parameters = new ArrayList<>();
            parameters.add(id);

            if (startDate != null && !startDate.trim().isEmpty() || endDate != null && !endDate.trim().isEmpty()) {
                if (startDate != null && !startDate.trim().isEmpty()) {
                    queryBuilder.append(" AND entryDate >= ?");
                    parameters.add(startDate);
                }

                if (endDate != null && !endDate.trim().isEmpty()) {
                    queryBuilder.append(" AND entryDate <= ?");
                    parameters.add(endDate);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 1;
                for (Object parameter : parameters) {
                    statement.setObject(parameterIndex++, parameter);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        double averageGlucose = resultSet.getDouble("averageGlucose");
                        double averageCarbIntake = resultSet.getDouble("averageCarbIntake");

                        averageValues.put("averageGlucose", averageGlucose);
                        averageValues.put("averageCarbIntake", averageCarbIntake);
                    }
                    return averageValues;
                }
            }
        } catch (SQLException e) {
	        if (e instanceof SQLIntegrityConstraintViolationException) {
	            // Unique constraint violation (duplicate username)
	            // Handle this case and return an error message or throw a custom exception
	            e.printStackTrace(); // For demonstration, print the stack trace
	            throw new RuntimeException("Username already exists. Please choose a different username.");
	        } else {
	            // Handle other SQL exceptions according to your error handling strategy
	            e.printStackTrace();
	            throw new RuntimeException("Error adding user. Please try again.");
	        }
	    }

        
    }

public boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE BINARY username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                   String hashedPassword = resultSet.getString("password");
                   
                   boolean passwordSame= BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
                   
                   return passwordSame;
                }
                return false;
            }
        } catch (SQLException e) {
	        if (e instanceof SQLIntegrityConstraintViolationException) {
	            // Unique constraint violation (duplicate username)
	            // Handle this case and return an error message or throw a custom exception
	            e.printStackTrace(); // For demonstration, print the stack trace
	            throw new RuntimeException("Authentication failed check your credentials");
	        } else {
	            // Handle other SQL exceptions according to your error handling strategy
	            e.printStackTrace();
	            throw new RuntimeException("Error adding user. Please try again.");
	        }
	    }
        
    }

	public boolean addUser(User user) {
		try {
			String passwordHashed = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
			String query = "INSERT INTO users (name, surname, username, password, role) VALUES (?,?,?,?,?)";
	       PreparedStatement statement = connection.prepareStatement(query);
	        	statement.setString(1, user.getName());
	       statement.setString(2, user.getSurname());
	       statement.setString(3, user.getUsername());
	       statement.setString(4, passwordHashed);
	       statement.setString(5, user.getRole());
	       
	       int affectedRows = statement.executeUpdate();

           return affectedRows > 0;

	           
			
		} catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
            // Return a special value (e.g., -1) to indicate failure
        } 
        return false;
	}

	public String getUserRole(String username) {
	    

	    try {
	        String query = "SELECT role FROM users WHERE username = ?";
	        PreparedStatement statement = connection.prepareStatement(query);
	        statement.setString(1, username);
	       

	        try (ResultSet resultSet = statement.executeQuery()) {
	            if (resultSet.next()) {
	                String role = resultSet.getString("role");
	                return role;
	            } else {
	                return null;
	            }
	        }
	    } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
            // Return a special value (e.g., -1) to indicate failure
        }
		return null; 
        
	    } // Return null if the user is not found or an error occurs
	


       

    // Close the connection when the application is shutting down
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   
}
    
