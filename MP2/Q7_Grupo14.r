x <- read.csv("C:\\Users\\joao-\\eclipse-workspace\\MP2Redes\\MP2_Grupo14.csv", skip=1) # Change path to csv
xs = x[, c(8)]
f <- ecdf(xs)
plot(sort(xs), 1-f(sort(xs)), type="s", lwd=1)
