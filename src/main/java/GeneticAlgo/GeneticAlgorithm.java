package GeneticAlgo;

import Model.ChromosomeKeyObjectiveValue;
import Model.ObjectivesPoint;
import Model.Transport;
import Model.Vehicle;
import Service.HelperService;

import java.util.*;

/**
 * Class to perform the GA
 */
public class GeneticAlgorithm {
    /**
     * helper service to do small tasks
     */
    private final HelperService helperService = new HelperService();
    /**
     * the genetic service to perform main task of genetic algorithm
     */
    private GeneticService geneticService = new GeneticService();

    public GeneticAlgorithm() {
    }

    /**
     * Method to solve the pick up and delivery problem with GA
     * @param distanceMatrix the distance matrix
     * @param mautKmMatrix the maut price matrix
     * @param vehiclesArray the array contains vehicles information
     * @param transportsArray the array contains requests information
     * @param timeLimit time limit of each GA run
     * @param runNumber index of this GA run
     * @param option option of mutation, only to test, not important
     * @return the Pareto front
     */
    public ArrayList<ObjectivesPoint> solve(int[][] distanceMatrix, int[][] mautKmMatrix, Vehicle[] vehiclesArray, Transport[] transportsArray, int timeLimit, int runNumber, int option) {
        geneticService.setParameters(distanceMatrix,mautKmMatrix,vehiclesArray,transportsArray);
        return geneticAlgorithm(100,200,0.5,0.5, timeLimit, runNumber,option);
    }

    /**
     * MEthod to perform the GA
     * @param initPopSize initial population size, is set to be 100
     * @param generation number of generations , set to be 50
     * @param initialCrossoverRate cross over rate, set to be 0.5
     * @param initialMutationRate mutation rate, set to be 0.5
     * @param timeLimit time limit of GA
     * @param runNumber index of this GA run
     * @param option not important, only for testing
     * @return the Pareto Front
     */
    public ArrayList<ObjectivesPoint> geneticAlgorithm (int initPopSize, int generation, double initialCrossoverRate, double initialMutationRate, int timeLimit, int runNumber, int option) {
        int initTimeLimit = timeLimit;
        int startTime = (int) (System.currentTimeMillis() / 1000);
        int currentTime;
        ArrayList<Chromosome> population = createInitPopulation(initPopSize);
        currentTime = (int) (System.currentTimeMillis() / 1000);
        timeLimit = timeLimit - (currentTime - startTime);
        System.out.print("GA RUN NUMBER "+runNumber+" EXECUTED "+Math.max(10,((double)(Math.max(initTimeLimit-timeLimit,0)/initTimeLimit)*100))+" %");
        System.out.print("\r");
        int generationCount = 0;
        ArrayList<Chromosome> bestIndividuals = new ArrayList<>();
        ArrayList<ObjectivesPoint> paretoFront = new ArrayList<>();
        while (generationCount <= generation) {
            startTime = (int) (System.currentTimeMillis() / 1000);
            double crossoverRate = initialCrossoverRate;
            double mutationRate = initialMutationRate;
            ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> fronts = doSurvivalSelectionNSGAII(population,initPopSize);
            if(generationCount==generation || timeLimit < 0) {
                ArrayList<ChromosomeKeyObjectiveValue> firstFront = fronts.get(0);
                firstFront.forEach(e-> {
                    bestIndividuals.add(population.get(e.getChromosomeKey()));
                });
                fronts.get(0).forEach(e -> {
                    if(!paretoFront.contains(e.getObjectivesPoint())) {
                        paretoFront.add(e.getObjectivesPoint());
                    }
                });
                //System.out.println(bestIndividuals.get(0).toString());
                //geneticService.caculateTotalTimeOfThisRoutedebug(bestIndividuals.get(0).getGenesList()[0].getVehicleIndex(),bestIndividuals.get(0).getGenesList()[0].getRoute());
                System.out.println("GA RUN NUMBER "+runNumber+" FINISHED !");
                return paretoFront;
            }
            else{
                ArrayList<Chromosome> selectedParents = doCrossOverSelectionNSGAII(population,fronts,initPopSize/2);
                ArrayList<Chromosome> poolList = new ArrayList<>();
                doCrossOver(poolList, selectedParents, initPopSize, crossoverRate);
                doMutation(poolList, mutationRate, option);
                population.addAll(poolList);
            }
            generationCount = generationCount + 1;
            currentTime = (int) (System.currentTimeMillis() / 1000);
            timeLimit = timeLimit - (currentTime - startTime);
            double countExecuted = (double) generationCount/generation;

            System.out.print("GA RUN NUMBER "+runNumber+" EXECUTED "+Math.max((10 + (double)90*countExecuted),((double)(Math.max(initTimeLimit-timeLimit,0)/initTimeLimit)*100))+" %");
            System.out.print("\r");
        }
        return paretoFront;
    }

