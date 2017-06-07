## Java Solution to the famous Dinning Philosophers problem

# v1

In this version, despite avoiding deadlock conditions, starvation of threads is still possible. 
For enough runs of "eating and thinking", the difference between threads will be more notorious.

# v2

This version won't let the same thread eat twice in a row. This does not completely avoid 
the starvation problem, only minimizes it (if we have a big number of philosophers, not letting one eat 
twice in a row will have less impact, as one can imagine). 

# v1 vs v2

Starvation and deadlocks (race conditions and live-locks also) are a real big problem when 
talking about multi-threaded applications. Version v2 of the solution presented minimizes starvation among threads,
however it makes philosophers eat considerably less times than in version v1, since they have additional overhead in 
checking if they just ate or not.