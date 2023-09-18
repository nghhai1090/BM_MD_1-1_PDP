package GurobiSolver;

import Model.ObjectivesPoint;
import Model.Transport;
import Model.Vehicle;
import Service.HelperService;
import gurobi.*;

import java.util.ArrayList;
import java.util.HashMap;

public class DAR3Index {
    /**
     * Helper Service to create instance's lists  of classes Vehicle, Transport and caculate the ride time
     */
    private final HelperService helperService = new HelperService();

    /**
     * Class to solve the problem through a 3Index-Formular with Gurobi MILP Solver
     */
    public DAR3Index() {
    }

    public ArrayList<ObjectivesPoint> solveDAR3IndexMultipleObjectives(int[][] distanceMatrix, Transport[] transportsArray, Vehicle[] vehiclesArray, int[][] mautKmMatrix, int timeLimit) {

        ArrayList<ObjectivesPoint> paretoFront = new ArrayList<>();
        int initTimeLimit = timeLimit;
        try {
            int startTime = (int) (System.currentTimeMillis() / 1000);
            int currentTime;

            ObjectivesPoint firstParetoPoint = solveDAR3Index(distanceMatrix, transportsArray, vehiclesArray, mautKmMatrix, timeLimit, 1, 0, Integer.MAX_VALUE);
            paretoFront.add(firstParetoPoint);

            currentTime = (int) (System.currentTimeMillis() / 1000);
            timeLimit = timeLimit - (currentTime - startTime);

            System.out.print("MILP EXECUTED "+Math.max(10,((Math.max(initTimeLimit-timeLimit,0)/initTimeLimit)*100))+" %");
            System.out.print("\r");
            if(timeLimit <= 0) {
                System.out.print("MILP FINISHED");
                return helperService.getFirstFront(paretoFront);
            }

            startTime = (int) (System.currentTimeMillis() / 1000);

            double upperBoundSecondObj = firstParetoPoint.getY();
            double lowerBoundSecondObj = 0;

            while(Math.abs(upperBoundSecondObj - lowerBoundSecondObj)> 0.5) {
                startTime = (int) (System.currentTimeMillis() / 1000);
                ObjectivesPoint paretoPoint = solveDAR3Index(distanceMatrix, transportsArray, vehiclesArray, mautKmMatrix, timeLimit, 1, lowerBoundSecondObj, upperBoundSecondObj);
                paretoFront.add(paretoPoint);
                currentTime = (int) (System.currentTimeMillis() / 1000);
                timeLimit = timeLimit - (currentTime - startTime);
                System.out.print("MILP EXECUTED "+ (20+((double)(Math.max(initTimeLimit-timeLimit,0)/initTimeLimit)*80))+" %");
                System.out.print("\r");
                if(timeLimit <= 0) {
                    System.out.print("MILP FINISHED");
                    return helperService.getFirstFront(paretoFront);
                }
                upperBoundSecondObj = paretoPoint.getY();
            }
            System.out.print("MILP FINISHED");
            return helperService.getFirstFront(paretoFront);
        }
        catch(Exception e) {
            System.out.print("MILP FINISHED");
            return helperService.getFirstFront(paretoFront);
        }
    }

    /**
     * DARP Solver with Gurobi
     *
     * @param distanceMatrix   The distance matix
     * @param mautKmMatrix     The Maut KM Matrix
     * @param timeLimit        Time Limit for the solver, set to negative value if you dont want to use it
     * @param objective        Objective 1: toll km , set tmin tmax Objective 2: time not set tmin tmax
     * @throws GRBException
     */
    public ObjectivesPoint solveDAR3Index(int[][] distanceMatrix, Transport[] transportsArray, Vehicle[] vehiclesArray, int[][] mautKmMatrix, int timeLimit, int objective, double Tmin, double Tmax) throws GRBException {
        int numberOfTransports = transportsArray.length;
        int numOfVehicle = vehiclesArray.length;

        int[][][] transportTimeMatrix = helperService.caculateTransportTimeMatrix(distanceMatrix, vehiclesArray);

        int N = 2 * numberOfTransports;
        int A = N + 2 * numOfVehicle;

        HashMap<Integer, Integer> nodeToDistanceMap = mapNodeToDistance(vehiclesArray, transportsArray);

        GRBEnv env = new GRBEnv();
        GRBModel model = new GRBModel(env);

        GRBVar[][][] Xijk = new GRBVar[A][A][numOfVehicle];
        GRBVar[][] Lik = new GRBVar[A][numOfVehicle];
        GRBVar[][] Tik = new GRBVar[A][numOfVehicle];
        GRBVar[] wi = new GRBVar[A];
        GRBVar maxTime;
        if(objective == 1) {
            maxTime = model.addVar(Tmin, Tmax-1, 1.0, GRB.CONTINUOUS, "MaxTime"); // obj 1 tmin tmax
        }
        else {
            maxTime = model.addVar(0.0, Integer.MAX_VALUE, 1.0, GRB.CONTINUOUS, "MaxTime"); // obj 2 non tmix tmax
        }

        for (int i = 0; i < A; i++) {
            for (int j = 0; j < A; j++) {
                for (int k = 0; k < numOfVehicle; k++) {
                    Xijk[i][j][k] = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, "X_" + i + "_" + j + "_" + k);
                }
            }
        }

