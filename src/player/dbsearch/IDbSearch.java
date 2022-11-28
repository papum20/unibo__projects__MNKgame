/*
 * INSTEAD OF NODES CONTAINING BOARD, THEY COULD CONTAIN SEQUENCE OF OPERATORS APPLIED,
 * IN ORDER, SO THAT, IN A COMBINATION WITH NODES A AND B, IT IS SUFFICIENT TO USE THE OPERATORS
 * UP TO THE POINT IN COMMON (MAYBE SAVED AS LEVEL, POINTER OR OTHER)
 */


package player.dbsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKPlayer;
import player.ArrayBoard;
import player.ArrayBoardDb;
import player.dbsearch.structures.INodeDB;
import player.dbsearch.structures.Operator;
import player.pnsearch.structures.INodes.MovePair;



public abstract class IDbSearch<N extends INodeDB<N>> implements MNKPlayer {
	
	public static final MovePair DIRECTIONS[] = {
		new MovePair(-1, 0),
		new MovePair(-1, 1),
		new MovePair(0, 1),
		new MovePair(1, 1),
		new MovePair(1, 0),
		new MovePair(1, -1),
		new MovePair(0, -1),
		new MovePair(-1, -1)
	};
	public static final short SHORT_INFINITE = 32767;
	
	protected int M;
	protected int N;
	protected int K;
	protected boolean first;
	protected long timeout_in_millisecs;

	protected MNKCellState MY_MNK_PLAYER;
	protected MNKCellState YOUR_MNK_PLAYER;
	//protected MNKGameState MY_WIN;
	protected int MY_PLAYER;
	//protected final short PROOF_N_ZERO = INodes.PROOF_N_ZERO;
	//protected final short PROOF_N_INFINITE = INodes.PROOF_N_INFINITE;

	protected ArrayBoardDb board;
	
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Runtime runtime;