    /**
     * Method to perform crossover
     * @param poolList the list to save individuals after performed crossover
     * @param selectedParents the selected parents for crossover
     * @param initPopSize size of individuals to be resulting from this crossover (size of pool list)
     * @param crossoverRate crossover rate
     */
    private void doCrossOver( ArrayList<Chromosome> poolList, ArrayList<Chromosome> selectedParents, int initPopSize, double crossoverRate) {
        int numOfChildsFromCrossOver = (int) (initPopSize * crossoverRate);
        ArrayList<Chromosome> childsList1 = crossOverStrategy1(selectedParents, numOfChildsFromCrossOver);
        int numOfChildsCopyFromParents = initPopSize - numOfChildsFromCrossOver;
        ArrayList<Integer> randomIndices = helperService.generateNRandomNumberBetween(selectedParents.size()-1,0,numOfChildsCopyFromParents);
       for(int i = 0 ; i < randomIndices.size() ; i++) {
           poolList.add(selectedParents.get(randomIndices.get(i)));
       }
        poolList.addAll(childsList1);
    }

    /**
     * Method to do survival selection according to NSGAII (non dominance sort -> crowding distance)
     * @param population the population to perform selection
     * @param popSize the number of individuals to be selected from population as survivors
     * @return the survived individuals as pareto fronts. each point in pareto front is a object represents an individual. This object contains key of individual and its fitness value
     */
    public ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> doSurvivalSelectionNSGAII(ArrayList<Chromosome> population, int popSize) {
        ArrayList<ChromosomeKeyObjectiveValue> chromosomeKeyObjectiveValues = new ArrayList<>();
        int selectNumber = popSize;
        ArrayList<Integer> deletedIndices = new ArrayList<>();
        ArrayList<Chromosome> remove = new ArrayList<>();
        for(int i = 0 ; i < population.size() ; i++) {
            chromosomeKeyObjectiveValues.add(new ChromosomeKeyObjectiveValue(i,new ObjectivesPoint(population.get(i).getTotalMautKM(),population.get(i).getMaxTime())));
        }
        ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> fronts = helperService.nonDominanceSort(chromosomeKeyObjectiveValues);
        for(int i = 0 ; i < fronts.size() ; i++) {
            ArrayList<ChromosomeKeyObjectiveValue> front = fronts.get(i);
            if(selectNumber>0) {
                if(selectNumber<front.size()) {
                    HashMap<Integer,Double> crowdingDistancesMap = helperService.assignCrowdingDistance(front);
                    front.sort(Comparator.comparingDouble(a->crowdingDistancesMap.get(a.getChromosomeKey())));
                    Collections.reverse(front);
                    List<ChromosomeKeyObjectiveValue> toRemove = front.subList(selectNumber,front.size());
                    for(int j = 0 ; j < toRemove.size() ; j++) {
                        remove.add(population.get(toRemove.get(j).getChromosomeKey()));
                        deletedIndices.add(toRemove.get(j).getChromosomeKey());
                    }
                    toRemove.clear();
                    selectNumber = 0;
                }
                else {
                    selectNumber = selectNumber - front.size();
                }
            }
            else {
                for(int j = 0 ; j < front.size() ; j++) {
                    remove.add(population.get(front.get(j).getChromosomeKey()));
                    deletedIndices.add(front.get(j).getChromosomeKey());
                }
                fronts.remove(i);
                i--;
            }
        }
        resetingKeysInFronts(population, deletedIndices, fronts);
        return fronts;
    }

