package de.tuberlin.iosp;

import de.tuberlin.iosp.Detector.HotspotDetector;
import de.tuberlin.iosp.Parser.JsonGenerator;
import de.tuberlin.iosp.Predictor.PredictorFunctions;
import de.tuberlin.iosp.Webserver.Server;

import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class App {

	public static void main (String[] args) {

		System.out.println("----------------------");
		System.out.println("--IOSP NYCab Project--");
		System.out.println("----------------------\n");

		App app = new App();
		app.menu();
	}

	public void menu() {
		int decision;

		do {
			try {
				this.printMenu();
				String input = this.getDecision();
				decision = parseInt(input);

				if ( decision == 0 ) {
					System.out.println("---Exiting Program ...");
					break;
				}

				this.doAction(decision);
			}
			catch (Exception e ) {
				System.out.println("Couldn't parse input, please try again");
			}
		} while (true);
	}

	private void doAction (int action) {
		switch ( action ) {
			case 1:
				System.out.println("");
				System.out.println("---Finding Hotspots ... (this can take some time)");
				HotspotDetector detector = new HotspotDetector();
				detector.run();
				break;

			case 2:
				System.out.println("");
				System.out.println("---Parse Data to JSON ...");
				JsonGenerator generator = new JsonGenerator();
				generator.run();
				break;

			case 3:
				System.out.println("");
				System.out.println("---Predict with manual input ...");
				PredictorFunctions pred = PredictorFunctions.instance();
				try {
					pred.run();
					pred.startManualPrediction();
				} catch ( Exception e ) {
					System.out.println("An unexpected error occured while using PredictorFunction.run()");
				}
				break;

			case 4:
				System.out.println("");
				System.out.println("---Starting Webservice ...");
				Server server = new Server();
				server.run();
				break;

			default:
				System.out.println("No menu item with such a label. Please try again.");
		}
		System.out.println("");
	}

	private String getDecision () {
		Scanner in = new Scanner(System.in);
		return in.nextLine();
	}

	private void printMenu () {
		String actions = "\t[1] Calculate Hotspots\n" +
				"\t[2] Parse Hotspots to JSON\n" +
				"\t[3] Test Predictor manually\n" +
				"\t[4] Start Webservice\n" +
				"\t[0] Exit\n";

		System.out.println("---Menu---");
		System.out.println( actions );
		System.out.print("Please choose one of the following actions: ");
	}
}
