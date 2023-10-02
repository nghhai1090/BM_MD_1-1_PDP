READ ME:
+ To run the benchmark : 
	1. open terminal window 
	2. navigate to the directory where this file is located
	3.  type: java -jar BM_MD_1-1_PDP.jar distanceMatrix.txt mautMatrix.txt [name of .txt file contains transports and vehicles info] [name of result file in txt] [time limit of each GA run] [number of GA runs to be performed] [time limit of MILP, set to -1 for disable MILP when gurobi solver not available]

+ File Structure for Transport and Vehicle Information:: 
	* The first row should contain the number of transports and the number of vehicles.
	* The next rows equal to the number of transports should contain the following information for each transport: 
		- From location
		- To location
		- Amount of transport
		- Active time at the from location
		- Active time at the to location
	* The subsequent rows equal to the number of vehicles should specify the details for each vehicle:
		- Depot location of the vehicle
		- Capacity of the vehicle
		- Speed of the vehicle

	* Example: see file t4v5.txt

+ For example run -> open run.bat