    /**
     * Method to reset key of individuals in pareto front after removed dead individuals
     * @param population the population
     * @param deletedIndices the deleted keys ( of not survived individuals)
     * @param fronts the pareto front after reseting the keys
     */
    private static void resetingKeysInFronts(ArrayList<Chromosome> population, ArrayList<Integer> deletedIndices, ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> fronts) {
        Collections.sort(deletedIndices);

        for(int i = 0; i < fronts.size() ; i++) {
            for(int j = 0; j < fronts.get(i).size() ; j++) {
                int key = fronts.get(i).get(j).getChromosomeKey();
                if(!deletedIndices.contains(key)) {
                    boolean reset = false;
                    for(int c = 0; c < deletedIndices.size() ; c++) {
                        if(key < deletedIndices.get(c)) {
                            fronts.get(i).get(j).setChromosomeKey(key - c );
                            reset = true;
                            break;
                        }
                    }
                    if(!reset) {
                        fronts.get(i).get(j).setChromosomeKey(key - deletedIndices.size() );
                    }
                }
            }
        }
        for(int i = 0 ; i < deletedIndices.size() ; i++) {
            population.remove(deletedIndices.get(i)-i);
        }
    }

    /**
     * Method to perfrom crossover selection as in NSGA (random -> compare pareto front levels and crowding distance)
     * @param population the population
     * @param fronts the list of pareto fronts
     * @param crossOverPoolSize the number of parents to be selected
     * @return selected individuals for crossover
     */
    public ArrayList<Chromosome> doCrossOverSelectionNSGAII(ArrayList<Chromosome> population, ArrayList<ArrayList<ChromosomeKeyObjectiveValue>> fronts, int crossOverPoolSize) {

        ArrayList<Chromosome> parentsPool = new ArrayList<>();
        for(int c = 0 ; c < crossOverPoolSize ; c++) {
            ArrayList<Integer> parentIndices = helperService.generateNRandomNumberBetween(population.size()-1,0,2);
            int firstFront = 0;
            int secondFront = 0;
            for(int i = 0 ; i < fronts.size() ; i++) {
                for(int j = 0 ; j < fronts.get(i).size() ; j++) {
                    if(fronts.get(i).get(j).getChromosomeKey() == parentIndices.get(0)) {
                        firstFront = i;
                    }
                    if(fronts.get(i).get(j).getChromosomeKey() == parentIndices.get(1)) {
                        secondFront = i;
                    }
                }
            }

            if(firstFront<secondFront) {parentsPool.add(population.get(parentIndices.get(0)));}
            else  if(firstFront>secondFront) {parentsPool.add(population.get(parentIndices.get(1)));}
            else {
                HashMap<Integer,Double>crowdingDistance = helperService.assignCrowdingDistance(fronts.get(firstFront));
                double firstValue = crowdingDistance.get(parentIndices.get(0));
                double secondValue = crowdingDistance.get(parentIndices.get(1));
                if(firstValue>=secondValue) {
                    parentsPool.add(population.get(parentIndices.get(0)));
                }
                else{
                    parentsPool.add(population.get(parentIndices.get(1)));
                }
            }
        }
        return parentsPool;
    }

    /**
     * Method to do mutation
     * @param poolList the set of individuals to do mutation
     * @param mutationRate muation rate
     * @param option not used
     */
    public void doMutation(ArrayList<Chromosome> poolList, double mutationRate, int option) {
        Random rand = new Random();
        Collections.shuffle(poolList);
        int numOfChildsMutated = (int) (poolList.size() * mutationRate);
        for(int i = 0 ; i < numOfChildsMutated ; i++) {
            Chromosome chromosome = poolList.get(i);
            Chromosome copy = chromosome.clone();
            boolean success = false;

            int count = 0;
            int randomNumber = rand.nextInt(3) + 1;
            if(option == 2) {
                randomNumber = 1;
            }
            if(randomNumber == 1) {
                while(!success && count < 50) {
                    try {
                        count ++;
                        boolean opt = rand.nextBoolean();
                        mutationOfRoute(chromosome,opt);
                        success = true;
                    }
                    catch (Exception e) {
                        chromosome = copy;
                    }
                }
            }
            if(randomNumber == 2) {
                success = false;
                count = 0;
                while(!success && count < 50) {
                    try {
                        count++;
                        mutationOfDepots(chromosome);
                        success = true;
                    }
                    catch (Exception e) {
                        chromosome = copy;
                    }
                }
            }
            if(randomNumber == 3) {
                success = false;
                count = 0;
                while(!success && count < 50) {
                    try {
                        count++;
                        mutationOfTransports(chromosome);
                        success = true;
                    }
                    catch (Exception e) {
                        chromosome = copy;
                    }
                }
            }
        }
        // System.out.println("do mutation succeed on "+countSuccedd+" chromosomes.");
    }

