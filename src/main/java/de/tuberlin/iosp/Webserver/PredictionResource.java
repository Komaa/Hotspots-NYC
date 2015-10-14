package de.tuberlin.iosp.Webserver;

import de.tuberlin.iosp.Predictor.PredictorFunctions;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "prediction" path)
 */
@Path("prediction")
public class PredictionResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIt(
			@QueryParam ("lon") String lon,
			@QueryParam ("lat") String lat,
			@QueryParam ("time") String time,
			@QueryParam ("isholiday") String isholiday,
			@QueryParam ("mean_temp") String temp,
			@QueryParam ("weather") String weather,
			@QueryParam ("events") String events,
			@QueryParam ("attendees") String attendees) {
		String prediction;
		PredictorFunctions predictor = PredictorFunctions.instance();
		try {
			predictor.run();
		} catch ( Exception e ) {
			prediction = "{ \"error\": \"Building Model failed\"}";
		}

		try {
			//"-73.96841004000001,40.75801568,0:00,false,4.4,Clear,0,0"
			String input = lon + "," + lat + "," + time + "," + isholiday + "," + temp + "," + weather + "," + events + "," + attendees;
			prediction = predictor.getPredictionFor(input);
		} catch ( Exception e ) {
			prediction = "{ \"error\": \"Prediction is aborted. Are the input variables correct formatted?\"}";
			return Response.status(400) // Bad Request
					.entity(prediction)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET")
					.allow("OPTIONS").build();
		}

		return Response.ok() //200
				.entity(prediction)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET")
				.allow("OPTIONS").build();
	}
}