        for (int i = 0; i < N / 2; i++) {
            wi[i] = model.addVar(0.0, findActiveTimeOf(i, true, transportsArray), 1.0, GRB.CONTINUOUS, "w_" + i + 1);
        }

        for (int i = N / 2; i < N; i++) {
            wi[i] = model.addVar(0.0, findActiveTimeOf(i, false, transportsArray), 1.0, GRB.CONTINUOUS, "w_" + i + 1);
        }

        for (int i = N; i < A; i++) {
            wi[i] = model.addVar(0.0, 0.0, 1.0, GRB.CONTINUOUS, "w_" + i + 1);
        }

        for (int i = 0; i < A; i++) {
            for (int k = 0; k < numOfVehicle; k++) {
                int capVehicleK = vehiclesArray[k].getCap();
                Lik[i][k] = model.addVar(0.0, capVehicleK, 1.0, GRB.INTEGER, "L_" + i + 1 + "_" + k + 1);
                if(objective == 1) {
                    Tik[i][k] = model.addVar(0.0, Tmax-1, 1.0, GRB.CONTINUOUS, "T_" + i + 1 + "_" + k + 1);
                }
                else{
                    Tik[i][k] = model.addVar(0.0, Integer.MAX_VALUE, 1.0, GRB.CONTINUOUS, "T_" + i + 1 + "_" + k + 1);
                }
            }
        }

        model.update();


        for (int i = 0; i < numberOfTransports; i++) {
            GRBLinExpr left = new GRBLinExpr();
            for (int k = 0; k < numOfVehicle; k++) {
                for (int j = 0; j < N; j++) {
                    left.addTerm(1.0, Xijk[i][j][k]);
                }
            }
            model.addConstr(left, GRB.EQUAL, 1, "(3)_" + i);
        }

        for (int i = 0; i < numberOfTransports; i++) {
            for (int k = 0; k < numOfVehicle; k++) {
                GRBLinExpr left = new GRBLinExpr();
                GRBLinExpr right = new GRBLinExpr();
                for (int j = 0; j < N; j++) {
                    left.addTerm(1.0, Xijk[i][j][k]);
                    right.addTerm(1.0, Xijk[j][i + numberOfTransports][k]);
                }
                model.addConstr(left, GRB.EQUAL, right, "(4)_" + i + "_" + k);
            }
        }

        for (int k = 0; k < numOfVehicle; k++) {
            GRBLinExpr left = new GRBLinExpr();
            int depot = 2 * numberOfTransports + k;
            for (int j = 0; j < numberOfTransports; j++) {
                left.addTerm(1.0, Xijk[depot][j][k]);
            }
            model.addConstr(left, GRB.LESS_EQUAL, 1, "(5)_" + depot + "_" + k);
        }

        for (int k = 0; k < numOfVehicle; k++) {
            GRBLinExpr left = new GRBLinExpr();
            int depot = 2 * numberOfTransports + k + numOfVehicle;
            for (int i = numberOfTransports; i < 2 * numberOfTransports; i++) {
                left.addTerm(1.0, Xijk[i][depot][k]);
            }
            model.addConstr(left, GRB.LESS_EQUAL, 1, "(6)_" + depot + "_" + k);
        }

