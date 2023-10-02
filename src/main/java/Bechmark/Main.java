package Bechmark;

import GeneticAlgo.GeneticAlgorithm;
import GurobiSolver.DAR3Index;
import Model.ObjectivesPoint;
import Model.Transport;
import Model.Vehicle;
import Service.HelperService;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final GeneticAlgorithm ga = new GeneticAlgorithm();
    private static final DAR3Index dar3Index = new DAR3Index();

    /**
     * Main method
     * @param args consists of 7 command line arguments
     *             1. address of distance matrix file
     *             2. address of maut price matrix file
     *             3. address of pickups and deliveries file
     *             4. adress to save result file
     *             5. time limit of each GA run in seconds
     *             6. number of GA runs to be performed
     *             7. time limit of Gurobi Solver, set to -1 to disable Gurobi Solver, or when Gurobi License is not available
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length!=7) {
            System.out.println("Please provide 7 command line arguments.");
            System.out.println("1 distance matrix in txt");
            System.out.println("2 maut preis matrix in txt");
            System.out.println("3 pick-up and deliveries and vehicles info matrix in txt");
            System.out.println("4 destination of result file in txt");
            System.out.println("5 time limit of each GA run");
            System.out.println("6 number of performed Genetic Algorithm runs");
            System.out.println("7 time limit of MILP run");
            return;
        }


        int GAtimeLimit = Integer.parseInt(args[4]);
        int MILPtimeLimit = Integer.parseInt(args[6]);
        int gaRunsLimit = Integer.parseInt(args[5]);
        if(GAtimeLimit<=0 || gaRunsLimit<=0 || MILPtimeLimit<-1) {
            System.out.println("Arguments 3, 4, 5 have to follow the rules:");
            System.out.println("Argument 3 muss be integer bigger than 0");
            System.out.println("Argument 4 muss be integer bigger than 0");
            System.out.println("Argument 5 muss be integer bigger than -2");
            return;
        }
        int[][] distanceMatrix = readCSVFile(args[0]);
        int[][] mautKmMatrix = readCSVFile(args[1]);

        long starttime = System.currentTimeMillis();

        System.out.println();
        System.out.println(" ____   ______  _   _   _____  _    _  __  __            _____   _  __");
        System.out.println("|  _ \\ |  ____|| \\ | | / ____|| |  | ||  \\/  |    /\\    |  __ \\ | |/ / ");
        System.out.println("| |_) || |__   |  \\| || |     | |__| || \\  / |   /  \\   | |__) || ' /  ");
        System.out.println("|  _ < |  __|  | . ` || |     |  __  || |\\/| |  / /\\ \\  |  _  / |  <   ");
        System.out.println("| |_) || |____ | |\\  || |____ | |  | || |  | | / ____ \\ | | \\ \\ | . \\  ");
        System.out.println("|____/ |______||_| \\_| \\_____||_|  |_||_|  |_|/_/    \\_\\|_|  \\_\\|_|\\_\\ ");
        System.out.println("                                                                      ");
        System.out.println("                                                                      ");
        System.out.println("  _____  _______            _____   _______   _____   _  _  _         ");
        System.out.println(" / ____||__   __|    /\\    |  __ \\ |__   __| / ____| | || || |        ");
        System.out.println("| (___     | |      /  \\   | |__) |   | |   | (___   | || || |        ");
        System.out.println(" \\___ \\    | |     / /\\ \\  |  _  /    | |    \\___ \\  | || || |        ");
        System.out.println(" ____) |   | |    / ____ \\ | | \\ \\    | |    ____) | |_||_||_|        ");
        System.out.println("|_____/    |_|   /_/    \\_\\|_|  \\_\\   |_|   |_____/  (_)(_)(_)        ");
        System.out.println();
        System.out.println("SET TIME LIMIT OF EACH GA RUN : " + GAtimeLimit + " SECS");
        System.out.println("SET LIMIT OF GA RUN TO : " + gaRunsLimit + " RUNS");
        System.out.println("SET TIME LIMIT OF MILP RUN : " + MILPtimeLimit + " SECS");
        System.out.println();





        String transportsDataAndVehicles = args[2];
        String saveResultAt = args[3];
        HelperService h = new HelperService();

        BufferedReader reader = new BufferedReader(new FileReader(transportsDataAndVehicles));
        // Read the first line containing n and m
        String[] firstLine = reader.readLine().trim().split(",");
        int n = Integer.parseInt(firstLine[0]);
        int m = Integer.parseInt(firstLine[1]);

        // Initialize the first matrix with n rows and 6 columns
        int[][] pickupsDeliveries = new int[n][6];

        // Read the next n lines and populate the first matrix
        for (int i = 0; i < n; i++) {
            String[] lineData = reader.readLine().trim().split(",");
            for (int j = 0; j < 5; j++) {
                pickupsDeliveries[i][j] = Integer.parseInt(lineData[j]);
            }
        }

        // Initialize the second matrix with m rows and 3 columns
        int[][] vehiclesMatrix = new int[m][4];

        // Read the next m lines and populate the second matrix
        for (int i = 0; i < m; i++) {
            String[] lineData = reader.readLine().trim().split(",");
            vehiclesMatrix[i][0] = i;
            for (int j = 0; j < 3; j++) {
                vehiclesMatrix[i][j + 1] = Integer.parseInt(lineData[j]);
            }
        }

        Vehicle[] vehiclesArray = h.createVehiclesArray(vehiclesMatrix);
        Transport[] transportsArray = h.createTransportsArray(pickupsDeliveries);

        ArrayList<ObjectivesPoint> mergeGA = new ArrayList<>();
        ArrayList<ObjectivesPoint> mergeGAMILP = new ArrayList<>();
        ArrayList<String> GAValues = new ArrayList<>();
        ArrayList<ArrayList<ObjectivesPoint>> gaFronts = new ArrayList<>();

        System.out.println(" __                                                           __              ");
        System.out.println("/ _   _  _   _ |_ .  _    /\\  |  _   _   _ . |_ |_   _   _   (_  |_  _   _ |_ ");
        System.out.println("\\__) (- | ) (- |_ | (_   /--\\ | (_) (_) |  | |_ | ) ||| _)   __) |_ (_| |  |_ ");
        System.out.println("                                _/                                              ");

        for (int i = 0; i < gaRunsLimit; i++) {
            System.out.println("GA RUN NUMBER " + (i + 1) + " STARTS");
            int limit = GAtimeLimit;
            ArrayList<ObjectivesPoint> paretoFront0 = ga.solve(distanceMatrix, mautKmMatrix, vehiclesArray, transportsArray, limit, (i + 1), 0);
            gaFronts.add(paretoFront0);
            mergeGA.addAll(paretoFront0);
            System.out.println();
        }
        ObjectivesPoint refGA = h.caculateReferencePoint(mergeGA);
        double maxValue = 0.0;
        int maxIndex = 0;
        for (int i = 0; i < gaFronts.size(); i++) {
            double value = h.caculateHypervolume(gaFronts.get(i), refGA);
            if (value >= 0) {
                GAValues.add(String.valueOf(value));
            } else {
                GAValues.add("NO VALUE");
            }
            if (value > maxValue) {
                maxValue = value;
                maxIndex = i;
            }
        }


        int limit = MILPtimeLimit;
        ArrayList<ObjectivesPoint> paretoFrontMILP;
        if(limit!=-1) {
            System.out.println();
            System.out.println("            __                       ");
            System.out.println("|\\/| | |   |__)    _ |_  _   _ |_  _ ");
            System.out.println("|  | | |__ |      _) |_ (_| |  |_ _) ");
            System.out.println("                                     ");
            paretoFrontMILP = dar3Index.solveDAR3IndexMultipleObjectives(distanceMatrix, transportsArray, vehiclesArray, mautKmMatrix, limit);
        }
       else {
           paretoFrontMILP = new ArrayList<>();
        }
        mergeGAMILP.addAll(gaFronts.get(maxIndex));
        mergeGAMILP.addAll(paretoFrontMILP);
        ObjectivesPoint refGAMILP = h.caculateReferencePoint(mergeGAMILP);
        double bestGAValue = h.caculateHypervolume(gaFronts.get(maxIndex), refGAMILP);
        String gaV = "";
        double IPValue = h.caculateHypervolume(paretoFrontMILP, refGAMILP);
        String IPVal = "";
        if (bestGAValue >= 0) {
            gaV = String.valueOf(bestGAValue);
        } else {
            gaV = "NO VALUE";
        }
        if (IPValue >= 0) {
            IPVal = String.valueOf(IPValue);
        } else {
            IPVal = "NOVALUE";
        }
        String coverage = "";
        if (bestGAValue >= 0 && IPValue > 0) {
            coverage = String.valueOf((bestGAValue / IPValue) * 100);
        } else if (bestGAValue == IPValue) {
            coverage = "100";
        } else {
            coverage = "COVERAGE INVALID";
        }
        System.out.println();

        System.out.println("FINISHED CALCULATING");

        System.out.println("WRITING RESULTS");

        writeOutput(transportsDataAndVehicles, String.valueOf(GAtimeLimit), GAValues, gaFronts, refGA, IPVal, paretoFrontMILP, refGAMILP, String.valueOf(maxIndex + 1), gaV, coverage, saveResultAt, String.valueOf(MILPtimeLimit));

        System.out.println("BENCHMARK OF FILE "  + transportsDataAndVehicles+" ENDED");


        long endTime = System.currentTimeMillis();

        System.out.println("TAKES " + (endTime - starttime) / 1000 + " SECONDS");
        boolean exit = false;
        while(!exit) {

        }
    }

    /**
     * Method to write benchmark's results to result.txt
     * @param dataName name of pickup deliveries file
     * @param timeLimit time limit of each GA run
     * @param GAValues List contains Hypervolumes of Pareto fronts resulting from GA runs
     * @param GAfronts List contains Pareto fronts resulting from GA runs
     * @param ref1 reference point to calculate Hypervolumes of GAs
     * @param IPValue Hypervolume of pareto front resulting from MILP
     * @param IPfront Pareto front resulting from MILP
     * @param ref2 reference point to calculate Hypervolumes of Pareto fronts resulting from MILP and GA
     * @param bestGAIndex index of GA run gives best pareto front
     * @param bestGAVal value of Hypervolume of best pareto front from GA runs
     * @param coverage % Hypervolume of GA's pareto front over Hypervolume of MILP's pareto front
     * @param resultFile name of result file
     * @param milptime time limit of Gurobi solver
     * @throws IOException
     */
    private static void writeOutput(String dataName, String timeLimit, ArrayList<String> GAValues, ArrayList<ArrayList<ObjectivesPoint>> GAfronts, ObjectivesPoint ref1, String IPValue, ArrayList<ObjectivesPoint> IPfront, ObjectivesPoint ref2, String bestGAIndex, String bestGAVal, String coverage, String resultFile, String milptime) throws IOException {

        FileWriter fileWriter = new FileWriter(resultFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("BENCHMARK RESULT OF DATA " + dataName);
        printWriter.println("NUMBER OF GA RUNS : " + GAfronts.size());
        printWriter.println("TIME LIMIT EACH GA RUN : " + timeLimit);
        for (int i = 0; i < GAfronts.size(); i++) {
            printWriter.println();
            printWriter.println("PARETO FRONT OF GA RUN NUMBER : " + (i + 1));
            printWriter.println("  toll km  \t  max time  ");
            for (int j = 0; j < GAfronts.get(i).size(); j++) {
                printWriter.printf("  %.3f  \t  %.3f%n  ", GAfronts.get(i).get(j).getX(), GAfronts.get(i).get(j).getY());
            }
            printWriter.println();
            printWriter.println("HYPERVOLUME WITH REFERENCE POINT " + ref1.toString() + " IS : " + GAValues.get(i));
        }
        printWriter.println();
        printWriter.println("BEST GA RUN IS RUN NUMBER : " + (bestGAIndex));
        printWriter.println();
        printWriter.println("PARETO FRONT OF MILP");
        printWriter.println("TIME LIMIT OF MILP : " + milptime);
        printWriter.println("  toll km  \t  max time  ");
        for (int j = 0; j < IPfront.size(); j++) {
            printWriter.printf("  %.3f  \t  %.3f%n  ", IPfront.get(j).getX(), IPfront.get(j).getY());
        }
        printWriter.println();
        printWriter.println("REFERENCE POINT TAKEN TO COMPARE RESULTS FROM BEST GA RUN AND MILP : " + ref2.toString());
        printWriter.println("HYPERVOLUME OF MILP : " + IPValue);
        printWriter.println("HYPERVOLUME OF BEST GA RUN : " + bestGAVal);
        printWriter.println("% HYPERVOLUME OF BEST GA RUN OVER MILP : " + coverage);

        printWriter.close();
        fileWriter.close();

    }

    /**
     * Method to read CSV file into matrix of numbers containing information (distance matrix, maut price matrix, pickup and deliveries matrix)
     * @param filePath adress of the file
     * @return matrix
     */
    private static int[][] readCSVFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String[]> rows = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split(",");
                if (values.length != 0 && !line.trim().equals("")) {
                    rows.add(values);
                }
            }
            int numRows = rows.size();
            int numCols = rows.get(0).length;

            int[][] matrix = new int[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                String[] row = rows.get(i);
                for (int j = 0; j < numCols; j++) {
                    matrix[i][j] = Integer.parseInt(row[j]);
                }
            }

            return matrix;
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            return null;
        }
    }
}
