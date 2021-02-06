import java.util.ArrayList;
import java.util.Scanner;


public class DynaQ {
	// Variables needed
	static int numRows = 3;
	static int numCols = 4;
	static int num_episodes = 500000;
	static int n = 20;
	static double alpha = 0.7;
	// Gamma has to be high
	static double gamma = 0.999;
	static double epsilon = 1;
	static double living_reward = -0.03;
	// down, up, right, left
	static int dx[] = { 1, -1, 0, 0 };
	static int dy[] = { 0, 0, 1, -1 };
	static double Q[][] = new double[100][100];
	static int N[][] = new int[15][4];
	static double reward_model[][] = new double[15][4];
	static int transition_model[][] = new int[15][4];
	static int grid[][] = new int[10][10];
	// The "enviornment" reward 
	static double reward[][] = new double[15][15];
	static int coord_to_state[][] = new int[3][4];
	static int x_coord_given_state[] = new int[20];
	static int y_coord_given_state[] = new int[20];

	// Returns whether given state is a terminal state
	public static boolean isTerminal(int x, int y) {
		if (grid[x][y] == 0)
			return false;
		return true;
	}

	// Returns whether given state is a terminal state
	public static boolean isTerminal(int state) {
		int x = x_coord_given_state[state];
		int y = y_coord_given_state[state];
		if (grid[x][y] == 0)
			return false;
		return true;
	}
	
	// Returns whether given coordinates are not on grid
	public static boolean outOfBounds(int row, int col) {
		if (row < 0 || row >= numRows || col < 0 || col >= numCols)
			return true;
		return false;
	}

	// Return the next state given current state and action
	public static int next_state(int state, int action) {
		int newX = x_coord_given_state[state] + dx[action];
		int newY = y_coord_given_state[state] + dy[action];
		if (outOfBounds(newX, newY)) {
			return state;
		}
		return coord_to_state[newX][newY];
	}

	// Returns action by following the epsilon greedy algorithm
	public static int epsilon_greedy(int state) {
		// Determine whether you take a random action or not
		double take_random_action = Math.random();
		if (take_random_action <= epsilon) {
			// Take a random action
			int next_action = (int) (Math.random() * 4);
			// Repeat process until you get action that leads to a valid state
			while (next_state(state, next_action) == state) {
				next_action = (int) (Math.random() * 4);
			}
			return next_action;
		} else {
			// Perform a greedy action
			int next_action = 0;
			double currentBest = Q[state][0];
			for (int a = 1; a < 4; a++) {
				if (Q[state][a] > currentBest) {
					currentBest = Q[state][a];
					next_action = a;
				}
			}
			return next_action;
		}
	}
	
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		// Input the enviornment
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				grid[i][j] = in.nextInt();
			}
		}

		// Building our coord_to_state array
		int counter = 0;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				coord_to_state[i][j] = counter;
				x_coord_given_state[counter] = i;
				y_coord_given_state[counter] = j;
				counter++;
			}
		}

		// Set up our Rewards Array
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				int state = coord_to_state[i][j];
				for (int k = 0; k < 4; k++) {
					int nx = i + dx[k];
					int ny = j + dy[k];
					if (outOfBounds(nx, ny))
						continue;
					if (isTerminal(nx, ny)) {
						reward[coord_to_state[i][j]][coord_to_state[nx][ny]] = grid[nx][ny];
					} else {
						reward[coord_to_state[i][j]][coord_to_state[nx][ny]] = living_reward;
					}
				}
			}
		}
		
		// Dyna-Q algorithm
		int current_state = 0;
		int start_state = 0;
		ArrayList<Integer> state_list = new ArrayList<Integer>();
		ArrayList<Integer> action_list = new ArrayList<Integer>();
 		for(int episodes = 0; episodes < num_episodes; episodes++){
			// If the current state is terminal, find a new state
 			if(isTerminal(current_state)){
				start_state++;
				if(start_state == numRows * numCols){
					start_state = 0;
				}
				while(isTerminal(start_state)){
					start_state++;
					if(start_state == numRows * numCols){
						start_state = 0;
					}
				}
				current_state = start_state;
			}
			
 			// Add current state to state list
			state_list.add(current_state);
			// Calculate action
			int action = epsilon_greedy(current_state);
			// Add action to action list
			action_list.add(action);
			// Find next state
			int sprime = next_state(current_state, action);
			
			// Get the reward from the enviornment
			double earned_reward = reward[current_state][sprime];
			// Update the count array
			N[current_state][action]++;
			
			// Find the max Q of sprime
			double best_q = Q[sprime][0];
			for(int j = 1; j < 4; j++){
				best_q = Math.max(best_q, Q[sprime][j]);
			}
			
			// Update Q
			Q[current_state][action] = Q[current_state][action] + (alpha * (earned_reward + (gamma * best_q) - Q[current_state][action]));
			// Update Reward Model
			reward_model[current_state][action] = (1/(double) N[current_state][action]) * ((reward_model[current_state][action]*(N[current_state][action]-1)) + earned_reward); 

			// Assuming deterministic enviornment, meaning when we take an action we will 
			// 100% take that action
			transition_model[current_state][action] = sprime;
			
			// Simulated actions
			for(int j = 0; j < n; j++){
				// Pick previous state, action pair
				int index = (int) (Math.random() * state_list.size());
				int sampled_state = state_list.get(index);
				int sampled_action = action_list.get(index);
				
				// Find reward and next state
				double modeled_r = reward_model[sampled_state][sampled_action];
				int nextState = transition_model[sampled_state][sampled_action];
				
				// Find max Q of next state
				double max_q = Q[nextState][0];
				for(int k = 1; k < 4; k++){
					max_q = Math.max(max_q, Q[nextState][k]);
				}
				
				// Update Q
				Q[sampled_state][sampled_action] = Q[sampled_state][sampled_action] + (alpha * (modeled_r + (gamma * max_q) - Q[sampled_state][sampled_action]));
			}
			// Move to next state
			current_state = sprime;
			
			// Decay Epsilon and Alpha
			if ((episodes) % 5000 == 0) {
				epsilon = 1 / (1 + (double) episodes/5000);
			}
			if (episodes % 800 == 0) {
				alpha = 1 / (1 + (double) episodes/800);
			}
		}
		
 		// Output calculations
 		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				if(isTerminal(coord_to_state[i][j])){
					System.out.print("T ");
					continue;
				}
				int curr_state = coord_to_state[i][j];
				double current_best = Q[curr_state][0];
				int action = 0;
				for (int a = 1; a < 4; a++) {
					if (Q[curr_state][a] > current_best) {
						current_best = Q[curr_state][a];
						action = a;
					}
				}
				String s_action = "D";
				// up left right
				if (action == 1) {
					s_action = "U";
				} else if (action == 2) {
					s_action = "R";
				} else if (action == 3) {
					s_action = "L";
				}
				System.out.print(s_action + " ");
			}
			System.out.println();
		}
	}
	


}
