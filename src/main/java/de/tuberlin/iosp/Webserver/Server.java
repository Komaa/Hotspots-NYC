package de.tuberlin.iosp.Webserver;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Server {

	private static final URI BASE_URI = URI.create("http://localhost:8080/nycab/");
	public static final String ROOT_PATH = "prediction";

	public void run() {
		try {
			System.out.println("\"NYCab\" Prediction Service");

			final ResourceConfig resourceConfig = new ResourceConfig(PredictionResource.class);
			final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig);

			System.out.println(String.format("Application started.\nTry out %s%s\nHit enter to stop it...", BASE_URI, ROOT_PATH));
			System.in.read();
			server.shutdownNow();
		} catch ( IOException ex ) {
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}