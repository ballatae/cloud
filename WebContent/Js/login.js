// API Configuration
var apiConfig = {
  baseUrl: "/api",
  recordsEndpoint: "/records",
  logIn: "/login",
};
var token;

function login() {
  // Get the username and password from the form
  var username = $("#username").val();
  var password = $("#password").val();

  // Make an AJAX request to the login endpoint
  $.ajax({
    url: apiConfig.baseUrl + apiConfig.recordsEndpoint + apiConfig.logIn, // Replace with your actual login endpoint
    type: "POST",

    contentType: "application/json",
    data: JSON.stringify({ username: username, password: password }),
    success: function (response) {
      console.log(response.token);
      localStorage.setItem("accessToken", response.token);
      window.location.href = "records.html";
    },
    error: function (jqXHR, textStatus, errorThrown) {
      // Failed login
      handleUnauthorizedError(
        "Authentication failed! Please check your credentials"
      );

      // Clear the password field for security reasons
      $("#password").val("");
    },
  });
}

function handleUnauthorizedError(errorMessage) {
  // Custom logic for handling unauthorized errors

  // Display a user-friendly message to the user
  alert(errorMessage);
  // You can also redirect the user to a login page or take other actions as needed.
}

function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}