	//protected N current_root;
	protected Operator[][] threats;				//threats (as operators), partitioned in arrays by category
	protected short max_tier;
	protected final short MAX_CHILDREN = 10;

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
				MNKCell opponent_move = MC[MC.length - 1];
				//mark opponent cell
				board.markCell(opponent_move.i, opponent_move.j);
				// DEBUG
				System.out.println("last/opponent: " + MC[MC.length - 1]);
			}

			//new root
			N root = newNode(board);

			//recursive call for each possible move
			try{
				visit(root, true, getGoalSquares(board, SHORT_INFINITE, true), true, max_tier);
			} catch (NullPointerException e) {
				System.out.println("VISIT: NULL EXCEPTION");
				throw e;
			} catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("VISIT: ARRAY BOUNDS EXCEPTION");
				throw e;
			}
			// DEBUG
			debug.markedCells(0);
			nodes_created_tot += nodes_created;
			nodes_alive_tot += nodes_alive;
			
			MNKCell best_move = getBestMove();
			if(best_move != null) System.out.println("FOUND BEST NODE");
			else best_move = FC[0];
			//update my istance of board
			board.markCell(best_move.i, best_move.j);								//mark my cell

			// DEBUG
			debug.info();
			debug.close();
			System.out.println("my move: " + best_move);

			return best_move;
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
		 * @param node
		 * @param attacker : true if i'm attacker
		 * @param goal_squares : if one occupied by attacker, terminates search
		 * @param attacking : potential winning threat sequences only investigated for attacker
		 * @param max_tier : only threats <= this category can be applied
		 */
		protected void visit(N root, boolean attacker, MovePair[] goal_squares, boolean attacking, short max_tier) {
			short level = 1;
			while(!isTimeEnded() && isTreeChanged()) {
				addDependencyStage(root, level);
				addCombinationStage(root, root);
				level++;
			}
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		protected void addDependencyStage(N node, short level) {
			if(node != null) {
				if(isLastCombination(node))
					addDependentChildren(node);
				addDependencyStage_next(node);
			}
		}

		protected void addDependentChildren(N node) {
			Operator[] applicableOperators = getApplicableOperators(node);
			for(Operator f : applicableOperators) {
				N newChild = addChild(node, f);
				addDependentChildren(newChild);
			}
		}

		protected void addCombinationStage(N node, N root) {
			if(node != null) {
				if(isLastDependency(node))
					findAllCombinationNodes(node, root);
				addCombinationStage_next(node, root);
			}
		}

		protected void findAllCombinationNodes(N partner, N node) {
			if(node != null) {
				if(!inConflict(partner, node)) {
					if(isDependencyNode(node)) addDependingCombinations(partner, node);
					findAllCombinationNodes_next(partner, node);
				}
			}
		}
		
	//#endregion ALGORITHM

	
	
	//#region AUXILIARY
	
		/**
		 * @param node
		 * @return wether node is a combination node (or root) and was created in the last turn (level)
		 */
		protected abstract boolean isLastCombination(N node);
		/**
		 * @param node
		 * @return wether node is a combination node (or root) and was created in this turn turn (level)
		 */
		protected abstract boolean isLastDependency(N node);
		/**
		 * recursive call to addDependencyStage, for children and siblings
		 * @param node
		 */
		protected abstract boolean isCombinationNode(N node);
		protected abstract boolean isDependencyNode(N node);
		protected abstract void addDependencyStage_next(N node);
		protected abstract void addCombinationStage_next(N node, N root);
		protected abstract Operator[] getApplicableOperators(N node);
		protected abstract N addChild(N node, Operator f);
		protected abstract boolean isTreeChanged();
		protected abstract boolean inConflict(N A, N B);
		protected abstract void addDependingCombinations(N A, N B);
		protected abstract void findAllCombinationNodes_next(N partner, N next);

		//returns true if it's time to end the turn
		protected boolean isTimeEnded() {
			return (System.currentTimeMillis() - timer_start) >= timer_end;
		}
		//returns true if available memory is less than a small percentage of max memory
		protected boolean isMemoryEnded() {
			// max memory useable by jvm - (allocatedMemory = memory actually allocated by system for jvm - free memory in totalMemory)
			long freeMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
			return freeMemory < runtime.maxMemory() * (5 / 100);
		}
		protected boolean isMyTurn() {
			return board.currentPlayer() == MY_PLAYER;
		}
		protected abstract MNKCell getBestMove();
		protected MovePair[] getGoalSquares(ArrayBoardDb board, short max, boolean my_attacker) {
			LinkedList<ThreatCell> threats = getThreats(board, max, my_attacker);
			int min = (threats.size() < max) ? threats.size() : max;
			MovePair[] res = new MovePair[min];
			ListIterator<ThreatCell> it = threats.listIterator();
			for(int i = 0; i < min; i++) {
				ThreatCell tmp = it.next();
				res[i] = new MovePair(tmp.y, tmp.x);
			}
			return res;
		}
		protected LinkedList<ThreatCell> getThreats(ArrayBoardDb board, short max, boolean my_attacker) {
			short tier = 0, i = 0, j = 0;
			LinkedList<ThreatCell> res = new LinkedList<ThreatCell>();
			while(res.size() < max && tier <= max_tier) {
				for(short fi = 0; fi < threats[tier].length; fi++) {
					for(short dir = 0; dir < DIRECTIONS.length; dir++) {
						if(isOperatorInCell(board, i, j, dir, threats[tier][fi], my_attacker))
							res.addFirst(new ThreatCell(tier, (short)fi, (short)dir, i, j));
					}
				}
				if(j < N - 1) j++;
				else if(i < M - 1) {
					i++;
					j = 0;
				}
				else tier++;
			}
			return res;
		}
		protected boolean isOperatorInCell(ArrayBoardDb board, int i, int j, short dir, Operator f, boolean my_attacker) {
			int i_last = i + DIRECTIONS[dir].i() * f.precondition.length,
				j_last = i + DIRECTIONS[dir].j() * f.precondition.length;
			if(i_last < 0 || i_last >= M || j_last < 0 || j_last >= N) return false;
			else {
				MNKCellState[] precondition = Operator.toMNKCellState(f.precondition, my_attacker? MY_MNK_PLAYER : YOUR_MNK_PLAYER);
				for(int len = 0; len < f.precondition.length; len++) {
					if(board.cellState(i, j) != precondition[len]) return false;
					else {
						i += DIRECTIONS[dir].i();
						j += DIRECTIONS[dir].j();
					}
				}
				return true;
			}
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
				MY_MNK_PLAYER = MNKCellState.P1;
				YOUR_MNK_PLAYER = MNKCellState.P2;
				//player_opponent = MNKCellState.P2;
				//MY_WIN = MNKGameState.WINP1;
				MY_PLAYER = 0;
				//your_win = MNKGameState.WINP2;
			} else {
				MY_MNK_PLAYER = MNKCellState.P2;
				YOUR_MNK_PLAYER = MNKCellState.P1;
				//player_opponent = MNKCellState.P1;
				//MY_WIN = MNKGameState.WINP2;
				MY_PLAYER = 1;
				//your_win = MNKGameState.WINP1;
			}
		}
		//inits own attributes (for this class)
		// INIT BOARD!
		protected void initAttributes() {
			// init board
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			initThreats();
			//current_root = newNode();

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
			debug = new Debug("debug/debug-" + playerName(), false);
		}

		protected void initThreats() {

		}

		// create N object
		protected abstract N newNode(ArrayBoardDb board);

	//#endregion INIT



	//#region CLASSES

		protected class ThreatCell {
			public short tier;	//threat tier
			public short i;		//index in threat of such tier
			public short dir;	//direction
			public short y, x;
			public ThreatCell(short tier, short i, short dir, short y, short x) {
				this.tier = tier;
				this.i = i;
				this.dir = dir;
				this.y = y;
				this.x = x;
			}
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
			/*public void node(N node) {
				if(active) {
					String txt = (isMyTurn() ? "P" : "D") + ((node.getMove() == null) ? "root" : node.getMove());
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
					String txt = tabs(minus) + (isMyTurn() ? "P" : "D") + ((node.getMove() == null) ? "root" : node.getMove());
					System.out.println(txt);
					try {
						file.write(txt + "\n");
					} catch(IOException e) {
						System.out.println(error + "(nested)");
					}
				}
			}*/
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
