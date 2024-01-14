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

import com.example.restapi.dao.RecordDAO;





@Path("/records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecordResource {

    private RecordDAO dao = RecordDAO.getInstance();
    
//    @POST
//    @Path("/login")
//    public JsonObject login(@Context ContainerRequestContext requestContext) {
//        // Validate the provided credentials (this is just a basic example)
//    	String token = (String) requestContext.getProperty("token");
//
//       
//            return Json.createObjectBuilder()
//                    .add("status", "Ok")
//                    .add("message", "valid credentials")
//                    .add("token", token)
//                    .build();
//        }
//    

    @GET
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
            return Response.status(Response.Status.NOT_FOUND).entity("Record not found").build();
        }
    }

  

    
    @GET
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
    
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRecord(@PathParam("id") Long id, Record updatedRecord) {
        boolean updateSuccess = dao.updateRecord(id, updatedRecord);

        JsonObject jsonResponse;
        if (updateSuccess) {
            jsonResponse = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Record updated successfully")
                    .build();
        } else {
        	 return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity("Failed to update record.")
                     .build();
        }

        return Response.ok(jsonResponse.toString())
                .header("Content-Type", "application/json")
                .build();
    }
    
    
    @DELETE
    
    @Path("/delete/{id}")
   public Response deleteRecord(@PathParam("id") Long id) {
       boolean successful = dao.deleteRecord(id);
       if(successful) {
       JsonObject jsonResponse = Json.createObjectBuilder()
               .add("status", "success")
               .add("message", "Record deleted successfully")
               .build();

       return Response.ok(jsonResponse.toString())
               .header("Content-Type", "application/json")
                .build();
       }
       else {
    	   return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                   .entity("Failed to delete record.")
                   .build();
       }
    }
    
  

    @POST

    @Path("/add")
    public Response addRecord(Record record) {
    	System.out.println(record);
        long generatedId = dao.addDailyRecord(record);

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
