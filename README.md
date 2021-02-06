# QLearningAlgorithms
This is a set of Q-Learning algorithms I have implemented in Java.

In these three programs, you can input a 3x4 grid(by changing the variables n and m you can adjust the grid) of 1's(representing positive terminal states),
0's (repesenting free to move areas), and -1's(representing negative terminal states), and a Q-Learning algorithm will find, at each point, 
the optimal direction to move in so to get to the optimal terminal state.

For example, lets say I inputed the following grid:
 
 0  0  0 1 
 
-1 -1 -1 0

 0  0  0 0
 
 The program would output:
 
 R R R T
 
 T T T U
 
 R R R U
 
 where R stands for "Move Right", U stands for "Move up", and T stands for "Terminal State".
