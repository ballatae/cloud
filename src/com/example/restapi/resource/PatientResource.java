package com.example.restapi.resource;

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

import com.example.restapi.dao.PatientDAO;
import com.example.restapi.file.Patient;
import com.example.restapi.file.Record;

@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientResource {

    private PatientDAO dao = PatientDAO.getInstance();
    
    @GET
    public JsonObject getAllPatients() {
        List<Patient> patients = dao.getAllPatients();

        JsonArrayBuilder patientArrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder linkBuilder = Json.createObjectBuilder();
        JsonObjectBuilder linksBuilder = Json.createObjectBuilder()
                .add("Add new patient", Json.createObjectBuilder()
                        .add("href", "/MyProject/api/patients/add-form-patient")
                        .add("method", "GET"));
        
        linkBuilder.add("_links",linksBuilder);
        
        JsonObjectBuilder headerBuilder = Json.createObjectBuilder();

       
        headerBuilder.add("header patientId","Patient Id" );
        headerBuilder.add("header name","Name" );
        headerBuilder.add("surname","Surname" );
       

        patientArrayBuilder.add(linkBuilder);
        patientArrayBuilder.add(headerBuilder);
        patients.forEach(patient -> {
            JsonObjectBuilder patientJson = Json.createObjectBuilder()
                    .add("patientId", patient.getPatientId())
                    .add("name", patient.getName())
                    .add("surname", patient.getSurname())
                    .add("_links", Json.createObjectBuilder()
                    .add("View records", Json.createObjectBuilder()
                            .add("href", "/MyProject/api/patients/date-form/"+patient.getPatientId())
                            .add("method", "GET"))
                    .add("Edit Patient", Json.createObjectBuilder()
                    		.add("href", "/MyProject/api/patients/update-form-patient/"+patient.getPatientId())
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
    @GET
    @Path("/date-form/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDateForm(@PathParam("id") int patientId) {
        // Provide HTML form for getting data with date
        String formHtml = String.format(
                "<form id='dateForm' action='api/patients/patient-records/%d' method='GET' data-method='GET'>" +
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
    @Path("/patient-records/{id}")
    public Response getAllRecordsNoFilter(@PathParam("id") int patientId, @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
    	
    	List<Record> records = dao.getAllRecordsWithinTheSpecifiedPeriod(patientId, startDate,endDate);
        Response response = buildResponse(records, patientId, startDate,endDate);
        return response;
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
    @Path("/add-form-patient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddPatientForm() {
        // Provide HTML form for adding a patient
        String formHtml = String.format("<form id='addPatientForm' action='/MyProject/api/patients/add-patient' method='POST' data-method='POST'>" +
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
    @Path("/update-form-patient/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUpdateClientForm(@PathParam("id") int id) {
        // Fetch the existing record data
        Patient patient = dao.getPatientById(id);

        if (patient != null) {
            // Provide JSON response for updating a record with pre-filled data
        	String formHtml = String.format(
        	        "<form id='updateForm' action='/MyProject/api/patients/update-patient/%d' method='PUT' data-method='PUT'>" +
        	        "<label for='id'>ID:</label>" +
        	        "<input type='text' name='id' value='%d' disabled><br>" +
        	        "<label for='name'>Name:</label>" +
        	        "<input type='text' name='name' value='%s'><br>" +
        	        "<label for='surname'>Surname:</label>" +
        	        "<input type='text' name='surname' value='%s'><br>" + 
        	        "<input type='submit' value='Update Record'>" +
        	        "</form>" +
        	        "<form id='deleteForm' action='/MyProject/api/patients/delete-patient/%d' method='DELETE' data-method='DELETE'>" +
        	        
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
            return Response.status(Response.Status.NOT_FOUND).entity("Record not found").build();
        }
    }


    
@PUT
    
    @Path("/update-patient/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePatient(@PathParam("id") int id, Patient updatedPatient) {
        boolean updateSuccess = dao.updatePatient(id, updatedPatient);

        JsonObject jsonResponse;
        if (updateSuccess) {
            jsonResponse = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Patient updated successfully")
                    .build();
        } else {
            jsonResponse = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to update patient")
                    .build();
        }

        return Response.ok(jsonResponse.toString())
                .header("Content-Type", "application/json")
                .build();
    }


@DELETE

@Path("/delete-patient/{id}")
public Response deletePatient(@PathParam("id") int id) {
   boolean successful = dao.deletePatient(id);

   if(successful) {
       JsonObject jsonResponse = Json.createObjectBuilder()
               .add("status", "success")
               .add("message", "Patient deleted successfully")
               .build();

       return Response.ok(jsonResponse.toString())
               .header("Content-Type", "application/json")
                .build();
       }
       else {
    	   return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                   .entity("Failed to delete Patient.")
                   .build();
       }
}

@POST

@Path("/add-patient")
public Response addPatient(Patient patient) {
	int generatedId = dao.addPatient(patient);

    if (generatedId != -1) {
        // Add hypermedia links to the response, including the generated ID
        JsonObject jsonResponse = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Record added successfully")
                .add("generatedId", generatedId)
                .build();

        return Response.ok(jsonResponse.toString())
                .header("Content-Type", "application/json")
                .build();
    } else {
        // Handle the case where adding the record failed
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Failed to add record.")
                .build();
    }
}



}

