var points = 5;

$(document).ready(function () {

	$('#PredictionForm').submit(function(e) {
		e.preventDefault();

		var $form = $(this);
    	setAllMap(null);
    	markers = [];
    	// pointArray = [];

		circlex = parseFloat(document.getElementById("latitude").value);
		circley = parseFloat(document.getElementById("longitude").value);
		// Generate points inside the circle which centre is the points given in the form
		var radius = 0.05;

    	serializedData = $form.serialize();
		ajaxQuery(serializedData,circlex,circley,-1);

		for (var i=0; i<points; i++)
		{
			x = Math.random() * 2 * radius - radius;
			ylim = Math.sqrt(radius * radius - x * x);
			y = Math.random() * 2 * ylim - ylim;
			// Offset so that the circle is all on the screen   
			x += circlex;
			y += circley;
			document.getElementById("latitude").value = x;
			document.getElementById("longitude").value = y;
			serializedData = $form.serialize();
			ajaxQuery(serializedData,x,y,i);
		}
	});
});

function predict() {
	$('#PredictionForm').submit();
}

function ajaxQuery(serializedData,x,y,i) {
	// console.log(serializedData);
	var jqxhr = $.ajax({
		method: "GET",
		url: "http://localhost:8080/nycab/prediction",
		data: serializedData
	})
	.done(function( msg ) {
		console.log( "success" );
		console.log( msg );
		processResponse(msg,x,y,i);
	})
	.fail(function( jqXHR, textStatus ) {
		console.log( "error" );
		console.log( jqXHR );
		console.log( textStatus );
	})
	.always(function() {
		console.log( "complete" );
	});
}

function processResponse(msg,x,y,i) {
	// This will add the point to the map!
	var ishotspot, npeople;
	if (ispickup) {
		// Look at pickup data
		console.log('ispickup');
		ishotspot = msg.ishotspotpickup;
		npeople = msg.npeoplepickup;
	} else {
		// Look at dropoff data
		console.log('isdropoff');
		ishotspot = msg.npeopleishotspotdropoff;
		npeople = msg.npeopledropoff;
	}

	if (ishotspot=='true' || i==-1) {
		// console.log("We are ereee");
	    mark = new google.maps.Marker({
	        position: new google.maps.LatLng(x,y),
	        map: map
	    });
	    var infoWindow = new google.maps.InfoWindow({
	         content: 'ishotspot: '+ishotspot+' npeople='+npeople
	    });
	    google.maps.event.addListener(mark, "click", function (e) {
	        infoWindow.open(map, this);
	    });
	    // mark.setMap(map);
	    // new google.maps.LatLng(long, lat)
	    // pointArray.push({location: new google.maps.LatLng(x, y), weight: npeople});
	    markers.push(mark);

	}
	// setAllMap(map);
    if (i == points-1) {
    	console.log('LAST EXECUTION...');
    	setAllMap(map);
    }
 
}

