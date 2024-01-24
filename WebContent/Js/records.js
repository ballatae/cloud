// API Configuration
var apiConfig = {
  baseUrl: "/api",
  recordsEndpoint: "/records",
};
var storedToken = localStorage.getItem("accessToken");
$(document).ready(function () {
  // Call the function with token
  if (storedToken == null) {
    window.location.href = "index.html";
  }
  getDataFromREST();
});

function getDataFromREST() {
  // Make an AJAX request to get all records
  jQuery.ajax({
    url: apiConfig.baseUrl + apiConfig.recordsEndpoint,
    type: "GET",
    dataType: "json",
    headers: {
      Authorization: "JWT " + storedToken,
    },
    success: function (resultData) {
      // Display records and associated links
      displayRecords(resultData.data);
    },
    error: function (jqXHR, textStatus, errorThrown) {
      handleUnauthorizedError(jqXHR.responseText);
      if (jqXHR.status === 401) {
        // Unauthorized access
        handleUnauthorizedError(jqXHR.responseText);
      } else {
        // Handle other errors
        handleUnauthorizedError(jqXHR.responseText);
      }
    },
    timeout: 120000,
  });
}

/// Function to create buttons based on links
function createButtons(record, buttonsCell) {
  if (record._links) {
    Object.keys(record._links).forEach(function (linkKey) {
      $("<button>")
        .text(linkKey)
        .click(function () {
          handleHypermediaAction(record._links[linkKey]);
        })
        .appendTo(buttonsCell);
    });
  }
  if (record.table) {
    Object.keys(record.table).forEach(function (linkKey) {
      $("<button>")
        .text(linkKey)
        .click(function () {
          handleHypermediaAction(record.table[linkKey]);
        })
        .appendTo(buttonsCell);
    });
  }
}

// Function to get cell value based on property
function getCellValue(prop, value) {
  if (prop.toLowerCase().includes("header")) {
    return value;
  } else if (prop.toLowerCase().includes("average")) {
    return prop + ":" + value;
  } else {
    return value;
  }
}

// Updated displayRecords function
function displayRecords(records) {
  // Clear previous data
  $("div#forms").empty();

  // Display records in a table
  var tableElement = $("<table>").addClass("records-table");

  records.forEach(function (record) {
    var rowElement = $("<tr>");

    // Display only the ID initially
    for (var prop in record) {
      if (
        record.hasOwnProperty(prop) &&
        prop !== "_links" &&
        prop !== "table"
      ) {
        var cellValue = getCellValue(prop, record[prop]);
        var cellElement;

        // Use th for header cells with a different background color
        if (prop.toLowerCase().includes("header")) {
          cellElement = $("<th>")
            .text(cellValue)
            .css("background-color", "#4CAF50");
        } else {
          cellElement = $("<td>").text(cellValue);
        }

        cellElement.appendTo(rowElement);
      }
    }

    // Add buttons dynamically based on the links
    var buttonsCell = $("<td>");
    createButtons(record, buttonsCell);
    buttonsCell.appendTo(rowElement);

    // Append the row to the table
    tableElement.append(rowElement);
  });

  // Append the table to the container
  $("div#recordsContainer").html(tableElement);
}

