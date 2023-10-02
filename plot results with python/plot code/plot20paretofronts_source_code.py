# Initialize a dictionary to store sets of points for each "toll km" value
import matplotlib.pyplot as plt
from itertools import cycle
from matplotlib.cm import get_cmap
points_by_toll_km = {}
inputsname = ['t1v19', 't2v7', 't3v15', 't4v5', 't5v12', 't6v9', 't7v2', 't8v6', 't9v1', 't10v18', 't11v16', 't12v10', 't13v17', 't14v11', 't15v20'
              , 't16v4', 't17v14', 't18v13', 't19v8', 't20v3']
# Open the file for reading

for j in range(0,20):
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

    custom_colors = [
        '#E63946', '#F1FAEE', '#A8DADC', '#457B9D', '#1D3557',
        '#F4A261', '#2A9D8F', '#E9C46A', '#264653', '#6A0572',
        '#FF6B6B', '#FFE66D', '#6AB04A', '#4ECDC4', '#2B2D42',
        '#F45B69', '#5A677D', '#43AA8B', '#EE6C4D', '#293241'
    ]

    line_styles = ['-', '--', '-.', ':']

    # Initialize a color cycle iterator
    colors = cycle(custom_colors)
    line_styles = cycle(line_styles)
    set_labels = {}

    print(points_by_toll_km.items())

    # Plot each set of points with a different color
    for toll_km, points in points_by_toll_km.items():
        color = next(colors)
        line_style = next(line_styles)

        if len(points) > 0  and toll_km != 20:
            x_values, y_values = zip(*points)  # Unzip the points into x and y values
            plt.scatter(x_values, y_values, color=color,s=60, alpha=1)
            for i in range(len(points) - 1):
                if toll_km not in set_labels:
                    plt.plot([x_values[i], x_values[i+1]], [y_values[i], y_values[i+1]], color=color, linestyle=line_style, linewidth=2)
                    set_labels[toll_km] = True
                else:
                    plt.plot([x_values[i], x_values[i+1]], [y_values[i], y_values[i+1]], color=color, linestyle=line_style, linewidth=2)



    # Add labels and legend


    plt.xlabel('toll price')
    plt.ylabel('max time')
    plt.title('Pareto fronts of 20 GA runs by input '+inputsname[j])
    plt.legend()
    plt.savefig('r'+str(j+1))
    # Show the plot
    plt.show()












