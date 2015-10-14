/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

            $(document).ready(function () {
                    $('#birthday').daterangepicker({
                        singleDatePicker: true,
                        minDate: '01/01/2013',
                        maxDate: '12/31/2013'
                    },
                    function (start, end, label) {
                        console.log(start.toISOString().substring(0,10), end.toISOString(), label);
                        changeHotSpots(start.toISOString().substring(0,10));
                    });
                });

var map, pointarray, heatmap;
var jsoncontent;

var initial_hour;

var taxiDataFromFile = [];
var markers = [];

var globalHour = '0';
var globalDay = "2013-01-09";

// 0 means we will start with the start points prediction
var ispickup = true;

var pickup;
var dropoff;
function loadFiles() {
    pickupfile = readTextFile("DayPickUpJSONFile.json");
    this.pickup = JSON.parse(pickupfile);
    dropofffile = readTextFile("DayDropOffJSONFile.json");
    this.dropoff = JSON.parse(dropofffile);
}
loadFiles();

// function executelog() {
//     if (this.file = 0)
// }

function readFile(date, hour) {
    if (ispickup) {
        obj = this.pickup;
    }
    else {
        obj = this.dropoff;
    }

    var text = "";
    for (i = 0; i < obj.days.length; i++) {
        // console.log(obj.days[i].day);
        if (obj.days[i].day.toString() == date) {

            for (j = 0; j < obj.days[i].Hour.length; j++) {
                // if (obj.days[i].Hour[j].initial_hour.toString() == (initial_hour.toString() + ":00")) {
                if (obj.days[i].Hour[j].initial_hour.toString() == (hour + ":00")) {
                    // console.log(obj.days[i].Hour[j].initial_hour + "   " + obj.days[i].Hour[j].HotSpot.length + "   " + initial_hour);
                    //console.log('we are here!!!!' + obj.days[i].Hour[j].initial_hour);
                    for (k = 0; k < obj.days[i].Hour[j].Event.length; k++) {
                        drawEventMarkers(
                                obj.days[i].Hour[j].Event[k].Longitude,

                                obj.days[i].Hour[j].Event[k].Latitude,
                                obj.days[i].Hour[j].Event[k].id
                        )

                    }

                    for (k = 0; k < obj.days[i].Hour[j].HotSpot.length; k++) {
//                         {location: new google.maps.LatLng(40.645361689999994, -73.78384995), weight: 99},
                        var lat = obj.days[i].Hour[j].HotSpot[k].latitude;
                        var long = obj.days[i].Hour[j].HotSpot[k].longitude;
                        var people = obj.days[i].Hour[j].HotSpot[k].people;
//                        console.log(lat + " " + long + " " + people);
                        taxiDataFromFile.push({location: new google.maps.LatLng(long, lat), weight: people})
                    }
                    initial_hour = initial_hour + 4;
                    if (initial_hour > 20) {
                        initial_hour = 0;
                    }
                    break;
                }

            }
            break;
            // Why is there a break here?
        }

    }
}


function readTextFile(file) {
//    alert("fooo");
    var rawFile = new XMLHttpRequest();
    rawFile.open("GET", file, false);
    rawFile.onreadystatechange = function ()
    {
        if (rawFile.readyState === 4)
        {
            if (rawFile.status === 200 || rawFile.status === 0)
            {
                var allText = rawFile.responseText;
//                alert(allText);
            }
        }
    };
    rawFile.send(null);
    return(rawFile.responseText);
}

var taxiData2 = [];



function initialize() {

    var mapOptions = {
        zoom: 12,
        center: new google.maps.LatLng(40.72880909, -74.00755915),
        mapType: google.maps.MapTypeId.HYBRID,
        streetViewControl: false,
        maxZoom: 14,
        minZoom: 12,
    };

    map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
    pointArray = new google.maps.MVCArray(taxiData2);
    heatmap = new google.maps.visualization.HeatmapLayer({data: pointArray});
    heatmap.setMap(map);

    initial_hour = 0;

    heatmap.set('radius', heatmap.get('radius') ? null : 60);
    //changeGradient();
    //changeHotSpots();
    //TODO add markers from list
    // drawEventMarkers();
    //console.log(eventPoints);
    addOptions();

    // This event listener will call addMarker() when the map is clicked.
    google.maps.event.addListener(map, 'click', function (event) {
        addMarker(event.latLng);
    });

    dataVisualisation();
    addMarker(new google.maps.LatLng(40.75801568, -73.96841004));

}


