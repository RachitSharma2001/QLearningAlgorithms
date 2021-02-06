import java.util.Scanner;

public class Sarsa {
	// Variables needed
	static int numRows = 3;
	static int numCols = 4;
	static int num_episodes = 1000000;
	static double alpha = 0.7;
	static double gamma = 0.9;
	static double epsilon = 1;
	static double living_reward = -0.03;
	// down, up, right, left
	static int dx[] = { 1, -1, 0, 0 };
	static int dy[] = { 0, 0, 1, -1 };
	static double Q[][] = new double[100][100];
	static int grid[][] = new int[10][10];
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

	public static void main(String[] args) {
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
					// System.out.println("(" + i + "," + j + ") and (" + nx +
					// "," + ny + "): " + reward[state][newState]);
				}
			}
		}

		// Need to initialize the rewards
		int visited = 0;
		int start_state = 0;
		for (int episodes = 1; episodes < num_episodes; episodes++) {
			// Get a valid start state
			if (start_state == (numRows * numCols)) {
				start_state = 0;
			}
			while (isTerminal(x_coord_given_state[start_state],
					y_coord_given_state[start_state])) {
				start_state++;
				if (start_state == (numRows * numCols)) {
					start_state = 0;
				}
			}
			int curr_state = start_state;
			start_state++;
			// Get action A
			int a = epsilon_greedy(curr_state);
			// Repeat until the end of the episode
			while (!isTerminal(x_coord_given_state[curr_state],
					y_coord_given_state[curr_state])) {
				// Take Action a, observe reward and sprime
				int sprime = next_state(curr_state, a);
				double newReward = reward[curr_state][sprime];

				// Choose A' form S'
				int aprime = epsilon_greedy(sprime);

				// Update Q
				double sample = newReward + (gamma * Q[sprime][aprime]);
				Q[curr_state][a] = Q[curr_state][a]
						+ (alpha * (sample - Q[curr_state][a]));
				// Update curr_state and a
				curr_state = sprime;
				a = aprime;
			}

			// Decay Epsilon and Alpha
			if ((episodes) % 5000 == 0) {
				epsilon = 1 / (1 + (double) episodes / 5000);
			}
			if (episodes % 800 == 0) {
				alpha = 1 / (1 + (double) episodes / 800);
			}
			// if(episodes < 10)
			// System.out.println("Num Episodes: " + episodes + " epsilon: " +
			// epsilon + " alpha " + alpha + " visited " + visited);
		}

		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
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
		// System.out.println(Q[0][0] + " " + Q[0][1] + " " + Q[0][2] + " " +
		// Q[0][3] + " " + visited);
	}

}
