package player.dbsearch;

import java.io.FileWriter;
import java.io.IOException;

import player.ArrayBoard;
import player.pnsearch.structures.INodes.MovePair;



public abstract class IDbSearch implements mnkgame.MNKPlayer {
	
	
	
	protected int M;
	protected int N;
	protected int K;
	protected boolean first;
	protected long timeout_in_millisecs;

	//protected MNKGameState MY_WIN;
	protected int MY_PLAYER;
	//protected final short PROOF_N_ZERO = INodes.PROOF_N_ZERO;
	//protected final short PROOF_N_INFINITE = INodes.PROOF_N_INFINITE;

	protected ArrayBoard board;
	
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Runtime runtime;

	//protected N current_root;
	Threat[][] threats;							//threats (as operators), partitioned in arrays by category

	protected Debug debug;
	protected int nodes_created;
	protected int nodes_alive;
	protected int nodes_created_tot;
	protected int nodes_alive_tot;
	

	
	
	//#region PLAYER

		public IDbSearch() {

		}
	
		/**
			 * Initialize the (M,N,K) Player
			 *
			 * @param M Board rows
			 * @param N Board columns
			 * @param K Number of symbols to be aligned (horizontally, vertically, diagonally) for a win
			 * @param first True if it is the first player, False otherwise
			 * @param timeout_in_secs Maximum amount of time (in seconds) for selectCell 
		 */
		public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
			initParameters(M, N, K, first, timeout_in_secs);
			initCellStates(first);
			initAttributes();
		}

		
		/**
			* Select a position among those listed in the <code>FC</code> array
			*
			* @param FC Free Cells: array of free cells
			* @param MC Marked Cells: array of already marked cells, ordered with respect
			* to the game moves (first move is in the first position, etc)
			*
			* @return an element of <code>FC</code>
		*/
		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

			// DEBUG
			System.out.println("--------\t" + MC.length + "\t--------");
			debug.open();
			nodes_created = 0;
			nodes_alive = 0;

			//start conting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(MC.length > 0) {
				M opponent_move = newMove(MC[MC.length - 1]);
				//mark opponent cell
				board.markCell(opponent_move.i(), opponent_move.j());
				//update current_root (with last opponent move)
				//assumption: current_root != null
				N new_root = current_root.findChild(opponent_move);
				if(new_root != null) {
					System.out.println("found opponent move in tree.");
					nodes_alive_tot -= current_root.getChildrenLength();
					current_root = new_root;
					current_root.setParent(null);
				}
				else current_root.reset(opponent_move);
				
				// DEBUG
				System.out.println("last/opponent: " + MC[MC.length - 1]);
			}
			// DEBUG
			debug.markedCells(0);
			if(current_root.getMove() != null) System.out.println(current_root.getPosition());
			//recursive call for each possible move
			try{
				visit(current_root);
			} catch (NullPointerException e) {
				System.out.println("VISIT: NULL EXCEPTION");
				throw e;
			} catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("VISIT: ARRAY BOUNDS EXCEPTION");
				throw e;
			}
			// DEBUG
			debug.markedCells(0);
			if(current_root.getMove() != null) System.out.println(current_root.getPosition());
			nodes_created_tot += nodes_created;
			nodes_alive_tot += nodes_alive;
			
			N best_node = getBestNode();
			MNKCell res = FC[0];
			if(best_node != null) {
				System.out.println("FOUND BEST NODE");
				res = new MNKCell(best_node.i(), best_node.j());
				//update current_root (with my last move)
				nodes_alive_tot -= current_root.getChildrenLength();
				current_root = best_node;
			};
			//update my istance of board
			board.markCell(res.i, res.j);								//mark my cell

			// DEBUG
			debug.info();
			debug.close();
			System.out.println("my move: " + res);

			return res;
		}
		
		/**
			 * Returns the player name
			 *
			* @return string 
		*/
		public String playerName() {
			return "IDbSearch";
		}

		//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * @param position
		 * @param attacker : true if i'm attacker
		 * @param goal_squares : if one occupied by attacker, terminates search
		 * @param attacking : potential winning threat sequences only investigated for attacker
		 * @param max_tier : only threats <= this category can be applied
		 */
		public void visit(boolean attacker, MovePair[] goal_squares, boolean attacking, short max_tier) {

		}
		
	//#endregion ALGORITHM

	
	
	//#region AUXILIARY
	
		protected boolean isMyTurn() {
			return board.currentPlayer() == MY_PLAYER;
		}
		
	//#endregion AUXILIARY
	
	
	
	//#region INIT
		
		//inits InitPlayer parameters
		protected void initParameters(int M, int N, int K, boolean first, int timeout_in_secs) {
			// initialize Player interface variable (passed as parameters)
			this.M = M;
			this.N = N;
			this.K = K;
			this.first = first;
			this.timeout_in_millisecs = timeout_in_secs * 1000;		//converts seconds in milliseconds
		}
		//inits constants related to cell states, turn order in game...
		protected void initCellStates(boolean first) {
			if(first) {
				//player_own = MNKCellState.P1;
				//player_opponent = MNKCellState.P2;
				MY_WIN = MNKGameState.WINP1;
				MY_PLAYER = 0;
				//your_win = MNKGameState.WINP2;
			} else {
				//player_own = MNKCellState.P2;
				//player_opponent = MNKCellState.P1;
				MY_WIN = MNKGameState.WINP2;
				MY_PLAYER = 1;
				//your_win = MNKGameState.WINP1;
			}
		}
		//inits own attributes (for this class)
		protected void initAttributes() {
			board = new ArrayBoard(M, N, K);
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			initThreats();
			current_root = newNode();

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
			debug = new Debug("debug/debug-" + playerName(), false);
		}

		protected void initThreats() {

		}

		// create N object
		protected abstract M newMove(MNKCell move);
		protected abstract N newNode();

	//#endregion INIT



	//#region CLASSES

		protected class Threat {
			
		}
	
	//#endregion CLASSES

	//#region DEBUG

		protected class Debug{
			protected FileWriter file;
			protected String filename;
			protected String filename_current;
			protected String error;
			protected boolean active;

			public Debug(String filename, boolean active) {
				this.filename = filename;
				this.filename_current = filename;
				this.error = "WTF";
				this.active = active;
			}
			public void open() {
				if(active) {
					filename_current = filename + board.MarkedCells_length() + ".txt";
					try {
						new File(filename_current);
						file = new FileWriter(filename_current);
					} catch (IOException e) {
						System.out.println(error + " (open)");
					}
				}
			}
			public void close() {
				if(active) {
					try {
						file.close();
					} catch(IOException e) {
						System.out.println(error + " (close)");
					}
				}
			}
			
			// print up to last one minus "minus"
			public void freeCells(int minus) {
				if(active) {
					String fc = "";
					for(int i = 0; i < board.FreeCells_length() - minus; i++) fc += Integer.toString(board.getFreeCell(i).i) + Integer.toString(board.getFreeCell(i).j) + " ";
					System.out.println("mc: " + fc);				
					try {
						file.write("mc: " + fc + "\n");
					} catch(IOException e) {
						System.out.println(error + " (fc)");
					}
				}
			}
			public void markedCells(int minus) {
				if(active) {
					String mc = "";
					for(int i = 0; i < board.MarkedCells_length() - minus; i++) mc += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
					System.out.println("mc: " + mc);				
					try {
						file.write("mc: " + mc + "\n");
					} catch(IOException e) {
						System.out.println(error + " (mc)");
					}
				}
			}
			public void node(N node) {
				if(active) {
					String txt = (isMyTurn() ? "P" : "D") + ((node.getMove() == null) ? "root" : node.getPosition()) + " " + Short.toString(node.proof) + " " + Short.toString(node.disproof);
					System.out.println(txt);
					try {
						file.write(txt + "\n");
					} catch(IOException e) {
						System.out.println(error + " (node)");
					}
				}
			}
			public void nestedNode(N node, int minus) {
				if(active) {
					String txt = tabs(minus) + (isMyTurn() ? "P" : "D") + ((node.getMove() == null) ? "root" : node.getPosition()) + " " + Short.toString(node.proof) + " " + Short.toString(node.disproof);
					System.out.println(txt);
					try {
						file.write(txt + "\n");
					} catch(IOException e) {
						System.out.println(error + "(nested)");
					}
				}
			}
			public void info() {
				System.out.println("time" + Long.toString(System.currentTimeMillis() - timer_start));
				System.out.println("nodes created:\t\t" + Integer.toString(nodes_created));
				System.out.println("nodes alive:\t\t" + Integer.toString(nodes_alive));
				System.out.println("nodes created tot:\t" + Integer.toString(nodes_created_tot));
				System.out.println("nodes alive tot:\t" + Integer.toString(nodes_alive_tot));
			}
			// return string with a tab for each depth level
			public String tabs(int minus) {
				String tab = "\t";
				for(int i = 0; i < board.MarkedCells_length(); i++) tab += "\t";
				return tab;
			}

		}
	
	//#endregion DEBUG


}
