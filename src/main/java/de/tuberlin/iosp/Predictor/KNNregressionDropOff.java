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
import java.util.Scanner;

/**
 * Created by Koma on 04/07/15.
 */
public class KNNregressionDropOff {

    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader datafile = readDataFile(Config.outputPath() + "/DaysDropOffRegression.txt");

        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        System.out.println("KNN regression model");

        Classifier ibk = new IBk(10);
        ibk.buildClassifier(data);

        // Create empty instance with nine attribute values
        Instance inst = new DenseInstance(9);

        inst.setDataset(data);
        System.out.println("Model Ready");
        //-73.96841004000001,40.75801568,0:00,false,4.4,Clear,0,0
        //-73.9795955,40.745498569999995,0:00,false,4.4,Clear,0,0
    while(true){
        Scanner scan = new Scanner(System.in);
        String s = scan.next();
        if(s.equals("exit"))
            break;

        String[] attributes=s.split(",");

        for(int i=0;i<8;i++) {
            if(attributes[i].equals("?"))
                inst.setMissing(i);
            else{
                if((i==2)||(i==3)||(i==5))
                    inst.setValue(i, attributes[i]);
                else
                    inst.setValue(i, Double.parseDouble(attributes[i]));
            }
        }

        inst.setMissing(8);
        System.out.println("The instance: " + inst);
        Double predictValue = ibk.classifyInstance(inst);
        System.out.println("The predicted value is: " + Math.round(predictValue));
    }
        System.out.println("Bye");
    }
}
