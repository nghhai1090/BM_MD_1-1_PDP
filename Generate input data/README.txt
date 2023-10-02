This folder contains two source code files:

	+ Source Code for Generating Distance Matrix (source code to generate distance matrix.ts): This TypeScript file is used to retrieve the distances between 17 locations by making requests to the CAT API. It then use this to construct a distance matrix. The CAT API still provides no information regarding the toll price, so the toll price matrix will be generated from the distance matrix by removing the last character of each cell. For example: 141 -> 14

	+ Source Code for Generating Random Pickup, Delivery, and Vehicle Information (source code to generate random pickup deliveries and vehicles.java): This Java file generates random pickups and deliveries requests and vehicles information 
	
	+ Sorce code for generating latein hypercube sampling (lhd.py): this file is to generate a latein hypercube sampling using python library. The result will then be plotted.

The input data generated from these 2 source code can be found at /BM_MD_1-1_PDP/input data