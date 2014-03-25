RockPaperScissorsCellularAutomata
=================================

###Startup
To open the program, run dist/RPS.jar 

###Instructions
For each generation of the cellular automaton, each cell randomly checks one of its neighbors. If the cell "beats" that neighbor, then the neighbor turns the color of that
cell. In this implementation, blue beats green, green beats red, and red beats blue. This relationship cyclic relationship gives the cellular automaton its name (imagine each color as rock, paper, or scissors).

You can change the definition of "neighbor" with the von Neumann or radius radio buttons. If "von Neumann" is selected, each cell will randomly check one of the cells directly above, below, to the left, or to the right. If "radius" is selected, each cell will randomly check one of the cells within a certain distance (specified in the drop down menu next to "radius").

Use the Speed slider to adjust the time between generations.

Use the Pixel size slider to adjust zoom level.