//Attach click event handler to the marker.

// function addInfoWindow() {
//     marker = markers[0];
//     google.maps.event.addListener(marker, "click", function (e) {
//         var infoWindow = new google.maps.InfoWindow({
//             content: 'Latitude: ' + location.lat() + '<br />Longitude: ' + location.lng() + "fooooooooooooo"
//         });
//         infoWindow.open(map, marker);
//     });
// }



var secondClick = false;
var globalLocationForClick;

// Add a marker to the map and push to the array. will delete old marker if new one is added
function addMarker(location) {
    globalLocationForClick = location;
    if (secondClick) {
        deleteMarkers();
    }
    var marker = new google.maps.Marker({
        position: location,
        map: map
    });
    markers.push(marker);
    secondClick = true;
    document.getElementById("latitude").value = location.lat();
    document.getElementById("longitude").value = location.lng();
}

// Deletes all markers in the array by removing references to them.
function deleteMarkers() {
    clearMarkers();
    markers = [];
}

// Removes the markers from the map, but keeps them in the array.
function clearMarkers() {
    setAllMap(null);
}

function drawEventMarkers(lng, lat, url) {
    mark = new google.maps.Marker({
        position: new google.maps.LatLng(lng, lat),
        map: map
    });
    var infoWindow = new google.maps.InfoWindow({
         content: 'Latitude: '+lng+'<br />Longitude: '+lat+"<br /><a href='http://www.facebook.com/"+url+"'>link here</a>"
    });
    google.maps.event.addListener(mark, "click", function (e) {
        infoWindow.open(map, this);
    });
    markers.push(mark);
}

function changeHotSpots() {
    console.log('globalDay = ' + globalDay + ' globalHour=' + globalHour);
    setAllMap(null);
    markers = [];
 
    readFile(globalDay, globalHour);
    setAllMap(map);

    pointArray.clear();
    for (var int = 0; int < taxiDataFromFile.length; int++) {
        pointArray.push(taxiDataFromFile[int]);
    }
    taxiDataFromFile = [];
}


function clearMap() {
    pointArray.clear();
}


function toggleHeatmap() {
    heatmap.setMap(heatmap.getMap() ? null : map);
}

function changeGradient() {

    var gradient = [
        'rgba(0, 255, 255, 0)',
        'rgba(0, 255, 255, 1)',
        'rgba(0, 191, 255, 1)',
        'rgba(0, 127, 255, 1)',
        'rgba(0, 63, 255, 1)',
        'rgba(0, 63, 255, 1)',
        'rgba(0, 63, 255, 1)',
        'rgba(0, 63, 255, 1)',
        'rgba(0, 0, 255, 1)',
        'rgba(0, 0, 223, 1)',
        'rgba(0, 0, 191, 1)',
        'rgba(0, 0, 159, 1)',
        'rgba(0, 0, 127, 1)',
        'rgba(60, 0, 91, 1)',
        'rgba(100, 0, 63, 1)',
        'rgba(140, 0, 31, 1)',
        'rgba(180, 0, 0, 1)'
    ]
    heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
}

function changeRadius() {
    heatmap.set('radius', heatmap.get('radius') ? null : 20);
}

function changeOpacity() {
    heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}

function showValue(num) {
    var result = document.getElementById('time');
    result.innerHTML = num;
}

// Sets the map on all markers in the array.
function setAllMap(map) {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(map);
    }
}

