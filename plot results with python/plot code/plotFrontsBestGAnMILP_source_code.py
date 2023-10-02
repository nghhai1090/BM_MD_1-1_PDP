# Initialize a dictionary to store sets of points for each "toll km" value
import matplotlib.pyplot as plt
from itertools import cycle
from matplotlib.cm import get_cmap
points_by_toll_km = {}
inputsname = ['t1v19', 't2v7', 't3v15', 't4v5', 't5v12', 't6v9', 't7v2', 't8v6', 't9v1', 't10v18', 't11v16', 't12v10', 't13v17', 't14v11', 't15v20'
    , 't16v4', 't17v14', 't18v13', 't19v8', 't20v3']
for j in range(3,13):

    with open('result'+str(j+1)+'.txt', 'r') as file:
        lines = file.readlines()
    i = 0
    count = 0
    index = 0
    while i < len(lines):
        if lines[i].strip().startswith("BEST GA RUN IS RUN NUMBER :"):
            index = int(lines[i].split(":")[1])- 1
            print(index)
            i += 1
        elif lines[i].strip().startswith("toll km"):
            toll_km = lines[i].split()[2]
            points = []
            i += 1  # Skip the header line

            while i < len(lines) and lines[i].strip():  # Read lines until an empty line
                values = lines[i].strip().replace(',', '.').split()
                if len(values) == 2:
                    x, y = map(float, values)
                    points.append((x, y))
                i += 1
            points_by_toll_km[count] = points
            count += 1
        else:
            i += 1



    line_styles = ['-', '--', '-.', ':']
    line_styles = (line_styles)
    set_labels = {}

    print(points_by_toll_km.items())

    # Plot each set of points with a different color
    for toll_km, points in points_by_toll_km.items():

        line_style = line_styles[1]

        if len(points) > 0  and toll_km == index:
            x_values, y_values = zip(*points)  # Unzip the points into x and y values
            plt.scatter(x_values, y_values, color='red',s=100, alpha=1)
            for i in range(len(points) - 1):
                if toll_km not in set_labels:
                    plt.plot([x_values[i], x_values[i+1]], [y_values[i], y_values[i+1]], color='red', linestyle=line_style, linewidth=4)
                    set_labels[toll_km] = True
                else:
                    plt.plot([x_values[i], x_values[i+1]], [y_values[i], y_values[i+1]], color='red', linestyle=line_style, linewidth=4)

        elif len(points) > 0  and toll_km == 20:
            x_values, y_values = zip(*points)  # Unzip the points into x and y values
            plt.scatter(x_values, y_values, color='blue',s=60, alpha=1)
            for i in range(len(points) - 1):
                if toll_km not in set_labels:
                    plt.plot([x_values[i], x_values[i+1]], [y_values[i], y_values[i+1]], color='blue', linestyle=line_style, linewidth=2)
                    set_labels[toll_km] = True
                else:
                    plt.plot([x_values[i], x_values[i+1]], [y_values[i], y_values[i+1]], color='blue', linestyle=line_style, linewidth=2)


    # Add labels and legend


    plt.xlabel('toll price')
    plt.ylabel('max time')
    plt.title('Pareto fronts from GA (red) and MILP (blue) by input '+inputsname[j])
    plt.legend()
    plt.savefig('re'+str(j+1))
    # Show the plot
    plt.show()








