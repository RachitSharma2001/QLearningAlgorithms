import java.util.Scanner;

public class SarsaLamda {
	// Variables needed
	static int numRows = 3;
	static int numCols = 4;
	static int num_episodes = 1000000;
	static double alpha = 0.7;
	static double gamma = 0.9;
	static double lamda = 0.5;
	static double epsilon = 1;
	static double living_reward = -0.03;
	// down, up, right, left
	static int dx[] = { 1, -1, 0, 0 };
	static int dy[] = { 0, 0, 1, -1 };
	static double Q[][] = new double[100][100];
	static double E[][] = new double[100][100];
	static int grid[][] = new int[10][10];
	static double reward[][] = new double[15][15];
	static int coord_to_state[][] = new int[3][4];
	static int x_coord_given_state[] = new int[20];
	static int y_coord_given_state[] = new int[20];
	static long visited[][] = new long[20][20];
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
			//System.out.println("Greedy");
			// Take a random action
			int next_action = (int) (Math.random() * 4);
			// Repeat process until you get action that leads to a valid state
			while (next_state(state, next_action) == state) {
				next_action = (int) (Math.random() * 4);
			}
			return next_action;
		} else {
			// Perform a greedy action
			int next_action = -1;
			double currentBest = -1<<30;
			for (int a = 0; a < 4; a++) {
				if(next_state(state, a) == state) continue;
				if (Q[state][a] > currentBest) {
					currentBest = Q[state][a];
					next_action = a;
				}else if(Q[state][a] == currentBest){
					// If we have to settle tie breakers, pick randomly
					int random_a = (int) (Math.random() * 2);
					if(random_a == 1){
						currentBest = Q[state][a];
						next_action = a;
					}
				}
			}
			return next_action;
		}
	}
	
	public static void main(String[] args){
		
		Scanner in = new Scanner(System.in);
		// Input the enviornment
		for(int i = 0; i < numRows; i++){
			for(int j = 0; j < numCols; j++){
				grid[i][j] = in.nextInt();
			}
		}

		// Building our coord_to_state array
		int counter = 0;
		for(int i = 0; i < numRows; i++){
			for(int j = 0; j < numCols; j++){
				coord_to_state[i][j] = counter;
				x_coord_given_state[counter] = i;
				y_coord_given_state[counter] = j;
				counter++;
			}
		}
		
		//Set up our Rewards Array
		for(int i = 0; i < numRows; i++){
			for(int j = 0; j < numCols; j++){
				int state = coord_to_state[i][j];
				for(int k = 0; k < 4; k++){
					int nx = i + dx[k];
					int ny = j + dy[k];
					if(outOfBounds(nx, ny)) continue;
					if(isTerminal(nx, ny)){
						reward[coord_to_state[i][j]][coord_to_state[nx][ny]] = grid[nx][ny];
					}else{
						reward[coord_to_state[i][j]][coord_to_state[nx][ny]] = living_reward;
					}
				}
			}
		}
		int start_state = 0;
		for(int episodes = 0; episodes < num_episodes; episodes++){
			// Reset E 
			for(int states = 0; states < numRows*numCols; states++){
				for(int a = 0; a < 4; a++){
					E[states][a] = 0;
				}
			}
			
			// Initlize state and action to valid values
			while(isTerminal(start_state)){
				start_state++;
				if(start_state == (numRows * numCols)){
					start_state = 0;
				}
			}
			int s = start_state;
			int a = epsilon_greedy(s);
			start_state++;
			if(start_state == (numRows * numCols)){
				start_state = 0;
			}
			while(!isTerminal(s)){
				if(episodes < 5000)
				visited[s][a]++;
				// Take action a, observe reward and s'
				int sprime = next_state(s, a);
				double r = reward[s][sprime];
				
				// Choose a' from s' using epsilon greedy policy
				int aprime = epsilon_greedy(sprime);
				
				// Calculate error 
				double error = r + (gamma * Q[sprime][aprime]) - Q[s][a];
				
				// Increment our current state,action pair of E
				E[s][a]++;
				
				// Update all s,a pairs for Q and E
				for(int state = 0; state < (numRows * numCols); state++){
					for(int actions = 0; actions < 4; actions++){
						Q[state][actions] = Q[state][actions] + (alpha * error * E[state][actions]);
						E[state][actions] = gamma*lamda*E[state][actions];
					}
				}
				
				// Update current state and action
				s = sprime;
				a = aprime;
			}
			// Decay Epsilon and Alpha
			if ((episodes) % 5000 == 0) {
				epsilon = 1 / (1 + (double) episodes / 5000);
			}
			if(episodes % 800 == 0){
				alpha = 1/(1 + (double) episodes/800);
			}
		}
		for(int i = 0; i < numRows; i++){
			for(int j = 0; j < numCols; j++){
				int curr_state = coord_to_state[i][j];
				double current_best = Q[curr_state][0];
				int action = 0;
				for(int a = 1; a < 4; a++){
					if(Q[curr_state][a] > current_best){
						current_best = Q[curr_state][a];
						action = a;
					}
				}
				String s_action = "D";
				// up left right
				if(action == 1){
					s_action = "U";
				}else if(action == 2){
					s_action = "R";
				}else if(action == 3){
					s_action = "L";
				}
				System.out.print(s_action + " ");
			}
			System.out.println();
		}
		
	}

}
