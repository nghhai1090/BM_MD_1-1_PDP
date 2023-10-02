package GeneticAlgo;

import java.util.ArrayList;

/**
 * Class contains information of a gene, which is also a route
 */
class Gene {
    /**
     * depot of route
     */
    private int depot;
    /**
     * Index of vehicle used in this route
     */
    private int vehicleIndex;
    /**
     * deploy cost (not used)
     */
    private int deployCost;
    /**
     * indices of pickup and delivery requests served in this route
     */
    private ArrayList<Integer> transportsIndicesList;
    /**
     * visited nodes in this routes in their orders of visiting
     */
    private ArrayList<TransportNode> route;
    /**
     * total time of this route
     */
    private int totalTime;
    /**
     * total maut price of this route
     */
    private int totalMautKm;

    /**
     * constructor
     * @param depot depot of vehicle used in route
     * @param vehicleIndex index of vehicle used in route
     * @param deployCost no used
     */
    public Gene(int depot, int vehicleIndex, int deployCost) {
        this.depot = depot;
        this.vehicleIndex = vehicleIndex;
        this.deployCost = deployCost;
        this.transportsIndicesList = new ArrayList<>();
        this.route = new ArrayList<>();
        this.totalMautKm = -1;
        this.totalTime = -1;
    }

    /**
     * Method to get the route's depot
     * @return the depot
     */
    public int getDepot() {
        return depot;
    }

    /**
     * Method to get vehicle's index used in route
     * @return the index of vehicle
     */

    public int getVehicleIndex() {
        return vehicleIndex;
    }

    /**
     * Method to get the indices of pickup and delivery requests served in this route
     * @return the indices of pickup and delivery requests served in this route
     */
    public ArrayList<Integer> getTransportsIndicesList() {
        return transportsIndicesList;
    }

    /**
     * Method to set new List of indices of pickup and delivery requests served in this route
     * @param transportsCodesList new List of indices of pickup and delivery requests served in this route
     */
    public void setTransportsIndicesList(ArrayList<Integer> transportsCodesList) {
        for(int i = 0 ; i < transportsCodesList.size() ; i++) {
            this.transportsIndicesList.add(transportsCodesList.get(i));
        }
    }

    /**
     * Method to add new index of pickup and delivery request to list of served pickup and delivery of this route
     * @param transportCode new index of pickup and delivery request to be served in this route
     */
    public void addToTransportsIndicesList(int transportCode) {
        this.transportsIndicesList.add(transportCode);
    }

    /**
     * Method to get the visited nodes in route in their order of visiting
     * @return List of visited nodes in route in their order of visiting
     */
    public ArrayList<TransportNode> getRoute() {
        return route;
    }

    /**
     * Method to set the new visited nodes in route in their order of visiting
     * @param route new List of visited nodes in route in their order of visiting
     */
    public void setRoute(ArrayList<TransportNode> route) {
        this.route = route;
    }

    /**
     * Method to get the total time of route
     * @return total time of route
     */
    public int getTotalTime() {
        return totalTime;
    }

    /**
     * Method to set total time of route
     * @param totalTime new total time of route
     */
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * Not used
     * @return not used
     */
    public int getDeployCost() {
        return deployCost;
    }

    /**
     * Method to set depot
     * @param depot the depot
     */
    public void setDepot(int depot) {
        this.depot = depot;
    }

    /**
     * Method to set vehicle index
     * @param vehicleIndex the vehicle index
     */
    public void setVehicleIndex(int vehicleIndex) {
        this.vehicleIndex = vehicleIndex;
    }

    /**
     * not used
     * @param deployCost not used
     */
    public void setDeployCost(int deployCost) {
        this.deployCost = deployCost;
    }

    /**
     * Method to get total maut price of route
     * @return the total maut price
     */
    public int getTotalMautKm() {
        return totalMautKm;
    }

    /**
     * MEthod to set the new total maut price of route
     * @param totalMautKm new total maut price
     */
    public void setTotalMautKm(int totalMautKm) {
        this.totalMautKm = totalMautKm;
    }

    /**
     * Method to create a clone of this gene
     * @return a clone of this gene
     */
    public Gene clone() {
        Gene clone = new Gene(this.depot,this.vehicleIndex,this.getDeployCost());
        clone.setTransportsIndicesList((ArrayList<Integer>) this.getTransportsIndicesList().clone());
        clone.setRoute((ArrayList<TransportNode>) this.getRoute().clone());
        return clone;
    }
}
