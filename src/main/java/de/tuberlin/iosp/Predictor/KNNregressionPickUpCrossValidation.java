package de.tuberlin.iosp.Predictor;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.tuberlin.iosp.Detector.Config;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instances;

/**
 * Created by Koma on 02/07/15.
 */
public class KNNregressionPickUpCrossValidation {
    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static Evaluation classify(Classifier model,
                                      Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

    public static void main(String[] args) throws Exception {

        float meanAbsError=0, rootMeanSquareError=0,correlationCoefficent=0;
        float correct=0, incorrect=0;
        BufferedReader datafile = readDataFile(Config.outputPath() + "/DaysPickUpRegression.txt");

        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        // Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, 10);

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        // Use KNN classifier
        Classifier ibk = new IBk(10);
        System.out.println("KNN");

        // Run for model
        // Collect every group of predictions for current model in a FastVector
        FastVector predictions = new FastVector();

        // For each training-testing split pair, train and test the classifier
        for (int i = 0; i < trainingSplits.length; i++) {
            Evaluation validation = classify(ibk, trainingSplits[i], testingSplits[i]);
            predictions.appendElements(validation.predictions());
            meanAbsError+=validation.meanAbsoluteError();
            rootMeanSquareError+=validation.rootMeanSquaredError();
            correlationCoefficent+=validation.correlationCoefficient();

            System.out.println(i + " -----"+ibk.toString());
        }

        System.out.println("Mean Absolute Error: " + meanAbsError/10);
        System.out.println("Root Mean Square Error: " + rootMeanSquareError/10);
        System.out.println("Correlation Coefficient: " + correlationCoefficent/10);

    }
}