function addOptions() {
    var weatherConditions = ["Clear", "HeavySnow", "LightRain", "Snow", "Haze", "PartlyCloudy",
        "LightSnow", "HeavyRain", "Rain", "ScatteredClouds", "Unknown", "MostlyCloudy",
        "LightFreezingRain", "Overcast", "Mist", "Fog"];
    for (var i = 0; i < weatherConditions.length; i++) {
        //AddItem(weatherConditions[i].toString(), weatherConditions[i].toString);
        var opt = document.createElement("option");

        // Add an Option object to Drop Down/List Box
        document.getElementById("DropDownListWeather").options.add(opt);

        // Assign text and value to Option object
        opt.text = weatherConditions[i].toString();
        opt.value = weatherConditions[i].toString();
    }


    for (var i = 0; i < 24; i++) {
        //AddItem(weatherConditions[i].toString(), weatherConditions[i].toString);
        var opt = document.createElement("option");

        // Add an Option object to Drop Down/List Box
        document.getElementById("DropDownListTime").options.add(opt);

        // Assign text and value to Option object
        opt.text = i + ":00";
        opt.value = i + ":00";
    }
}

function AddItem(Text, Value) {
    // Create an Option object
    var opt = document.createElement("option");

    // Add an Option object to Drop Down/List Box
    document.getElementById("DropDownListWeather").options.add(opt);

    // Assign text and value to Option object
    opt.text = Text;
    opt.value = Value;

}

function test() {
    document.getElementById('demo').innerHTML = 'Hello JavaScript!';
}

// 0 means visualisation
var switchMode = 0;

function dataVisualisation() {
    switchMode = 0;
    document.getElementById("data_visualization").checked = true;

    document.getElementById("slider").disabled = false;
    document.getElementById("birthday").disabled = false;

    document.getElementById("latitude").disabled = true;
    document.getElementById("longitude").disabled = true;
    document.getElementById("submitBtn").disabled = true;
    document.getElementById("temp").disabled = true;
    document.getElementById("fb_people").disabled = true;
    document.getElementById("fb_events").disabled = true;
    document.getElementById("DropDownListWeather").disabled = true;
    document.getElementById("DropDownListTime").disabled = true;
    document.getElementById("isholiday").disabled = true;

}

function activatePrediction() {
    switchMode = 1;
    document.getElementById("prediction").checked = true;

    document.getElementById("slider").disabled = true;
    document.getElementById("birthday").disabled = true;

    document.getElementById("latitude").disabled = false;
    document.getElementById("longitude").disabled = false;
    document.getElementById("submitBtn").disabled = false;
    document.getElementById("temp").disabled = false;
    document.getElementById("fb_people").disabled = false;
    document.getElementById("fb_events").disabled = false;
    document.getElementById("DropDownListWeather").disabled = false;
    document.getElementById("DropDownListTime").disabled = false;
    document.getElementById("isholiday").disabled = false;

    setAllMap(null);
    markers = [];
    setAllMap(map);
    pointArray.clear();
}

google.maps.event.addDomListener(window, 'load', initialize);

google.maps.event.addListener(map, 'click', function (event) {
    placeMarker(event.latLng);
});

function placeMarker(location) {
    var marker = new google.maps.Marker({
        position: location,
        map: map
    });

}

///////////////////////////////
////Init Date Range Picker/////
///////////////////////////////
$(document).ready(function () {
    $('#birthday').daterangepicker({
        singleDatePicker: true,
        minDate: '01/01/2013',
        maxDate: '12/31/2013'
    },
    function (start, end, label) {
        globalDay = start.toISOString().substring(0, 10);
        // console.log(start.toISOString().substring(0,10), end.toISOString(), label);
        changeHotSpots();
    });
});

function toggleStartDest(elem) {
    var start = document.getElementById("startBtn");
    var dest = document.getElementById("destinationBtn");
    if (start == elem) {
        // Start pressed!
        console.log('start pressed!');
        start.className = "btn btn-primary";
        dest.className = "btn";
        ispickup = true;
    } else if (dest == elem) {
        console.log('dest pressed!');
        start.className = "btn";
        dest.className = "btn btn-primary"
        ispickup = false;
    }
    if (switchMode == 0) {
        changeHotSpots();
    } else {
        predict();
    }   
}

