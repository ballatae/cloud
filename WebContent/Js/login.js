// API Configuration
var apiConfig = {
    baseUrl: "http://localhost:8082/MyProject/api",
    recordsEndpoint: "/patients",
   
};
var token;

function login() {
    // Get the username and password from the form
    var username = $("#username").val();
    var password = $("#password").val();

    // Make an AJAX request to the login endpoint
    $.ajax({
        url: apiConfig.baseUrl + apiConfig.recordsEndpoint+"/login", // Replace with your actual login endpoint
        type: "POST",
        headers: {
            "Authorization": "Bearer AWDADA", // Include the access token here
            "X-UserRole": "ADMIN" // Include the user roles here
            
        },
        contentType: "application/json",
        data: JSON.stringify({ "username": username, "password": password }),
        success: function (response) {
           
        	window.location.href = "records.html";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            // Failed login
             $("<p>").text("ssjj").appendTo("div#neo");
            console.log(password);
            // Show an error message to the user
            console.error("Login failed:", textStatus, errorThrown);

            // You may want to display an error message to the user
            // ...

            // Clear the password field for security reasons
            $("#password").val("");
        }
    });
}




function handleUnauthorizedError(errorMessage) {
    // Custom logic for handling unauthorized errors
    console.error("Unauthorized access:", errorMessage);

    // Display a user-friendly message to the user
    alert("Unauthorized access. Please log in or check your permissions.");
    // You can also redirect the user to a login page or take other actions as needed.
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}