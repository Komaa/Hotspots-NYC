package de.tuberlin.iosp.Predictor;

import de.tuberlin.iosp.Detector.Config;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;
import java.util.Scanner;

public class PredictorFunctions {

    private static PredictorFunctions instance;

    private Instance instclassificationpickup, instclassificationdropoff;
    private Instance instregressionpickup, instregressiondropoff;
    private Instances dataclassificationpickup, dataclassificationdropoff;
    private Instances dataregressionpickup, dataregressiondropoff;
    private Classifier ibkclassificationpickup, ibkclassificationdropoff;
    private Classifier ibkregressionpickup, ibkregressiondropoff;

    // Prevent direct access to class
    private PredictorFunctions () {}

    // Singleton Pattern implementation
    public static PredictorFunctions instance() {
        if (PredictorFunctions.instance == null) {
            PredictorFunctions.instance = new PredictorFunctions ();
        }
        return PredictorFunctions.instance;
    }

    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public void run() throws Exception {
        BufferedReader datafileclassificationpickup = readDataFile( Config.outputPath()+ "DaysPickUpClassification.txt");
        BufferedReader datafileclassificationdropoff = readDataFile( Config.outputPath()+ "DaysDropOffClassification.txt");
        BufferedReader datafileregresssionpickup = readDataFile(Config.outputPath()+ "DaysPickUpRegression.txt");
        BufferedReader datafileregresssiondropoff = readDataFile(Config.outputPath()+ "DaysDropOffRegression.txt");

        dataclassificationpickup = new Instances(datafileclassificationpickup);
        dataclassificationpickup.setClassIndex(dataclassificationpickup.numAttributes() - 1);

        dataclassificationdropoff = new Instances(datafileclassificationdropoff);
        dataclassificationdropoff.setClassIndex(dataclassificationdropoff.numAttributes() - 1);

        dataregressionpickup = new Instances(datafileregresssionpickup);
        dataregressionpickup.setClassIndex(dataregressionpickup.numAttributes() - 1);

        dataregressiondropoff = new Instances(datafileregresssiondropoff);
        dataregressiondropoff.setClassIndex(dataregressiondropoff.numAttributes() - 1);

        System.out.println("KNN classification model");
        ibkclassificationpickup = new IBk(10);
        ibkclassificationpickup.buildClassifier(dataclassificationpickup);
        ibkclassificationdropoff = new IBk(10);
        ibkclassificationdropoff.buildClassifier(dataclassificationdropoff);
        System.out.println("Classification Model Ready");

        System.out.println("KNN regression model");
        ibkregressionpickup = new IBk(10);
        ibkregressionpickup.buildClassifier(dataregressionpickup);
        ibkregressiondropoff = new IBk(10);
        ibkregressiondropoff.buildClassifier(dataregressiondropoff);
        System.out.println("Regression Model Ready");

        instclassificationpickup = new DenseInstance(9);
        instclassificationpickup.setDataset(dataclassificationpickup);
        instclassificationdropoff = new DenseInstance(9);
        instclassificationdropoff.setDataset(dataclassificationdropoff);
        instregressionpickup = new DenseInstance(9);
        instregressionpickup.setDataset(dataregressionpickup);
        instregressiondropoff = new DenseInstance(9);
        instregressiondropoff.setDataset(dataregressiondropoff);
        System.out.println("Models ready");
    }

    public String getPredictionFor(String input) {
        loadattributes(input,instclassificationpickup, instclassificationdropoff, instregressionpickup, instregressiondropoff);

        boolean ishotspotpickup=calcultateifhotspot(ibkclassificationpickup, instclassificationpickup);
        boolean ishotspotdropoff=calcultateifhotspot(ibkclassificationdropoff, instclassificationdropoff);
        int npeoplepickup=calculatenpeople(ibkregressionpickup,instregressionpickup);
        int npeopledropoff=calculatenpeople(ibkregressiondropoff,instregressiondropoff);

        return "{ \"ishotspotpickup\": \"" + String.valueOf(ishotspotpickup) + "\", \"npeoplepickup\": \"" + String.valueOf(npeoplepickup) + "\" , \"npeopleishotspotdropoff\": \"" + String.valueOf(ishotspotdropoff) + "\" , \"npeopledropoff\": \"" + String.valueOf(npeopledropoff) + "\"}";
    }

