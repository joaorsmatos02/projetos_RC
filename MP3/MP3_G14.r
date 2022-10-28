x <- read.table("C:\\Users\\joao-\\eclipse-workspace\\MP3Redes\\results_SYN.txt") # Change path to txt
plot (x, log = 'y')
y <- read.table("C:\\Users\\joao-\\eclipse-workspace\\MP3Redes\\results_UDP.txt") # Change path to txt
plot (y, log = 'y')
z <- read.table("C:\\Users\\joao-\\eclipse-workspace\\MP3Redes\\results_POD.txt") # Change path to txt
plot (z, log = 'y')
