import matplotlib.pyplot as plt
import numpy as np
# Replace 'data' with your list of values
data = [100, 100, 100, 96.76, 99.41, 100, 105.36, 125.18, 140.49, 249.12, 162.80, 162.17, 133.95]
#data = [23, 45, 67, 34, 56, 78, 90, 12, 45, 67]
# Create a boxplot
minimum = min(data)
maximum = max(data)
median = np.median(data)
q1 = np.percentile(data, 25)
q3 = np.percentile(data, 75)

# Add annotations for the values
plt.annotate(f'Min: {minimum:.2f}', xy=(1, minimum), xytext=(1.2, minimum-3), fontsize=10, ha='center')
plt.annotate(f'Max: {maximum:.2f}', xy=(1, maximum), xytext=(1.2, maximum), fontsize=10, ha='center')
plt.annotate(f'Median: {median:.2f}', xy=(1, median), xytext=(1.2, median), fontsize=10, ha='center')
plt.annotate(f'Q1: {q1:.2f}', xy=(1, q1), xytext=(1.2, q1), fontsize=10, ha='center')
plt.annotate(f'Q3: {q3:.2f}', xy=(1, q3), xytext=(1.2, q3), fontsize=10, ha='center')

plt.boxplot(data)

# Customize the plot (optional)
plt.title('Boxplot of % Hypervolume of GA over MILP from first 13 inputs')
plt.xlabel('')
plt.ylabel('% Hypervolume of GA over MILP')
plt.savefig('boxplot')
# Show the plot
plt.show()
