import os
import subprocess
import sys

jarfile = sys.argv[1]
xml = sys.argv[2]
path = sys.argv[3]

# Check if path exits
if not os.path.exists(path):
	print ("Path does not exist")
	sys.exit()

print("Files in folder: ")
print(os.listdir(path))

for i, eventio_file in enumerate(os.listdir(path)):
    filepath = os.path.join(path, eventio_file)
    command =   "java -jar " + jarfile +  " " + xml +\
                " -Dinfile=" + filepath + \
                " -Doutfile=" + path + "/data_" + str(i) + ".kryo"


    print(command)
    try:
        subprocess.check_call(command, shell=True)
    except:
        print("Error caught. Continuing")
