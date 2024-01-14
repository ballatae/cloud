package com.example.restapi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.example.restapi.javaFiles.Patient;

import com.example.restapi.javaFiles.Record;
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
    private static final String INSERT_DAILY_RECORD = "INSERT INTO DailyRecord ( id, glucoseLevel, carbIntake, medicationDose, entryDate, patient_id) VALUES ( ?, ?, ?, ?, ?, ?)";
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
    
    public int addPatient(Patient patient) {
    	 try (PreparedStatement statement = connection.prepareStatement(ADD_PATIENT, PreparedStatement.RETURN_GENERATED_KEYS)) {
             
             statement.setString(1, patient.getName());
             statement.setString(2, patient.getSurname());
             
             // Assuming id is auto-incremented

             int affectedRows = statement.executeUpdate();

             if (affectedRows == 0) {
                 throw new SQLException("Creating record failed, no rows affected.");
             }

             try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                 if (generatedKeys.next()) {
                     // Retrieve the auto-generated key (ID)
                     int generatedId = generatedKeys.getInt(1);
                    

                     return generatedId; // Return the generated ID
                 } else {
                     throw new SQLException("Creating record failed, no ID obtained.");
                 }
             }

         } catch (SQLException e) {
             e.printStackTrace(); // Handle the exception according to your application's error handling strategy
             return -1; // Return a special value (e.g., -1) to indicate failure
         }
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
             return false; // Return false to indicate failure
         }
    }
    
    public boolean deleteRecord(long id) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_RECORD)) {
            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return false;
            } 

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
        }
		return true;
    }
    
    
    
    public long addDailyRecord(Record record) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_DAILY_RECORD, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(2, record.getGlucoseLevel());
            statement.setInt(3, record.getCarbIntake());
            statement.setString(4, record.getMedicationDose());
            statement.setString(5, record.getEntryDate());
            statement.setInt(6, record.getPatientId());
            statement.setNull(1, java.sql.Types.INTEGER); // Assuming id is auto-incremented

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Retrieve the auto-generated key (ID)
                    long generatedId = generatedKeys.getLong(1);
                   

                    return generatedId; // Return the generated ID
                } else {
                    throw new SQLException("Creating record failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error handling strategy
            return -1; // Return a special value (e.g., -1) to indicate failure
        }
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
            return false; // Return false to indicate failure
        }
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle exceptions appropriately
        }

        return averageValues;
    }

public boolean authenticateUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String storedPasswordHash = resultSet.getString("password");
                    // Compare the hashed password with the provided password
                    return storedPasswordHash.equals(password);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your error handling strategy
        }
        return false; // Return false if the user is not found or an error occurs
    }

    public String getUserRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your error handling strategy
        }
        return null; // Return null if the user is not found or an error occurs
    }
       

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
    