    /**
     * Perform route mutation of individual
     * @param chromosome the individual
     * @param toOptimize is this mutation goal to better the fitness avlue of individual ?
     */
    private void mutationOfRoute(Chromosome chromosome, boolean toOptimize) {
        Gene[] genes = chromosome.getGenesList();
        for(int i = 0 ; i < genes.length ; i++) {
            ArrayList<TransportNode> route = genes[i].getRoute();
            int vehicleIndex = genes[i].getVehicleIndex();
            int option = new Random().nextInt(2);
            if(option<1) {
                geneticService.shuffleThisRoute(vehicleIndex,route,genes[i].getTransportsIndicesList(),toOptimize);
                geneticService.reassignSubRoute(vehicleIndex,route,genes[i].getTransportsIndicesList(),toOptimize);
                genes[i].setTotalTime(geneticService.caculateTotalTimeOfThisRoute(vehicleIndex,route));
                genes[i].setTotalMautKm(geneticService.caculateTotalMautKmOfThisRoute(vehicleIndex,route));
            }
            else {
                ArrayList<TransportNode> newRoute = geneticService.createTransportsRouteOfThisGene1(vehicleIndex,genes[i].getTransportsIndicesList());
                genes[i].setRoute(newRoute);
                genes[i].setTotalTime(geneticService.caculateTotalTimeOfThisRoute(vehicleIndex,newRoute));
                genes[i].setTotalMautKm(geneticService.caculateTotalMautKmOfThisRoute(vehicleIndex,newRoute));
            }
        }
        geneticService.checkChromosomeIsValid(chromosome);
     }

    /**
     * To reassign transports requests between routes of individual
     * @param chromosome the individual
     */
    private void mutationOfTransports(Chromosome chromosome) {
        geneticService.reassignTransportsBetweenRoutes(chromosome);
        geneticService.checkChromosomeIsValid(chromosome);
    }

    /**
     * To reassigns new depot and vehicles for routes of individual
     * @param chromosome the individual
     */
    private  void mutationOfDepots(Chromosome chromosome) {
        geneticService.reassignRoutesToBestDepots(chromosome);
        geneticService.checkChromosomeIsValid(chromosome);
     }

    /**
     * To perform uniform crossover over the parents list
     * @param parents the chosen parent lists
     * @param size the size of child population
     * @return the child
     */
    public ArrayList<Chromosome> crossOverStrategy1(ArrayList<Chromosome> parents, int size) {
        ArrayList<Chromosome> child = new ArrayList<>();
        for(int i = 0 ; i < size ; i= i+2) {
            Collections.shuffle(parents);
            Chromosome child1 = geneticService.recombinationOf2Chromosomes(parents.get(0),parents.get(1));
            Chromosome child2 = geneticService.recombinationOf2Chromosomes(parents.get(1),parents.get(0));
            geneticService.checkChromosomeIsValid(child1);
            child.add(child1);
            geneticService.checkChromosomeIsValid(child2);
            child.add(child2);
        }
        // System.out.println("DONE CROSSOVER !");
        return child;
    }

    /**
     * create the initial population
     * @param size size of the initial population
     * @return the initial population
     */
    private ArrayList<Chromosome> createInitPopulation(int size) {
        Chromosome[] population = new Chromosome[size];
        int generatedInvidualCount = 0; // count generated individual
        int firstStrategy = size*20/100;
        int secondStrategy = size*20/100 + size*10/100;
        while (generatedInvidualCount != size) {
            Chromosome individual;
            if(generatedInvidualCount<= firstStrategy) {
                individual = geneticService.createChromosomeBestRouteRandomDepot(1);
            }
            else if(generatedInvidualCount > firstStrategy && generatedInvidualCount <= secondStrategy) {
                individual = geneticService.createChromosomeBestRouteRandomDepot(2);
            }
            else{
                individual = geneticService.createChromosomeBestDepotsRandomRoute();
            }
            geneticService.checkChromosomeIsValid(individual);
            population[generatedInvidualCount] = individual;
            generatedInvidualCount++;
        }
        return new ArrayList<>(Arrays.asList(population));
    }
}

