package Service;

import Model.ChromosomeKeyObjectiveValue;
import Model.ObjectivesPoint;
import Model.Transport;
import Model.Vehicle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class HelperService {

    /**
     * Method to create a array of instances Vehicle class according to input vehicle matrix
     *
     * @param vehiclesMatrix Matrix which stores informations of vehicles
     *                       .Info : (code,depot,capacity,deploy cost,speed,load factor)
     * @return array of instances Vehicle class according to input vehicle matrix
     */
    public Vehicle[] createVehiclesArray(int[][] vehiclesMatrix) {
        Vehicle[] vehiclesArray= new Vehicle[vehiclesMatrix.length];

        for (int i = 0; i < vehiclesMatrix.length; i++) {
            Vehicle vehicle = new Vehicle(vehiclesMatrix[i][0], vehiclesMatrix[i][1], vehiclesMatrix[i][2], vehiclesMatrix[i][3]);
            vehiclesArray[i] = vehicle;
        }

        return vehiclesArray;
    }

    /**
     * Method to create an array of instances Transport class according to the input transports matrix.
     *
     * @param transportsMatrix Matrix which stores informations of transports
     *                         .Info : (pickup, delivery, active time of pickup, active time of delivery, transports amount)
     * @return array of instances Transport class according to the input transports matrix
     */

    public Transport[] createTransportsArray(int[][] transportsMatrix) {
        Transport[] transportsArray = new Transport[transportsMatrix.length];

        for (int i = 0; i < transportsMatrix.length; i++) {
            Transport transport = new Transport(transportsMatrix[i][0], transportsMatrix[i][1], transportsMatrix[i][2], transportsMatrix[i][3], transportsMatrix[i][4]);
            transportsArray[i] = transport;
            transportsArray[i].setCode(i);
        }

        return transportsArray;
    }


    /**
     * Method to create a time matrix, which stores ride time between 2 points in distance matrix of vehicles
     *
     * @param distanceMatrix distance matrix
     * @param vehiclesList   vehicles array
     * @return time matrix, which stores ride time between 2 points in distance matrix of vehicles
     */
    public int[][][] caculateTransportTimeMatrix(int[][] distanceMatrix, Vehicle[] vehiclesList) {
        int[][][] transportTime = new int[vehiclesList.length][distanceMatrix.length][distanceMatrix.length];

        for (int k = 0; k < vehiclesList.length; k++) {
            int speedOfK = vehiclesList[k].getSpeed();
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    int transportTimeIToJ = distanceMatrix[i][j] / speedOfK;
                    transportTime[k][i][j] = transportTimeIToJ;
                }
            }
        }

        return transportTime;
    }

    public ArrayList<Integer> generateNRandomNumberBetween(int up, int low, int num) {
        ArrayList<Integer> numList = new ArrayList<>();
        if(low > up) {
            throw new RuntimeException("low"+low+"up"+up);
        }
        while(low<=up) {
            numList.add(low);
            low++;
        }
        Collections.shuffle(numList);
        return new ArrayList<Integer>( numList.subList(0,Math.min(num+1,numList.size())));
    }

    public ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> nonDominanceSort(ArrayList<ChromosomeKeyObjectiveValue> population) {
        ArrayList<Integer>[] submissiveSetsArray = new ArrayList[population.size()];
        int[] individualDominationCountArray = new int[population.size()];
        ArrayList<Integer>[] frontsArray = new ArrayList[population.size()+1];
        for(int i = 0 ; i < population.size()+1 ; i++) {
            frontsArray[i] = new ArrayList<>();
        }
        for(int i = 0 ; i < population.size() ; i++) {
            submissiveSetsArray[i] = new ArrayList<>();
            individualDominationCountArray[i] = 0;
            for(int j = 0 ; j < population.size() ; j++) {
                if(i!=j) {
                    if(population.get(i).getObjectivesPoint().isDominance(population.get(j).getObjectivesPoint())) {
                        submissiveSetsArray[i].add(j);
                    }
                    else if (population.get(j).getObjectivesPoint().isDominance(population.get(i).getObjectivesPoint())) {
                        individualDominationCountArray[i] = individualDominationCountArray[i] + 1;
                    }
                }
            }
            if(individualDominationCountArray[i]== 0) {
                frontsArray[0].add(i);
            }
        }
        int i = 0;
        ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> result = new ArrayList<>();
        while(frontsArray[i].size()!=0) {
            ArrayList<Integer> individualsNextFrontList = new ArrayList<>();
            ArrayList<ChromosomeKeyObjectiveValue> front = new ArrayList<>();
            for(int j = 0 ; j < frontsArray[i].size() ;j++) {
                int index = frontsArray[i].get(j);
                for(int h = 0 ; h < submissiveSetsArray[index].size() ; h++ ) {
                    int subIndex = submissiveSetsArray[index].get(h);
                    individualDominationCountArray[subIndex] = individualDominationCountArray[subIndex] -1 ;
                    if(individualDominationCountArray[subIndex] == 0) {
                        individualsNextFrontList.add(subIndex);
                    }
                }
                front.add(population.get(frontsArray[i].get(j)));
            }
            result.add(front);
            i = i +1;
            frontsArray[i].addAll(individualsNextFrontList);
        }
        return result;
    }

    public ArrayList<ObjectivesPoint> getFirstFront(ArrayList<ObjectivesPoint> paretoFront) {
        ArrayList<Integer>[] submissiveSetsArray = new ArrayList[paretoFront.size()];
        int[] individualDominationCountArray = new int[paretoFront.size()];
        ArrayList<Integer>[] frontsArray = new ArrayList[paretoFront.size()+1];
        for(int i = 0 ; i < paretoFront.size()+1 ; i++) {
            frontsArray[i] = new ArrayList<>();
        }
        for(int i = 0 ; i < paretoFront.size() ; i++) {
            submissiveSetsArray[i] = new ArrayList<>();
            individualDominationCountArray[i] = 0;
            for(int j = 0 ; j < paretoFront.size() ; j++) {
                if(i!=j) {
                    if(paretoFront.get(i).isDominance(paretoFront.get(j))) {
                        submissiveSetsArray[i].add(j);
                    }
                    else if (paretoFront.get(j).isDominance(paretoFront.get(i))) {
                        individualDominationCountArray[i] = individualDominationCountArray[i] + 1;
                    }
                }
            }
            if(individualDominationCountArray[i]== 0) {
                frontsArray[0].add(i);
            }
        }
        int i = 0;
        ArrayList<ArrayList<ObjectivesPoint>> result = new ArrayList<>();
        while(frontsArray[i].size()!=0) {
            ArrayList<Integer> individualsNextFrontList = new ArrayList<>();
            ArrayList<ObjectivesPoint> front = new ArrayList<>();
            for(int j = 0 ; j < frontsArray[i].size() ;j++) {
                int index = frontsArray[i].get(j);
                for(int h = 0 ; h < submissiveSetsArray[index].size() ; h++ ) {
                    int subIndex = submissiveSetsArray[index].get(h);
                    individualDominationCountArray[subIndex] = individualDominationCountArray[subIndex] -1 ;
                    if(individualDominationCountArray[subIndex] == 0) {
                        individualsNextFrontList.add(subIndex);
                    }
                }
                front.add(paretoFront.get(frontsArray[i].get(j)));
            }
            result.add(front);
            i = i +1;
            frontsArray[i].addAll(individualsNextFrontList);
        }
        return result.size()>0 ? result.get(0) : new ArrayList<>();
    }

    public HashMap<Integer,Double> assignCrowdingDistance(ArrayList<ChromosomeKeyObjectiveValue> front) {
        HashMap<Integer, Double> crowdingDistancesMap = new HashMap<>();
        for (int i = 0; i < front.size(); i++) {
            crowdingDistancesMap.put(front.get(i).getChromosomeKey(), 0.0);
        }

        front.sort(Comparator.comparingDouble(a -> a.getObjectivesPoint().getX()));
        crowdingDistancesMap.put(front.get(0).getChromosomeKey(),Double.MAX_VALUE);
        crowdingDistancesMap.put(front.get(front.size()-1).getChromosomeKey(),Double.MAX_VALUE);
        for(int i = 1; i < front.size()-1; i++) {
            double newDistance = Double.MAX_VALUE;
            if(crowdingDistancesMap.get(front.get(i).getChromosomeKey())!=Double.MAX_VALUE) {
                newDistance = crowdingDistancesMap.get(front.get(i).getChromosomeKey()) +
                        (front.get(i+1).getObjectivesPoint().getX() - front.get(i-1).getObjectivesPoint().getX() )
                                / (front.get(front.size()-1).getObjectivesPoint().getX() - front.get(0).getObjectivesPoint().getX());
            }
            crowdingDistancesMap.put(front.get(i).getChromosomeKey(),newDistance);
        }

        front.sort(Comparator.comparingDouble(a -> a.getObjectivesPoint().getY()));
        crowdingDistancesMap.put(front.get(0).getChromosomeKey(),Double.MAX_VALUE);
        crowdingDistancesMap.put(front.get(front.size()-1).getChromosomeKey(),Double.MAX_VALUE);
        for(int i = 1; i < front.size()-1; i++) {
            double newDistance = Double.MAX_VALUE;
            if(crowdingDistancesMap.get(front.get(i).getChromosomeKey())!=Double.MAX_VALUE) {
                newDistance = crowdingDistancesMap.get(front.get(i).getChromosomeKey()) +
                        (front.get(i+1).getObjectivesPoint().getY() - front.get(i-1).getObjectivesPoint().getY() )
                                / (front.get(front.size()-1).getObjectivesPoint().getY() - front.get(0).getObjectivesPoint().getY());
            }
            crowdingDistancesMap.put(front.get(i).getChromosomeKey(),newDistance);
        }

        return crowdingDistancesMap;
    }

    public double caculateHypervolume(ArrayList<ObjectivesPoint> objectivesPointArrayList, ObjectivesPoint referencePoint) {
        objectivesPointArrayList.sort(Comparator.comparingDouble(ObjectivesPoint::getX));
        double sum = 0.0;
        for(int i = 1 ; i < objectivesPointArrayList.size() ; i++) {
            double val = (referencePoint.getX()-objectivesPointArrayList.get(i).getX())*(objectivesPointArrayList.get(i-1).getY() - objectivesPointArrayList.get(i).getY());
            sum = sum + val;
        }
        try {
            double val = (referencePoint.getX()-objectivesPointArrayList.get(0).getX())*(referencePoint.getY()-objectivesPointArrayList.get(0).getY()) + sum;
            return val;
        }
        catch (Exception e) {
            return -1;
        }
    }

    public ObjectivesPoint caculateReferencePoint(ArrayList<ObjectivesPoint> objectivesPointArrayList) {
        final double[] x = {0.0};
        final double[] y = {0.0};
        objectivesPointArrayList.forEach( p -> {
            x[0] = Math.max(p.getX(), x[0]);
            y[0] = Math.max(p.getY(), y[0]);
        });
        return new ObjectivesPoint(x[0]+100,y[0]+100);
    }


    public double caculateDistanceToCenter(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

}
