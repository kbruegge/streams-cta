__author__ = 'kai'

import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
matplotlib.style.use('ggplot')

df = pd.read_json("./plots/performance/performance_multi_kryo_fhgfs.json")


total_event_rate = df.groupby("@stream").mean().sum()
print("Total event rate: " + str(total_event_rate))

axis = df.boxplot(by='@stream')
plt.title("Mean event rate per execution thread")
plt.suptitle("")
axis.set_xticklabels(["T "+ str(i) for i in range(0,8)])
plt.xlabel("Thread")
plt.savefig("multi_kryo_fhgfs.pdf")

plt.figure()

df = pd.DataFrame()
for i in range(1,8):
    df["num_threads_" + str(i)] = pd.read_json("")




# plt.show()
# print(df.head())