        for (int k = 0; k < numOfVehicle; k++) {
            int depotBeginn = 2 * numberOfTransports + k;
            int depotReturn = 2 * numberOfTransports + k + numOfVehicle;
            for (int j = 0; j < N; j++) {
                GRBLinExpr left = new GRBLinExpr();
                GRBLinExpr right = new GRBLinExpr();
                for (int i = 0; i < N; i++) {
                    left.addTerm(1.0, Xijk[i][j][k]);
                    right.addTerm(1.0, Xijk[j][i][k]);
                }
                left.addTerm(1.0, Xijk[depotBeginn][j][k]);
                right.addTerm(1.0, Xijk[j][depotReturn][k]);
                model.addConstr(left, GRB.EQUAL, right, "(7)_" + j + "_" + k);
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int load = 0;
                if (j < N / 2) {
                    load = findLoadOf(j, true, transportsArray); // pickups
                }
                else if (j < N) {
                    load = findLoadOf(j, false, transportsArray); // delivery
                }
                for (int k = 0; k < numOfVehicle; k++) {
                    double transportTime = getTimeBetweenNodeOfVehicle(transportTimeMatrix, i, j, nodeToDistanceMap, k);
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, Tik[i][k]);
                    expr.addConstant(transportTime);
                    expr.addTerm(1.0, wi[j]);
                    expr.addConstant(Math.abs(load)/vehiclesArray[k].getLoadFactor());
                    expr.addTerm(-1.0, Tik[j][k]);
                    model.addGenConstrIndicator(Xijk[i][j][k], 1, expr, GRB.EQUAL, 0.0, "(8a)_" + i + "_" + j + "_" + k);
                }
            }
        }

        for (int k = 0; k < numOfVehicle; k++) {
            int depot = N + k;
            for (int j = 0; j < N / 2; j++) {
                int load = findLoadOf(j, true, transportsArray); // pickups
                double transportTime = getTimeBetweenNodeOfVehicle(transportTimeMatrix, depot, j, nodeToDistanceMap, k);
                GRBLinExpr expr = new GRBLinExpr();
                expr.addConstant(transportTime);
                expr.addTerm(1.0, wi[j]);
                expr.addConstant(Math.abs(load)/vehiclesArray[k].getLoadFactor());
                expr.addTerm(-1.0, Tik[j][k]);
                model.addGenConstrIndicator(Xijk[depot][j][k], 1, expr, GRB.EQUAL, 0.0, "(8b)_" + depot + "_" + j + "_" + k);
            }
        }


        for (int i = 0; i < N; i++) {
            for (int k = 0; k < numOfVehicle; k++) {
                int beginTime = 0;
                int load = 0;
                if (i < (N / 2)) {
                    beginTime = findActiveTimeOf(i, true, transportsArray);
                    load = findLoadOf(i, true, transportsArray);
                } else if (i >= (N / 2)) {
                    beginTime = findActiveTimeOf(i, false, transportsArray);
                    load = findLoadOf(i, false, transportsArray);
                }
                GRBLinExpr right = new GRBLinExpr();
                right.addConstant(beginTime);
                right.addConstant(Math.abs(load)/vehiclesArray[k].getLoadFactor());
                model.addConstr(Tik[i][k], GRB.GREATER_EQUAL, right, "(9)_" + i + "_" + k);
                model.addConstr(Tik[i][k], GRB.LESS_EQUAL, 1000000, "(9)_"+i+"_"+k); // end of time window
            }
        }

        for (int i = 0; i < N / 2; i++) {
            for (int k = 0; k < numOfVehicle; k++) {
                GRBLinExpr left = new GRBLinExpr();
                GRBLinExpr right = new GRBLinExpr();
                double transportTime = getTimeBetweenNodeOfVehicle(transportTimeMatrix, i, numberOfTransports + i, nodeToDistanceMap, k);
                left.addTerm(1.0, Tik[i][k]);
                left.addConstant(transportTime);
                right.addTerm(1.0, Tik[numberOfTransports + i][k]);
                model.addConstr(left, GRB.LESS_EQUAL, right, "(10)_" + i + "_" + k);
            }
        }

        for (int j = 0; j < N; j++) {
            int load = 0;
            if (j < N / 2) {
                load = findLoadOf(j, true, transportsArray);
            } else {
                load = findLoadOf(j, false, transportsArray);
            }
            for (int k = 0; k < numOfVehicle; k++) {
                int depotOfVehicle = 2 * numberOfTransports + k;
                for (int i = 0; i < N; i++) {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, Lik[i][k]);
                    expr.addConstant(load);
                    expr.addTerm(-1.0, Lik[j][k]);
                    model.addGenConstrIndicator(Xijk[i][j][k], 1, expr, GRB.EQUAL, 0.0, "(11)_" + i + "_" + j + "_" + k);

                }// pick up load +, delivery load -
                GRBLinExpr expr = new GRBLinExpr();
                expr.addConstant(load);
                expr.addTerm(-1.0, Lik[j][k]);
                model.addGenConstrIndicator(Xijk[depotOfVehicle][j][k], 1, expr, GRB.EQUAL, 0.0, "(11)_" + depotOfVehicle + "_" + j + "_" + k);

            }// depot load 0, first pick up load +
        }



        for (int i = 0; i < N / 2; i++) {
            for (int k = 0; k < numOfVehicle; k++) {
                GRBLinExpr right = new GRBLinExpr();
                right.addConstant(vehiclesArray[k].getCap());
                int loadDelivery = findLoadOf(numberOfTransports + i, false, transportsArray);
                right.addConstant(loadDelivery);
                model.addConstr(Lik[numberOfTransports + i][k], GRB.LESS_EQUAL, right, "(12)_" + i + "_" + k);
            }
        }

        for (int k = 0; k < numOfVehicle; k++) {
            model.addConstr(Lik[2 * numberOfTransports + k][k], GRB.EQUAL, 0.0, "(13)_" + 2 * numberOfTransports + k + "_" + k);
        }

        for (int i = N/2; i < N; i++) {
            for (int k = 0; k < numOfVehicle; k++) {
                int endDepot = N + numOfVehicle + k;
                GRBLinExpr left = new GRBLinExpr();
                double transportTime = getTimeBetweenNodeOfVehicle(transportTimeMatrix, i, endDepot, nodeToDistanceMap, k);
                left.addTerm(1.0, Tik[i][k]);
                left.addConstant(transportTime);
                left.addTerm(-1.0,maxTime);
                model.addGenConstrIndicator(Xijk[i][endDepot][k],1,left, GRB.LESS_EQUAL,0.0, "MaxTime_Of_delivery_" + (i - N / 2));
            }
        }

        for(int i = 0 ; i < A ; i++) {
            for(int j = 0 ; j < A ; j++) {
                for(int k = 0 ; k < numOfVehicle ;k++) {
                    GRBLinExpr expr = new GRBLinExpr();

                    for(int k1 = 0 ; k1 < numOfVehicle ; k1++) {
                        if(k1!=k) {
                            expr.addTerm(1,Xijk[i][j][k1]);
                        }
                    }
                    model.addGenConstrIndicator(Xijk[i][j][k],1,expr,GRB.EQUAL,0,"");
                }
            }
        }

        model.update();

        GRBLinExpr obj1 = new GRBLinExpr();
        for (int i = 0; i < A; i++) {
            for (int j = 0; j < A; j++) {
                for (int k = 0; k < numOfVehicle; k++) {
                    int mautKm = getDistanceBetweenNode(mautKmMatrix, i, j, nodeToDistanceMap);
                    obj1.addTerm(mautKm, Xijk[i][j][k]);
                }
            }
        } // minmautKm

        GRBLinExpr obj2 = new GRBLinExpr();
        obj2.addTerm(1.0, maxTime); // min MaxTime

        if (timeLimit > 0) {
            model.set(GRB.DoubleParam.TimeLimit, timeLimit);
        }


        model.set(GRB.IntParam.OutputFlag,0); // disable log

        if(objective==1) {
            model.setObjective(obj1);
            model.optimize();
            double maxtime = 0.0;
            for (int i = N/2; i < N; i++) {
                for (int k = 0; k < numOfVehicle; k++) {
                    int endDepot = N + numOfVehicle + k;
                    double transportTime = getTimeBetweenNodeOfVehicle(transportTimeMatrix, i, endDepot, nodeToDistanceMap, k);
                    if(Xijk[i][endDepot][k].get(GRB.DoubleAttr.X) != 0.0) {
                        maxtime = Math.max(maxtime,Tik[i][k].get(GRB.DoubleAttr.X) + transportTime);
                    }
                }
            }
            return new ObjectivesPoint(obj1.getValue(),maxtime);
        }
        else {
            model.setObjective(obj2);
            model.optimize();
            double max = 0.0;
            for (int k = 0; k < numOfVehicle; k++) {
                for(int j = 0 ; j < A ; j++) {
                    for (int i = 0; i < A; i++) {
                        if(Xijk[i][j][k].get(GRB.DoubleAttr.X)!=0.0) {
                            if(i==N+k) {System.out.println("START DEPOT");}
                            if(j==N+numOfVehicle+k) {System.out.println("END DEPOT");}
                            System.out.println("from = "+nodeToDistanceMap.get(i)+"; to = "+nodeToDistanceMap.get(j)+"; k = "+k+"; leaving "+nodeToDistanceMap.get(i)+" at "+Tik[i][k].get(GRB.DoubleAttr.X)+"; arrive "+nodeToDistanceMap.get(j)+" at "+Tik[i][k].get(GRB.DoubleAttr.X)+getTimeBetweenNodeOfVehicle(transportTimeMatrix,i,j,nodeToDistanceMap,k));
                            max = Math.max(max,Tik[i][k].get(GRB.DoubleAttr.X)+getTimeBetweenNodeOfVehicle(transportTimeMatrix,i,j,nodeToDistanceMap,k));
                        }
                    }
                }

                System.out.println();
            }

            System.out.println("OBJ"+obj2.getValue());
            System.out.println(max);
            return new ObjectivesPoint(obj1.getValue(),obj2.getValue());
        }

    }

    /**
     * Method to create a Hashmap to map the nodes in 3Index-Formular to indices in distance matrix.
     * Nodes in 3Index-Formular :
     * first N nodes are pickup nodes of transports 1...N
     * , next N nodes are delivery nodes of transports 1...N
     * , next M nodes are start depot nodes of vehicles 1...M
     * , last M nodes are end depot nodes of vehicles 1...M.
     * The Hashmap has following informations:
     * KEY - node in 3Index-Formular,
     * VALUE - index in distance matrix.
     *
     * @param vehiclesArray   The array of vehicles
     * @param transportsArray The array of transports
     * @return A Hashmap which maps the nodes in 3Index-Formular to indices in distance matrix
     */
    private static HashMap<Integer, Integer> mapNodeToDistance(Vehicle[] vehiclesArray, Transport[] transportsArray) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < transportsArray.length; i++) {
            map.put(i, transportsArray[i].getFrom());
            map.put(transportsArray.length + i, transportsArray[i].getTo());
        }
        int key = map.size();
        for (int i = 0; i < vehiclesArray.length; i++) {
            int depot = vehiclesArray[i].getDepot();
            map.put(key, depot);
            map.put(key + vehiclesArray.length, depot);
            key++;
        }
        return map;
    }

    /**
     * Method to find active time of a transport according to a node
     *
     * @param node           The node which belongs to this transport in the 3Index-Formular
     * @param isPickUp     Boolean which says if the node is a delivery node (true) or pickup node (false)
     * @param transportsArray The array of transports
     * @return Active time of a transport
     */
    private static int findActiveTimeOf(int node, boolean isPickUp, Transport[] transportsArray) {
        if (isPickUp) {
            return transportsArray[node].getActiveTimeOfPickup();
        } else {
            return transportsArray[node - transportsArray.length].getActiveTimeOfDelivery();
        }
    }

    /**
     * Method to find amount of transport according to a node
     *
     * @param node           The node which belongs to this transport in the 3Index-Formular
     * @param isPickUp     Boolean which says if the node is a delivery node (true) or pickup node (false)
     * @param transportsArray The array of transports
     * @return Load of a transport, positive if isDelivery is false, negative if isDelivery is true
     */
    private static int findLoadOf(int node, boolean isPickUp, Transport[] transportsArray) {
        if (isPickUp) {
            return transportsArray[node].getAmount();
        } else {
            return -transportsArray[node - transportsArray.length].getAmount();
        }
    }

    /**
     * Method to retrieve distance between 2 nodes in the 3Index-Formular
     *
     * @param distanceMatrix The distance matrix, which is used to retrieve distance
     * @param from           Start node
     * @param to             End node
     * @param map            Node-distance Hashmap
     * @return Distance between 2 nodes in the 3Index-Formular
     */
    private static int getDistanceBetweenNode(int[][] distanceMatrix, int from, int to, HashMap<Integer, Integer> map) {
        return distanceMatrix[map.get(from)][map.get(to)];
    }

    /**
     * Method to retrieve ride time between 2 nodes in the 3Index-Formular
     *
     * @param transportTimeMatrix The time matrix, which is used to retrieve ride time
     * @param from                Start node
     * @param to                  End node
     * @param map                 Node-distance Hashmap
     * @param vehicleNumber       The index of vehicle which will ride between 2 nodes
     * @return Ride time between 2 nodes in the 3Index-Formular
     */
    private static double getTimeBetweenNodeOfVehicle(int[][][] transportTimeMatrix, int from, int to, HashMap<Integer, Integer> map, int vehicleNumber) {
        return transportTimeMatrix[vehicleNumber][map.get(from)][map.get(to)];
    }

}

