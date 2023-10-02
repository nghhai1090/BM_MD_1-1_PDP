from scipy.stats import qmc
import matplotlib.pyplot as plt
import math

sampler = qmc.LatinHypercube(d=2)
sample = sampler.random(n=20)
sample_transpose = sample.T * 20

x = sample_transpose[0]
y = sample_transpose[1]

# Create a scatter plot
plt.scatter(x, y, marker='o', linestyle='-', color='b')

for i, (xi, yi) in enumerate(zip(x, y)):
    rounded_x = math.ceil(xi)
    rounded_y = math.ceil(yi)
    plt.annotate(f'({rounded_x}, {rounded_y})', (xi, yi), textcoords="offset points", xytext=(0,10), ha='center')

# Set labels for the axes
plt.xlabel('number of vehicles')
plt.ylabel('number of transportation requests')

# Show the plot
plt.show()




