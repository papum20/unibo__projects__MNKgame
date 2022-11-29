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
import mnkgame.MNKGameState;
import mnkgame.MNKPlayer;
import player.boards.IBoardDB;
import player.dbsearch.structures.INodeDB;
import player.dbsearch.structures.Operator;
import player.pnsearch.structures.INodes.MovePair;



public abstract class IDbSearch<B extends IBoardDB, N extends INodeDB<N>> implements MNKPlayer {
	
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
	protected MNKGameState MY_WIN;
	protected MNKGameState YOUR_WIN;
	protected int MY_PLAYER;
	//protected final short PROOF_N_ZERO = INodes.PROOF_N_ZERO;
	//protected final short PROOF_N_INFINITE = INodes.PROOF_N_INFINITE;

	protected B board;
	protected MNKCell best_move;
	protected MNKCell last_move;
	
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
				last_move = MC[MC.length - 1];
				//mark opponent cell
				board.markCell(last_move.i, last_move.j);
				// DEBUG
				System.out.println("last/opponent: " + MC[MC.length - 1]);
			}

			//new root
			N root = createRoot();

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
		 * @param root : root for this db-search
		 * @param my_attacker : true if i'm attacker
		 * @param goal_squares : if one occupied by attacker, terminates search
		 * @param attacking : potential winning threat sequences only investigated for attacker
		 * @param max_tier : only threats <= this category can be applied
		 * @return true if there's a winning sequence
		 */
		protected boolean visit(N root, boolean my_attacker, MovePair[] goal_squares, boolean attacking, short max_tier) {
			short level = 1;
			boolean won = false;
			while(!isTimeEnded() && isTreeChanged() && !won) {
				won = addDependencyStage(root, level, my_attacker);
				if(!won) won = addCombinationStage(root, root);
				if(won) ;	//save best node
				level++;
			}
			return won;
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		protected boolean addDependencyStage(N node, short level, boolean my_attacker) {
			if(node != null) {
				//if(isLastCombination(node))
					return addDependentChildren(node, my_attacker);
				//iterate through children and siblings
			}
			else return false;
		}
		protected boolean addCombinationStage(N node, N root) {
			if(node != null) {
				//if(isLastDependency(node))
					return findAllCombinationNodes(node, root);
				//iterate through children and siblings
			}
			else return false;
		}

		protected boolean addDependentChildren(N node, boolean my_attacker) {
			//node.board.gameState()
			if(board.gameState() == MY_WIN) {
				setBestMove();
				return true;
			}
			else if(board.gameState() == YOUR_WIN) return false;
			else {
				boolean won = false;
				LinkedList<AppliedOperator> applicableOperators = getApplicableOperators(node, MAX_CHILDREN, my_attacker);
				for(AppliedOperator f : applicableOperators) {
					N newChild = addDependentChild(node, f);
					board.applyOperator(f.y, f.x, f.dir, threats[f.tier][f.i], my_attacker? MY_MNK_PLAYER:YOUR_MNK_PLAYER);
					won = addDependentChildren(newChild, my_attacker);
					board.undoOperator(f.y, f.x, f.dir, threats[f.tier][f.i], my_attacker? MY_MNK_PLAYER:YOUR_MNK_PLAYER);
					if(won) {
						//save best node
						break;
					}
				}
				return won;
			}
		}
		/**
		 * @param partner : fixed node for combination
		 * @param node : iterating node for combination
		 */
		protected boolean findAllCombinationNodes(N partner, N node) {
			//node.board
			if(board.gameState() == MY_WIN) {
				setBestMove();
				return true;
			}
			else if(board.gameState() == YOUR_WIN) return false;
			else if(node != null) {
				boolean won = false;
				if(!partner.inConflict(node)) {
					if(isDependencyNode(node)) won = addCombination(partner, node);
					//iterate through children and siblings
				}
				return won;
			}
			else return false;
		}
		
	//#endregion ALGORITHM

	
	
	//#region AUXILIARY
	
		//#region BOOL
			protected abstract boolean isDependencyNode(N node);
			protected abstract boolean isTreeChanged();
			protected boolean isMyTurn() {
				return board.currentPlayer() == MY_PLAYER;
			}
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
			protected static boolean equalMNKMoves(MNKCell a, MNKCell b) {
				return a.i == b.i && a.j == b.j;
			}
		//#endregion BOOL
		
		//#region CREATE
			protected N createRoot() {
				N root = newNode(board, true);
				return root;
			}
			protected abstract N addChild(N parent, AppliedOperator f, boolean is_combination);
			protected abstract N addDependentChild(N node, AppliedOperator f);
			// ENHANCEMENT: ONLY ADD COMBINATIONS WITH AT LEAST ONE OPERATOR APPLICABLE, SO YOU
			// DON'T ADD USELESS NODES
			protected abstract boolean addCombination(N A, N B);
		//#endregion CREATE

		//#region GET
			protected abstract LinkedList<AppliedOperator> getApplicableOperators(N node, short max, boolean my_attacker);
			protected LinkedList<AppliedOperator> getApplicableOperators(B board, short max, boolean my_attacker) {
				short tier = 0, i = 0, j = 0;
				LinkedList<AppliedOperator> res = new LinkedList<AppliedOperator>();
				while(res.size() < max && tier <= max_tier) {
					for(short fi = 0; fi < threats[tier].length; fi++) {
						for(short dir = 0; dir < DIRECTIONS.length; dir++) {
							if(board.isOperatorInCell(i, j, dir, threats[tier][fi], my_attacker? MY_MNK_PLAYER : YOUR_MNK_PLAYER))
								res.addFirst(new AppliedOperator(tier, (short)fi, (short)dir, i, j));
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
			protected MovePair[] getGoalSquares(B board, short max, boolean my_attacker) {
				LinkedList<AppliedOperator> threats = getApplicableOperators(board, max, my_attacker);
				int min = (threats.size() < max) ? threats.size() : max;
				MovePair[] res = new MovePair[min];
				ListIterator<AppliedOperator> it = threats.listIterator();
				for(int i = 0; i < min; i++) {
					AppliedOperator tmp = it.next();
					res[i] = new MovePair(tmp.y, tmp.x);
				}
				return res;
			}
		//#endregion GET
		//#region SET
			protected void setBestMove() {
				int i = 0;
				if(last_move != null)
					while(!last_move.equals(board.getMarkedCell(i)) ) i++;
				while(board.getMarkedCell(i).state != MY_MNK_PLAYER) i++;
				best_move = board.getMarkedCell(i);
			}
		//#endregion SET
		
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
				MY_WIN = MNKGameState.WINP1;
				YOUR_WIN = MNKGameState.WINP2;
				MY_PLAYER = 0;
				//your_win = MNKGameState.WINP2;
			} else {
				MY_MNK_PLAYER = MNKCellState.P2;
				YOUR_MNK_PLAYER = MNKCellState.P1;
				//player_opponent = MNKCellState.P1;
				MY_WIN = MNKGameState.WINP2;
				YOUR_WIN = MNKGameState.WINP1;
				MY_PLAYER = 1;
				//your_win = MNKGameState.WINP1;
			}
		}
		//inits own attributes (for this class)
		// INIT BOARD!
		protected void initAttributes() {
			// init board
			last_move = null;
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			initThreats();
			//current_root = newNode();

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
			debug = new Debug("debug/debug-" + playerName(), true);
		}

		protected void initThreats() {

		}

		// create N object
		protected abstract N newNode(B board, boolean is_combination);

	//#endregion INIT



	//#region CLASSES

		protected class AppliedOperator {
			public short tier;	//threat tier
			public short i;		//index in threat of such tier
			public short dir;	//direction
			public short y, x;
			public AppliedOperator(short tier, short i, short dir, short y, short x) {
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