// Updated displayTable function
function displayTable(resultdata) {
  // Clear previous data
  $("div#fullResource").empty();

  // Check if there is only one record
  var data = resultdata.data;

  if (data == undefined) {
    var record = resultdata;

    // Display the record in a table
    var table = $("<table>").addClass("record-table");
    var row = $("<tr>");
    var head = $("<tr>");

    for (var prop in record) {
      if (
        record.hasOwnProperty(prop) &&
        prop !== "_links" &&
        prop !== "chartData"
      ) {
        // Display property and value in the table row
        if (prop.toLowerCase().includes("header")) {
          var cellValue = record[prop];
          $("<th>").text(cellValue).appendTo(head);
        } else if (prop.toLowerCase().includes("average")) {
          var cellValue = prop + ":" + record[prop];
          $("<td>").text(cellValue).appendTo(row);
        } else {
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
      Object.keys(record._links).forEach(function (linkKey) {
        $("<button>")
          .text(linkKey)
          .click(function () {
            handleHypermediaAction(record._links[linkKey]);
          })
          .appendTo(buttonsCell);
      });
    }
    // Append the buttons to the container
    $("div#fullResource").append(buttonsCell);
  } else {
    // Display records in a table
    var tableElement = $("<table>").addClass("record-table");

    data.forEach(function (record) {
      var rowElement = $("<tr>");

      for (var prop in record) {
        if (
          record.hasOwnProperty(prop) &&
          prop !== "_links" &&
          prop !== "chartData"
        ) {
          var cellValue = getCellValue(prop, record[prop]);

          if (prop.toLowerCase().includes("header")) {
            cellElement = $("<th>")
              .text(cellValue)
              .css("background-color", "#4CAF50");
          } else if (prop.toLowerCase().includes("average")) {
            var cellValue = prop + ":" + record[prop];
            cellElement = $("<th>")
              .text(cellValue)
              .css("background-color", "#4CAF50");
          } else {
            cellElement = $("<td>").text(cellValue);
          }

          cellElement.appendTo(rowElement);
        }
      }

      // Add buttons dynamically based on the links
      var buttonsCell = $("<td>");
      createButtons(record, buttonsCell);
      rowElement.append(buttonsCell);

      // Append the row to the table
      tableElement.append(rowElement);
    });

    // Append the table to the container
    $("div#table-resource").html(tableElement);
  }
}

// Function to handle hypermedia actions based on the provided link
function handleHypermediaAction(link) {
  // Check if the method is GET
  if (
    (link.method && link.method.toUpperCase() === "GET") ||
    link.method.toUpperCase() === "DELETE"
  ) {
    // Make an AJAX request to fetch data
    jQuery.ajax({
      url: link.href,
      type: link.method || "GET",
      dataType: "json",
      headers: {
        Authorization: "JWT " + storedToken,
      },
      data: $(this).serialize(),
      success: function (resultData) {
        // Check if the response contains an update form
        if (resultData.formHtml) {
          // Inject the HTML form directly into the div
          $("div#forms").html(resultData.formHtml);
          // Add a submit event listener to the form
          $("form").submit(function (event) {
            event.preventDefault(); // Prevent the default form submission
            $("div#forms").empty();
            $("div#fullResource").empty();

            var formData = {};

            $(this)
              .find(":input")
              .each(function () {
                formData[this.name] = $(this).val();
              });

            // Determine the HTTP method based on the clicked submit button
            var httpMethod = $(this).data("method") || "POST";
            var data = JSON.stringify(formData);
            if (httpMethod === "GET") {
              data = $(this).serialize();
            }

            // Make an AJAX request with the determined HTTP method
            $.ajax({
              url: $(this).attr("action"), // Use the form's action URL
              type: httpMethod,
              dataType: "json",
              contentType: "application/json",
              data: data,
              headers: {
                Authorization: "JWT " + storedToken,
              },
              processData: false,
              success: function (resultData) {
                // Clear the form and display a success message
                if (
                  resultData &&
                  resultData.data &&
                  Array.isArray(resultData.data) &&
                  resultData.data.length > 1
                ) {
                  var chartData = resultData.data[1].chartData;

                  if (chartData && Array.isArray(chartData)) {
                    var chartData = resultData.data[1].chartData;

                    // Extract labels and data arrays from chartData
                    var labels = chartData.map((entry) => entry.entryDate);
                    var glucoseLevels = chartData.map(
                      (entry) => entry.glucoseLevel
                    );
                    var carbIntake = chartData.map((entry) => entry.carbIntake);
                    var data = {
                      labels: labels,
                      datasets: [
                        {
                          label: "Daily Glucose Level",
                          data: glucoseLevels,
                          borderColor: "rgb(75, 192, 192)",
                          borderWidth: 2,
                          fill: false,
                        },
                        {
                          label: "Carbon intake",
                          data: carbIntake, // Replace with your actual data array
                          borderColor: "rgb(190, 99, 132)",
                          borderWidth: 2,
                          fill: false,
                        },
                      ],
                    };
                    $("#canva").css("background-color", "#555555");
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
                } else {
                  successMessage = null;
                  displayTable(resultData);
                }

                successMessage = successMessage ? successMessage.trim() : null;

                if (successMessage !== null) {
                  alert(successMessage);
                }
              },
              error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status === 401) {
                  // Unauthorized access
                  handleUnauthorizedError(jqXHR.responseText);
                } else {
                  // Handle other errors
                  handleUnauthorizedError(jqXHR.responseText);
                  getDataFromREST();
                }
              },
            });
          });
        } else {
          displayTable(resultData);
        }
      },
      error: function (jqXHR, textStatus, errorThrown) {
        // Display error message
        handleUnauthorizedError(jqXHR.responseText);
      },
      timeout: 120000,
    });
  } else {
    // Handle other methods if needed
  }
}

function handleUnauthorizedError(errorMessage) {
  alert(errorMessage);
}

function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}
function logOut() {
  localStorage.removeItem("accessToken");
  window.location.href = "index.html";
}

function createLineChart(chartData) {
  var canvas = document.getElementById("myLineChart");
  var ctx = canvas.getContext("2d");

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
      x: {
        ticks: {
          color: "white", // Change X-axis tick color to white
        },
      },
      y: {
        ticks: {
          color: "white", // Change Y-axis tick color to white
        },
      },
    },
    plugins: {
      legend: {
        labels: {
          color: "white", // Change legend label color to white
        },
      },
    },
  };

  // Merge chart options with the provided chartData
  var mergedOptions = $.extend(true, {}, chartOptions, chartData.options || {});

  // Create the line chart
  new Chart(ctx, {
    type: "line",
    data: chartData,
    options: mergedOptions,
  });
}
