package com.example.restapi.dao;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

@Path("/records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecordResource {

    private RecordDAO dao = RecordDAO.getInstance();
    static String key = "asdkjgaidgubairgqgiu";
    static Algorithm algorithm = Algorithm.HMAC256(key);
    
    public static DecodedJWT verifyToken(String token) {
    	JWTVerifier verifier = JWT.require(algorithm).build();
    	return verifier.verify(token);
    }
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(User user) {
        // Validate the provided credentials (this is just a basic example)
    	if(dao.authenticateUser(user.getUsername(), user.getPassword())) {
    		String token = JWT.create()
    				.withClaim("username", user.getUsername())
    				.withClaim("password", user.getPassword())
    				.sign(algorithm);
    		
    		 JsonObject jsonObject = Json.createObjectBuilder()
    	                .add("token", token)
    	                .build();
    		return Response.ok(jsonObject.toString()).build();
    		
    	}
    	
    	return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    @POST
    @RolesAllowed("ADMIN")
    @Path("/add-user")
    public Response addUser(User user) {
    	if(dao.addUser(user)) {
    		return Response.ok().entity("User added successfully").build();
    	}
    	return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request or user data").build();
    	}
    
    
    @GET
    @RolesAllowed({"ADMIN", "PHYSICIAN"})
    public JsonObject getAllPatients() {
    	
        List<Patient> patients = dao.getAllPatients();
if(patients != null) {
        JsonArrayBuilder patientArrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder linkBuilder = Json.createObjectBuilder();
        JsonObjectBuilder linksBuilder = Json.createObjectBuilder()
                .add("Add new patient", Json.createObjectBuilder()
                        .add("href", "/MyProject/api/records/add-form-patient")
                        .add("method", "GET"))
                .add("View Users", Json.createObjectBuilder()
                                .add("href", "/MyProject/api/records/users")
                                .add("method", "GET")
                        );

        
        linkBuilder.add("_links",linksBuilder);
        
        JsonObjectBuilder headerBuilder = Json.createObjectBuilder();

       
        headerBuilder.add("header patientId","Patient Id" );
        headerBuilder.add("header name","Name" );
        headerBuilder.add("header surname","Surname" );
       
       
        
        
        
        
        patientArrayBuilder.add(linkBuilder);
        patientArrayBuilder.add(headerBuilder);
        patients.forEach(patient -> {
            JsonObjectBuilder patientJson = Json.createObjectBuilder()
                    .add("patientId", patient.getPatientId())
                    .add("name", patient.getName())
                    .add("surname", patient.getSurname())
                    .add("_links", Json.createObjectBuilder()
                    .add("View records", Json.createObjectBuilder()
                            .add("href", "/MyProject/api/records/date-form/"+patient.getPatientId())
                            .add("method", "GET"))
                    .add("Edit Patient", Json.createObjectBuilder()
                    		.add("href", "/MyProject/api/records/update-form-patient/"+patient.getPatientId())
                            .add("method", "GET"))
                    		);
      

            patientArrayBuilder.add(patientJson);
        });

        // Build the JSON object with hypermedia links
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder()
                .add("status", "success")
                .add("data", patientArrayBuilder);
               

        return responseBuilder.build();
    	}
            // Catch the exception thrown by addUser method
return (JsonObject) Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    
    @GET
    @RolesAllowed("ADMIN")
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getAllUsers() {
        List<User> users = dao.getAllUsers();

        if (users != null) {
            JsonArrayBuilder userArrayBuilder = Json.createArrayBuilder();
            JsonObjectBuilder linkBuilder = Json.createObjectBuilder();
            JsonObjectBuilder linksBuilder = Json.createObjectBuilder()
                    .add("Add new user", Json.createObjectBuilder()
                            .add("href", "/MyProject/api/records/add-form-user")
                            .add("method", "GET"));

            linkBuilder.add("_links", linksBuilder);

            JsonObjectBuilder headerBuilder = Json.createObjectBuilder();
            headerBuilder.add("header userId", "User Id");
            headerBuilder.add("header name", "Name");
            headerBuilder.add("header surname", "Surname");
            headerBuilder.add("header username", "Username");
            headerBuilder.add("header role", "Role");

            userArrayBuilder.add(linkBuilder);
            userArrayBuilder.add(headerBuilder);

            users.forEach(user -> {
                JsonObjectBuilder userJson = Json.createObjectBuilder()
                        .add("userId", user.getId())
                        .add("name", user.getName())
                        .add("surname", user.getSurname())
                        .add("username", user.getUsername())
                        .add("role", user.getRole())
                        .add("_links", Json.createObjectBuilder()
                                .add("Delete", Json.createObjectBuilder()
                                        .add("href", "/MyProject/api/records/user-delete/"+user.getId())
                                        .add("method", "DELETE"))
                                
                                		);
                                

                userArrayBuilder.add(userJson);
            });

            // Build the JSON object with hypermedia links
            JsonObjectBuilder responseBuilder = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("data", userArrayBuilder);

            return responseBuilder.build();
        }

        // Catch the exception or handle the case where getting users failed
        return (JsonObject) Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("An unexpected error occurred")
                .build();
    }

    
    @GET
    @RolesAllowed("ADMIN")
    @Path("/add-form-user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddUserForm() {
        // Provide HTML form for creating a user
        String formHtml = String.format(
        		"<form id='userForm' action='api/records/add-user' method='POST' data-method='POST' onsubmit='submitForm()'>" +
                        "<label for='name'>Name:</label>" +
                        "<input type='text' id='name' name='name' required><br>" +
                        "<label for='surname'>Surname:</label>" +
                        "<input type='text' id='surname' name='surname' required><br>" +
                        "<label for='username'>Username:</label>" +
                        "<input type='text' id='username' name='username' required><br>" +
                        "<label for='password'>Password:</label>" +
                        "<input type='password' id='password' name='password' required><br>" +
                        "<label for='role'>Role:</label>" +
                        "<select id='role' name='role' required>" +
                        "<option value='ADMIN'>ADMIN</option>" +
                        "<option value='PHYSICIANS'>PHYSICIANS</option>" +
                        "</select><br>" +
                        "<input type='submit' value='Create User'>" +
                        "</form>"
        );

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("formHtml", formHtml)
                .build();

        return Response.ok(jsonObject.toString()).build();
    }
    
    @GET
    @Path("/date-form/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDateForm(@PathParam("id") int patientId) {
        // Provide HTML form for getting data with date
        String formHtml = String.format(
                "<form id='dateForm' action='api/records/patient-records/%d' method='GET' data-method='GET'>" +
                        "<label for='startDate'>Start Date:</label>" +
                        "<input type='date' id='startDate' name='startDate'><br>" +
                        "<label for='endDate'>End Date:</label>" +
                        "<input type='date' id='endDate' name='endDate'><br>" +
                        "<input type='submit' value='get patient records'>" +
                        "</form>", patientId
        );

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("formHtml", formHtml)
                .build();

        return Response.ok(jsonObject.toString()).build();
    }
    

    @GET
    @RolesAllowed({"ADMIN", "PHYSICIAN"})
    @Path("/patient-records/{id}")
    public Response getAllRecordsNoFilter(@PathParam("id") int patientId, @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
    	
    	List<Record> records = dao.getAllRecordsWithinTheSpecifiedPeriod(patientId, startDate,endDate);
    	if(records != null) {
        Response response = buildResponse(records, patientId, startDate,endDate);
        return response;
    	}
    	return Response.status(Response.Status.NOT_FOUND).entity("An unexpected error occurred").build();
    }

    private Response buildResponse(List<Record> records, int patientId, String startDate, String endDate) {
        
        List<Record> chart = dao.getRecordsWithinPeriod(patientId, startDate, endDate);
        Map<String, Double> averages = dao.getAverageValues(patientId, startDate, endDate);
        Double averageGlucose = averages.get("averageGlucose");
        Double averageCarbIntake = averages.get("averageCarbIntake");

        JsonArrayBuilder recordsArrayBuilder = Json.createArrayBuilder();

        JsonObjectBuilder linkBuilder = Json.createObjectBuilder();

        linkBuilder.add("Average Glucose level", averageGlucose);
        linkBuilder.add("Average Carb Intake", averageCarbIntake);

        JsonObjectBuilder linksBuilder = Json.createObjectBuilder()
                .add("Add new record", Json.createObjectBuilder()
                        .add("href", "/MyProject/api/records/add-form-record/" + patientId)
                        .add("method", "GET"));

        linkBuilder.add("_links", linksBuilder);
        recordsArrayBuilder.add(linkBuilder);

        JsonArrayBuilder chartArrayBuilder = Json.createArrayBuilder();
        chart.forEach(record -> {
            JsonObjectBuilder chartRecordBuilder = Json.createObjectBuilder()
                    .add("entryDate", record.getEntryDate())
                    .add("glucoseLevel", record.getGlucoseLevel())
                    .add("carbIntake", record.getCarbIntake());

            chartArrayBuilder.add(chartRecordBuilder);
        });

        // Add chart data to the response
        JsonObjectBuilder chartDataBuilder = Json.createObjectBuilder()
                .add("chartData", chartArrayBuilder);

        recordsArrayBuilder.add(chartDataBuilder);
        
        JsonObjectBuilder headerBuilder = Json.createObjectBuilder();

        headerBuilder.add("header ID","ID" );
        headerBuilder.add("header Glucose level","Glucose level" );
        headerBuilder.add("header Carb intake","Carb intake" );
        headerBuilder.add("header medication dose","medication dose" );
        headerBuilder.add("header entry date","entry date" );
        headerBuilder.add("header Patient id","Patient id" );
        

        

       
        recordsArrayBuilder.add(headerBuilder);
        
        
        // Use the same list of records for both records and chart data
       

        
        
        // Add records data to the response
        records.forEach(record -> {
            JsonObjectBuilder recordBuilder = Json.createObjectBuilder()
                    .add("ID", record.getId())
                    .add("Glucose level", record.getGlucoseLevel())
                    .add("Carb intake", record.getCarbIntake())
                    .add("medication dose", record.getMedicationDose())
                    .add("entry date", record.getEntryDate())
                    .add("Patient id", record.getPatientId())
                    .add("_links", Json.createObjectBuilder()
                            .add("View", Json.createObjectBuilder()
                                    .add("href", "/MyProject/api/records/" + record.getId())
                                    .add("method", "GET")));

            recordsArrayBuilder.add(recordBuilder);
        });

        // Build the JSON object with hypermedia links and chart data
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder()
                .add("status", "success")
                .add("data", recordsArrayBuilder);

        // Return the Response with OK status and JSON media type
        return Response.ok(responseBuilder.build())
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/add-form-record/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddRecordForm(@PathParam("id") int patientId) {
        // Provide HTML form for adding a record
    	
    	String formHtml = String.format(
    		    "<form id='addForm' action='/MyProject/api/records/add' method='POST' data-method='POST'>" +
    		    "<label for='glucoseLevel'>Glucose Level:</label>" +
    		    "<input type='text' name='glucoseLevel'><br>" +
    		    "<label for='carbIntake'>Carb Intake:</label>" +
    		    "<input type='text' name='carbIntake'><br>" +
    		    "<label for='medicationDose'>Medication Dose:</label>" +
    		    "<input type='text' name=medicationDose><br>" +
    		    "<label for='entryDate'>Entry Date:</label>" +
    		    "<input type='text' name='entryDate' id='entryDate' disabled><br>" +
    		    "<input type='text' name='patientId' value='%d' hidden >" +
    		    "<input type='submit' value='Add Record'>" +
    		    "</form>" +
    		    "<script>" +
    		    "var today = new Date();" +
    		    "var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();" +
    		    "document.getElementById('entryDate').value = date;" +
    		    "</script>", patientId, patientId
    	
        );
    	
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("formHtml", formHtml)
                .build();

        return Response.ok(jsonObject.toString()).build();
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/add-form-patient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddPatientForm() {
        // Provide HTML form for adding a patient
        String formHtml = String.format("<form id='addPatientForm' action='/MyProject/api/records/add-patient' method='POST' data-method='POST'>" +
                "<label for='name'>Name:</label>" +
                "<input type='text' name='name'><br>" +
                "<label for='surname'>Surname:</label>" +
                "<input type='text' name='surname'><br>" +
                "<input type='submit' value='Add Patient'>" +
                "</form>");

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("formHtml", formHtml)
                .build();

        return Response.ok(jsonObject.toString()).build();
    }

    
    
    @GET
    @RolesAllowed("ADMIN")
    @Path("/update-form/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUpdateRecordForm(@PathParam("id") long id) {
        // Fetch the existing record data
        Record record = dao.getRecordById(id);

        if (record != null) {
            // Provide JSON response for updating a record with pre-filled data
        	String formHtml = String.format(
        			
        	        "<form id='updateForm' action='/MyProject/api/records/update/%d' method='PUT' data-method='PUT'>" +
        	        "<label for='id'>ID:</label>" +
        	        "<input type='text' name='id' value='%d' disabled><br>" +
        	        "<label for='glucoseLevel'>Glucose Level:</label>" +
        	        "<input type='text' name='glucoseLevel' value='%s'><br>" +
        	        "<label for='carbIntake'>Carb Intake:</label>" +
        	        "<input type='text' name='carbIntake' value='%s'><br>" +
        	        "<label for='medicationDose'>Medication Dose:</label>" +
        	        "<input type='text' name='medicationDose' value='%s'><br>" +
        	        "<label for='entryDate'>Entry Date:</label>" +
        	        "<input type='text' name='entryDate' id='entryDate' disabled><br>" +  
        	        "<input type='submit' value='Update Record'>" +
        	        "</form>" +
        	        "<form id='deleteForm' action='/MyProject/api/records/delete/%d' method='DELETE' data-method='DELETE'>" +
        	        
        	        "<input type='hidden' name='deleteId' value='%d'>" +  // Use a hidden field for delete ID
        	        "<input type='submit' value='Delete Record'>" +
        	        "</form>" +
        	        "<script>" +
        	        "var today = new Date();" +
        	        "var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();" +
        	        "document.getElementById('entryDate').value = date;" +
        	        "</script>",
        	        id, id, record.getGlucoseLevel(), record.getCarbIntake(), record.getMedicationDose(),
        	        id, id);




            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("formHtml", formHtml)
                    .build();

            return Response.ok(jsonObject.toString()).build();
        } else {
            // Handle the case where the record with the given ID is not found
        	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/update-form-patient/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUpdateClientForm(@PathParam("id") int id) {
        // Fetch the existing record data
        Patient patient = dao.getPatientById(id);

        if (patient != null) {
            // Provide JSON response for updating a record with pre-filled data
        	String formHtml = String.format(
        	        "<form id='updateForm' action='/MyProject/api/records/update-patient/%d' method='PUT' data-method='PUT'>" +
        	        "<label for='id'>ID:</label>" +
        	        "<input type='text' name='id' value='%d' disabled><br>" +
        	        "<label for='name'>Name:</label>" +
        	        "<input type='text' name='name' value='%s'><br>" +
        	        "<label for='surname'>Surname:</label>" +
        	        "<input type='text' name='surname' value='%s'><br>" + 
        	        "<input type='submit' value='Update Record'>" +
        	        "</form>" +
        	        "<form id='deleteForm' action='/MyProject/api/records/delete-patient/%d' method='DELETE' data-method='DELETE'>" +
        	        
        	        "<input type='hidden' name='deleteId' value='%d'>" +  // Use a hidden field for delete ID
        	        "<input type='submit' value='Delete Record'>" +
        	        "</form>",
  
        	        id, id, patient.getName(), patient.getSurname(),
        	        id, id);




            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("formHtml", formHtml)
                    .build();

            return Response.ok(jsonObject.toString()).build();
        } else {
            // Handle the case where the record with the given ID is not found
        	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }

    @DELETE
    @RolesAllowed("ADMIN")
    @Path("/user-delete/{id}")
   public Response deleteUser(@PathParam("id") int id) {
       if(dao.deleteUserById(id)) {
    	   
           	return Response.ok("User was deleted succesfuly").build();
           } 
           	return Response.status(Response.Status.NOT_FOUND).build();
       
    }

    @GET
    @RolesAllowed({"ADMIN", "PHYSICIAN"})
    @Path("user/{id}")
    public Response getUserById(@PathParam("id") int userId) {
        User user = dao.getUserById(userId);

        if (user != null) {
            // Manually construct the JSON object with a predefined order
            JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                    .add("header ID", "ID")
                    .add("header Name", "Name")
                    .add("header Surname", "Surname")
                    .add("header Username", "Username")
                    .add("header Role", "Role")
                    .add("id", user.getId())
                    .add("name", user.getName())
                    .add("surname", user.getSurname())
                    .add("username", user.getUsername())
                    .add("role", user.getRole());

            // Build the JSON object
            JsonObject jsonResponse = userBuilder.build();

            return Response.ok(jsonResponse).build();
        } else {
            // Handle the case where the user with the given ID is not found
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }
    }
    
    @GET
    @RolesAllowed({"ADMIN", "PHYSICIAN"})
    @Path("/{id}")
    public Response getRecordById(@PathParam("id") long id) {
        Record record = dao.getRecordById(id);

        if (record != null) {
            // Manually construct the JSON object with a predefined order

            JsonObjectBuilder recordBuilder = Json.createObjectBuilder()
           .add("header ID","ID" )
        	.add("header Glucose level","Glucose level" )
        	.add("header Carb intake","Carb intake" )
        	.add("header medication dose","medication dose" )
        	.add("header entry date","entry date" )
                    .add("id", record.getId())
                    .add("glucoseLevel", record.getGlucoseLevel())
                    .add("carbIntake", record.getCarbIntake())
                    .add("medicationDose", record.getMedicationDose())
                    .add("entryDate", record.getEntryDate())
                    .add("_links", Json.createObjectBuilder()
                            .add("edit", Json.createObjectBuilder()
                            		.add("href", "/MyProject/api/records/update-form/" + record.getId())
                                    .add("method", "GET"))
                    );

            // Build the JSON object
            JsonObject jsonResponse = recordBuilder.build();

            return Response.ok(jsonResponse).build();
        } else {
            // Handle the case where the record with the given ID is not found
            return Response.status(Response.Status.NOT_FOUND).entity("Record not found").build();
        }
    }

  
    @PUT
    @RolesAllowed("ADMIN")
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRecord(@PathParam("id") Long id, Record updatedRecord) {
        boolean updateSuccess = dao.updateRecord(id, updatedRecord);

        
        if (updateSuccess) {
        	return Response.ok("Record was update succesfuly").build();
        } 
        	return Response.status(Response.Status.NOT_FOUND).build();
       
        
    }
    
    @PUT
    @RolesAllowed("ADMIN")
    @Path("/update-patient/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePatient(@PathParam("id") int id, Patient updatedPatient) {
        boolean updateSuccess = dao.updatePatient(id, updatedPatient);

        if (updateSuccess) {
        	return Response.ok("Patient was update succesfuly").build();
        } 
        	return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    
    @DELETE
    @RolesAllowed("ADMIN")
    @Path("/delete/{id}")
   public Response deleteRecord(@PathParam("id") Long id) {
       if(dao.deleteRecord(id)) {
    	   
           	return Response.ok("Record was deleted succesfuly").build();
           } 
           	return Response.status(Response.Status.NOT_FOUND).build();
       
    }
    
    @DELETE
    @RolesAllowed("ADMIN")
    @Path("/delete-patient/{id}")
   public Response deletePatient(@PathParam("id") int id) {
       if(dao.deletePatient(id)) {

       return Response.ok("Record was deleted succesfuly").build();
    } 
    	return Response.status(Response.Status.NOT_FOUND).build();
    }
@POST
@RolesAllowed("ADMIN")
@Path("/add-patient")
public Response addPatient(Patient patient) {
	if(dao.addPatient(patient)) {
		return Response.ok("Patient was added succesfuly").build();
	}
	return Response.status(Response.Status.NOT_FOUND).build();
   
}


    @POST
    @RolesAllowed("ADMIN")
    @Path("/add")
    public Response addRecord(Record record) {
    	
        if(dao.addDailyRecord(record)){
        	return Response.ok("Record was added succesfuly").build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        
    }
    

}
