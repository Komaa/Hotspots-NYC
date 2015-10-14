package de.tuberlin.iosp.Predictor;

import de.tuberlin.iosp.Detector.Config;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Koma on 04/07/15.
 */
public class KNNclassificationDropOffCrossValidation {

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

    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
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

        float recall0=0, precision0=0, f1score0=0, matthewsCorrelation=0;
        float recall1=0, precision1=0, f1score1=0, numberistance=0;
        BufferedReader datafile = readDataFile(Config.outputPath() + "/DaysDropOffClassification.txt");

        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        // Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, 10);

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        // UseKNN model
        Classifier ibk = new IBk(10);
        System.out.println("KNN");
        // Run for model
        // Collect every group of predictions for current model in a FastVector
        FastVector predictions = new FastVector();

        // For each training-testing split pair, train and test the classifier
        for (int i = 0; i < trainingSplits.length; i++) {
            Evaluation validation = classify(ibk, trainingSplits[i], testingSplits[i]);
            predictions.appendElements(validation.predictions());
            precision0+=validation.precision(0);
            precision1+=validation.precision(1);
            recall0+=validation.recall(0);
            recall1+=validation.recall(1);
            f1score0+=validation.fMeasure(0);
            f1score1+=validation.fMeasure(1);
            matthewsCorrelation+=validation.matthewsCorrelationCoefficient(0);
            numberistance+=validation.numInstances();
            System.out.println(i + " -----"+ibk.toString());
        }
        System.out.println("Number of instances: " + Math.round(numberistance)+"\n");

        System.out.println("Precision class true: " + String.format("%.2f", precision0 / 10));
        System.out.println("Recall class true: " + String.format("%.2f",recall0/10));
        System.out.println("F1-score class true: " + String.format("%.2f",f1score0/10) + "\n");

        System.out.println("Precision class false: " + String.format("%.2f",precision1/10));
        System.out.println("Recall class false: " + String.format("%.2f",recall1/10));
        System.out.println("F1-score class false: " + String.format("%.2f",f1score1/10) + "\n");

        System.out.println("Matthews Correlation Coefficient: " + String.format("%.2f",matthewsCorrelation/10));
        // Calculate overall accuracy of current classifier on all splits
        double accuracy = calculateAccuracy(predictions);
        System.out.println("Overral Accuracy of KNN(10): "+ String.format("%.2f%%", accuracy) + "\n-----------------------");

    }
}
