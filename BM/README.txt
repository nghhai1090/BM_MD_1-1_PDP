READ ME:
+ to run : open terminal window -> navigate to address of this file -> type: java -jar BM_MD_1-1_PDP.jar distanceMatrix.txt mautMatrix.txt [name of .txt file contains transports and vehicles info] [name of result file in txt] [time limit of each GA run] [number of GA runs to be performed] [time limit of MILP, set to -1 for disable MILP when gurobi solver not available]

+ file contains transports and vehicles info: 
first row number of transports,number of vehicles
next number of transports rows, at each row: from,to,amount,active time at from, active time at to
next number of vehicles rows, at each row: depot of vehicle,capacity of vehicle,speed of vehicle

example see file t4v5.txt

example run -> open run.bat