    public void startManualPrediction() {
        //-73.96841004000001,40.75801568,0:00,false,4.4,Clear,0,0
        while(true){
            String s = this.getInputFeatures();

            loadattributes(s,instclassificationpickup, instclassificationdropoff, instregressionpickup, instregressiondropoff);
            System.out.println("The instance: " + instclassificationpickup);
            boolean ishotspotpickup=calcultateifhotspot(ibkclassificationpickup, instclassificationpickup);
            boolean ishotspotdropoff=calcultateifhotspot(ibkclassificationdropoff, instclassificationdropoff);
            int npeoplepickup=calculatenpeople(ibkregressionpickup,instregressionpickup);
            int npeopledropoff=calculatenpeople(ibkregressiondropoff,instregressiondropoff);

            System.out.println("The predicted class for pickup is: "+ ishotspotpickup);
            System.out.println("The predicted number of people for pickup is: "+ npeoplepickup);
            System.out.println("The predicted class for dropoff is: "+ ishotspotdropoff);
            System.out.println("The predicted number of people for dropoff is: "+ npeopledropoff);

            System.out.print("\nExit? [y|N]:");
            Scanner scan = new Scanner(System.in);
            s = scan.nextLine();
            if(s.equals("y"))
                break;
        }
    }

    private String getInputFeatures () {
        Scanner scan = new Scanner(System.in);
        String s = "";

        System.out.println("Please enter your input variables. Values in brackets [] are default values if input is empty:");

        System.out.print("Latitude: [40.75801568] ");
        String lat = scan.nextLine();
        if ( Objects.equals(lat, "") ) {
            lat = "40.75801568";
        }

        System.out.print("Longitude: [-73.96841004] ");
        String lon = scan.nextLine();
        if ( Objects.equals(lon, "") ) {
            lon = "-73.96841004000001";
        }


        System.out.print("Time: [0:00] ");
        String time = scan.nextLine();
        if ( Objects.equals(time, "") ) {
            time = "0:00";
        }

        System.out.print("Is Holiday: [false] ");
        String holiday = scan.nextLine();
        if ( Objects.equals(holiday, "") ) {
            holiday = "false";
        }

        System.out.print("Mean Temparature: [4.4] ");
        String temparature = scan.nextLine();
        if ( Objects.equals(temparature, "") ) {
            temparature = "4.4";
        }

        System.out.print("Weather: [Clear] ");
        String weather = scan.nextLine();
        if ( Objects.equals(weather, "") ) {
            weather = "Clear";
        }

        System.out.print("Events: [0] ");
        String events = scan.nextLine();
        if ( Objects.equals(events, "") ) {
            events = "0";
        }

        System.out.print("Event Attendees: [0] ");
        String attendees = scan.nextLine();
        if ( Objects.equals(attendees, "") ) {
            attendees = "0";
        }

        s = lon + "," + lat + "," + time + "," + holiday + "," + temparature + "," + weather + "," + events + "," + attendees;
        return s;
    }

    private static void loadattributes(String s, Instance instclassificationpickup, Instance instclassificationdropoff, Instance instregressionpickup, Instance instregressiondropoff) {
        String[] attributes=s.split(",");

        for(int i=0;i<8;i++) {
            if(attributes[i].equals("?")) {
                instclassificationpickup.setMissing(i);
                instclassificationdropoff.setMissing(i);
                instregressionpickup.setMissing(i);
                instregressiondropoff.setMissing(i);
            }else{
                if((i==2)||(i==3)||(i==5)) {
                    instclassificationpickup.setValue(i, attributes[i]);
                    instclassificationdropoff.setValue(i, attributes[i]);
                    instregressionpickup.setValue(i, attributes[i]);
                    instregressiondropoff.setValue(i, attributes[i]);
                }else {
                    instclassificationpickup.setValue(i, Double.parseDouble(attributes[i]));
                    instclassificationdropoff.setValue(i, Double.parseDouble(attributes[i]));
                    instregressionpickup.setValue(i, Double.parseDouble(attributes[i]));
                    instregressiondropoff.setValue(i, Double.parseDouble(attributes[i]));
                }
            }
        }
        instclassificationpickup.setMissing(8);
        instclassificationdropoff.setMissing(8);
        instregressionpickup.setMissing(8);
        instregressiondropoff.setMissing(8);
    }

    private static int calculatenpeople(Classifier ibkregression, Instance instregression) {
        Double predictValue = null;
        try {
            predictValue = ibkregression.classifyInstance(instregression);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return predictValue.intValue();
    }

    private static boolean calcultateifhotspot(Classifier ibkclassification, Instance instclassification) {
        Double predictValue = null;
        try {
            predictValue = ibkclassification.classifyInstance(instclassification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(predictValue==0.0)
            return true;
        else
            return false;
    }
}
