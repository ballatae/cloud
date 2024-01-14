// API Configuration
var apiConfig = {
	baseUrl: "http://localhost:8082/MyProject/api",
	recordsEndpoint: "/patients",

};
var token = "AWDADA";
$(document).ready(function() {
	// Call the function with the desired token

	getDataFromREST(token);
});


function getDataFromREST(token) {


	// Construct the filter string

	// Make an AJAX request to get all records
	jQuery.ajax({
		url: apiConfig.baseUrl + apiConfig.recordsEndpoint,
		type: "GET",
		dataType: "json",
		headers: {
			"Authorization": "Bearer " + token, // Include the access token here
			"X-UserRole": "ADMIN" // Include the user roles here
		},
		success: function(resultData) {
			// Display records and associated links

			displayRecords(resultData.data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("<p>").text("ssjj").appendTo("div#neo");
			if (jqXHR.status === 401) {
				// Unauthorized access
				handleUnauthorizedError(jqXHR.responseText);
			} else {
				// Handle other errors
				console.error("Error fetching data:", textStatus, errorThrown);
			}
		},
		timeout: 120000,
	});
}

//Adjusted displayRecords function
function displayRecords(records) {
	// Clear previous data
	// $("div#recordsContainer").empty();
	$("div#forms").empty();

	// Display records in a table
	var table = $("<table>").addClass("records-table");

	records.forEach(function(record) {
		var row = $("<tr>");

		// Display only the ID initially
		for (var prop in record) {

			if (record.hasOwnProperty(prop) && prop !== '_links' && prop !== 'table') {
				if (prop.toLowerCase().includes("header")) {

					var cellValue = record[prop];
					$("<td>").text(cellValue).appendTo(row);
				}
				else if (prop.toLowerCase().includes("average")) {
					var cellValue = prop + ":" + record[prop];
					$("<td>").text(cellValue).appendTo(row);
				}
				else {
					var cellValue = record[prop];
					$("<td>").text(cellValue).appendTo(row);
				}
			}

			// If the property is an object, check its properties as well


		}
		// Add buttons dynamically based on the links
		var buttonsCell = $("<td>");

		if (record._links) {
			Object.keys(record._links).forEach(function(linkKey) {
				$("<button>").text(linkKey).click(function() {

					handleHypermediaAction(record._links[linkKey]);
				}).appendTo(buttonsCell);
			});
		}
		if (record.table) {
			Object.keys(record.table).forEach(function(linkKey) {
				$("<button>").text(linkKey).click(function(linkkey) {

					handleHypermediaAction(record.table[linkKey]);
				}).appendTo(buttonsCell);
			});
		}
		buttonsCell.appendTo(row);

		// Append the row to the table
		table.append(row);
	});

	// Append the table to the container
	$("div#recordsContainer").html(table);
}


// Function to handle hypermedia actions based on the provided link
function handleHypermediaAction(link) {

	// Check if the method is GET
	if (link.method && link.method.toUpperCase() === "GET") {
		// Make an AJAX request to fetch data
		jQuery.ajax({
			url: link.href,
			type: link.method || "GET",
			dataType: "json",
			headers: {
				"Authorization": "Bearer " + token, // Include the access token here
				"X-UserRole": "ADMIN"

			},
			data: $(this).serialize(),
			success: function(resultData) {

				// Clear previous data
				$("div#fullResource").empty();

				// Check if the response contains an update form
				if (resultData.formHtml) {

					// Inject the HTML form directly into the div
					$("div#forms").html(resultData.formHtml);
					// Add a submit event listener to the form
					$("form").submit(function(event) {
						event.preventDefault(); // Prevent the default form submission

						// Serialize form data to JSON


						var formData = {};

						$(this).find(':input').each(function() {
							formData[this.name] = $(this).val();

						});

						// Determine the HTTP method based on the clicked submit button
						var httpMethod = $(this).data('method') || "POST";
						var data = JSON.stringify(formData);
						if (httpMethod === "GET") {
							data = $(this).serialize();
						}


						// Make an AJAX request with the determined HTTP method
						$.ajax({
							url: $(this).attr('action'), // Use the form's action URL
							type: httpMethod, // Default to POST if no method is specified
							dataType: "json",
							contentType: "application/json",
							data: data,
							headers: {
								"Authorization": "Bearer " + token, // Include the access token here
								"X-UserRole": "ADMIN"
							},
							processData: false,

							success: function(resultData) {

								// Clear the form and display a success message
								if (resultData && resultData.data && Array.isArray(resultData.data) && resultData.data.length > 1) {
									var chartData = resultData.data[1].chartData;

									if (chartData && Array.isArray(chartData)) {
										var chartData = resultData.data[1].chartData;

										// Extract labels and data arrays from chartData
										var labels = chartData.map(entry => entry.entryDate);
										var glucoseLevels = chartData.map(entry => entry.glucoseLevel);
										var carbIntake = chartData.map(entry => entry.carbIntake);
										
										var data = {
											labels: labels,
											datasets: [{
												label: 'Daily Glucose Level',
												data: glucoseLevels,
												borderColor: 'rgb(75, 192, 192)',
												borderWidth: 2,
												fill: false
											}, {
												label: 'Carbon intake',
												data: carbIntake, // Replace with your actual data array
												borderColor: 'rgb(190, 99, 132)',
												borderWidth: 2,
												fill: false
											}]
										};
										
										
										$("#canva").css("background-color", "#f1f1f1");

										createLineChart(data);
									}
								}

								$("div#forms").empty();

								var successMessage;
								if (httpMethod === "POST") {
									successMessage = "Record added successfully!";
								} else if (httpMethod === "PUT") {
									successMessage = "Record updated successfully!";
								} else if (httpMethod === "DELETE") {
									successMessage = "Record deleted successfully!";
								} else if (httpMethod === "GET") {
									displayTable(resultData);
								}

								else {

								}
								getDataFromREST(token);

								$("<p>").text(successMessage).appendTo("div#fullResource");


							},
							error: function(jqXHR, textStatus, errorThrown) {
								$("<p>").text("ssjj").appendTo("div#neo");
								if (jqXHR.status === 401) {
									// Unauthorized access
									handleUnauthorizedError(jqXHR.responseText);
								} else {
									// Handle other errors
									console.error("Error fetching data:", textStatus, errorThrown);
								}
							}
						});
					});
				} else {

					displayTable(resultData);


					// Iterate through the properties of the resultData


					// Add buttons for links in the full resource

				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				// Display error message
				console.error("Error:", textStatus, errorThrown);
			},
			timeout: 120000,
		});
	} else {
		// Handle other methods if needed
	}
}

function displayTable(resultdata) {
	// Clear previous data
	//$("div#fullResource").empty();

	// Check if there is only one record

	var data = resultdata.data;

	if (data == undefined) {
		var record = resultdata;


		// Display the record in a table
		var table = $("<table>");
		var row = $("<tr>");
		var head = $("<tr>");

		for (var prop in record) {

			if (record.hasOwnProperty(prop) && prop !== '_links' && prop !== 'chartData') {
				// Display property and value in the table row
				if (prop.toLowerCase().includes("header")) {
					var cellValue = record[prop];
					$("<th>").text(cellValue).appendTo(head);
				}
				else if (prop.toLowerCase().includes("average")) {
					var cellValue = prop + ":" + record[prop];
					$("<td>").text(cellValue).appendTo(row);
				}
				else {
					var cellValue = record[prop];
					$("<td>").text(cellValue).appendTo(row);
				}
			}
		}

		// Append the row to the table
		table.append(head);
		table.append(row);

		// Append the table to the container
		$("div#fullResource").html(table);

		// Add buttons dynamically based on the links
		var buttonsCell = $("<div>");
		if (record._links) {
			Object.keys(record._links).forEach(function(linkKey) {
				$("<button>").text(linkKey).click(function() {
					handleHypermediaAction(record._links[linkKey]);
				}).appendTo(buttonsCell);
			});
		}
		// Append the buttons to the container
		$("div#fullResource").append(buttonsCell);
	} else {

		// Display records in a table
		var table = $("<table>").addClass("record-table");

		data.forEach(function(record) {
			var row = $("<tr>");

			for (var prop in record) {

				if (record.hasOwnProperty(prop) && prop !== '_links' && prop !== 'chartData') {
					// Display only the ID initially
					if (prop.toLowerCase().includes("header")) {
						var cellValue = record[prop];
						$("<td>").text(cellValue).appendTo(row).addClass("black");
					}
					else if (prop.toLowerCase().includes("average")) {
						var cellValue = prop + ":" + record[prop];
						$("<td>").text(cellValue).appendTo(row);
					}
					else {
						var cellValue = record[prop];
						$("<td>").text(cellValue).appendTo(row);
					}
				}
			}

			// Add buttons dynamically based on the links
			var buttonsCell = $("<td>");
			if (record._links) {
				Object.keys(record._links).forEach(function(linkKey) {
					$("<button>").text(linkKey).click(function() {

						handleHypermediaAction(record._links[linkKey]);
					}).appendTo(buttonsCell);
				});
			}
			// Append the buttons to the row
			row.append(buttonsCell);

			// Append the row to the table
			table.append(row);
		});

		// Append the table to the container
		$("div#table-resource").html(table);
	}
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
function createLineChart(chartData) {
	var canvas = document.getElementById('myLineChart');
	var ctx = canvas.getContext('2d');
	// Check if a chart instance already exists
	var existingChart = Chart.getChart(ctx);

	// Destroy the existing chart instance if present
	if (existingChart) {
		existingChart.destroy();
	}

	 var chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            x: [{
                ticks: {
                    color: 'white', // Set the font color of the x-axis labels
                }
            }],
            y: [{
                ticks: {
                    color: 'white', // Set the font color of the y-axis labels
                }
            }]
        }
    };

	// Create the line chart
	new Chart(ctx, {
		type: 'line',
		data: chartData,
		options: chartOptions
	});
}