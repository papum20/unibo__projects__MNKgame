package player.dbsearch2;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.ListIterator;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import mnkgame.MNKPlayer;
import player.dbsearch2.BiList.BiNode;
import player.dbsearch2.Operators.RankedThreats;
import player.dbsearch2.Operators.Threat;
import player.pnsearch.structures.INodes.MovePair;



public class DbSearch implements MNKPlayer {
	
/*
 * INSTEAD OF NODES CONTAINING BOARD, THEY COULD CONTAIN SEQUENCE OF OPERATORS APPLIED,
 * IN ORDER, SO THAT, IN A COMBINATION WITH NODES A AND B, IT IS SUFFICIENT TO USE THE OPERATORS
 * UP TO THE POINT IN COMMON (MAYBE SAVED AS LEVEL, POINTER OR OTHER)
 */


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
	private final int MAX_THREAT_SEQUENCES = 10;
	//protected final short PROOF_N_ZERO = INodes.PROOF_N_ZERO;
	//protected final short PROOF_N_INFINITE = INodes.PROOF_N_INFINITE;

	protected DbBoard board;
	
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Runtime runtime;

	////protected N current_root;
	//protected Operator[][] threats;				//threats (as operators), partitioned in arrays by category
	//protected final short MAX_CHILDREN = 10;

	//VARIABLES FOR A DB-SEARCH EXECUTION
	protected LinkedList<NodeBoard> lastCombination;
	protected LinkedList<NodeBoard> lastDependency;
	int possible_winning_sequences;
	/* used for combining nodes: at each combination, first COMBINED_N is increased, then the cells added from 
	 * a node's to another's board are marked in COMBINED as COMBINED_N;
	 * then the combination node, which must combine the two boards, starting from one, adds the other one's
	 * cells, only considering, for the implementation of markCells(), the cells in COMBINED marked as COMBINED_N
	 */
	private Combined COMBINED;
	private boolean[][] GOAL_SQUARES;

	
	protected NodeBoard win_node;


	FileWriter file = null;
	//protected Debug debug;
	protected int nodes_created;
	protected int nodes_alive;
	protected int nodes_created_tot;
	protected int nodes_alive_tot;
	

	
	
	//#region PLAYER

		public DbSearch() {

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
			//debug.open();
			nodes_created = 0;
			nodes_alive = 0;
			
			//start conting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(MC.length > 0) {
				MNKCell last_move = MC[MC.length - 1];
				//mark opponent cell
				board.markCell(last_move.i, last_move.j);
				// DEBUG
				System.out.println("last/opponent: " + MC[MC.length - 1]);
			}


			System.out.println("MC: ");
			for(int i = 0; i < MC.length; i++) System.out.println(MC[i]);
			try {
				file = new FileWriter("debug/db2/main" + MC.length + ".txt");
				DbTest.printBoard(board, file);
				DbTest.debugBoard(board, file, false, false, false);
				file.close();
			} catch (Exception e) {}



			//new root
			NodeBoard root = createRoot();
			boolean won = false;
			possible_winning_sequences = 0;

			//recursive call for each possible move
			try{
				won = visit(root, MY_MNK_PLAYER, true, Operators.MAX_TIER);
			} catch (NullPointerException e) {
				System.out.println("VISIT: NULL EXCEPTION");
				throw e;
			} catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("VISIT: ARRAY BOUNDS EXCEPTION");
				throw e;
			}
			// DEBUG
			System.out.println(won);
			//debug.markedCells(0);
			nodes_created_tot += nodes_created;
			nodes_alive_tot += nodes_alive;
			
			MNKCell best_move;
			if(won) {
				best_move = getBestMove();
				System.out.println("FOUND BEST MOVE: " + best_move);
			}
			else best_move = FC[0];
			//update my istance of board
//UNCOMMENT!!!
			//board.markCell(best_move.i, best_move.j);								//mark my cell

			// DEBUG
			//debug.info();
			//debug.close();
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
		protected boolean visit(NodeBoard root, MNKCellState attacker, boolean attacking, int max_tier) {
			short level = 1;
			boolean won = false;
			while(!isTimeEnded() && isTreeChanged() && !won && possible_winning_sequences < MAX_THREAT_SEQUENCES) {
				try {
					String filename_current = "debug/db2/db" + board.MC_n + "-" + level + ".txt";
					new File(filename_current);
					file = new FileWriter(filename_current);
				} catch(Exception e) {
					
				}
				
				lastDependency.clear();
				try {
					file.write("--------\tDEPENDENCY\t--------\n");
				} catch(Exception e) {}
				won = addDependencyStage(attacker);			//uses lastCombination, fills lastDependency
				lastCombination.clear();
				try {
					file.write("--------\tCOMBINATION\t--------\n");
				} catch(Exception e) {}
				if(!won) won = addCombinationStage(root, attacker);		//uses lasdtDependency, fills lastCombination
				level++;

				try {
					file.close();
				} catch (Exception e) {}
			}
			return won;
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		protected boolean addDependencyStage(MNKCellState attacker) {
			boolean won = false;
			ListIterator<NodeBoard> it = lastCombination.listIterator();
			while(it.hasNext() && !won) {
				NodeBoard node = it.next();
				int lev = 0;
				try {
					file.write("parent: \n");
					DbTest.printBoard(node.board, file);
					file.write("children: \n");
				} catch (Exception e) {}
				won = addDependentChildren(node, attacker, 0);
			}
			return won;
		}
		protected boolean addCombinationStage(NodeBoard root, MNKCellState attacker) {
			boolean won = false;
			ListIterator<NodeBoard> it = lastDependency.listIterator();
			while(it.hasNext() && !won) {
				NodeBoard node = it.next();
				try {
					file.write("parent: \n");
					DbTest.printBoard(node.board, file);
					file.write("children: \n");
				} catch (Exception e) {}
				won = findAllCombinationNodes(node, root, attacker);
			}
			return won;
		}

		protected boolean addDependentChildren(NodeBoard node, MNKCellState attacker, int lev) {
			if(node.board.gameState() == MY_WIN) {
				win_node = node;
				return true;
			}
			else if(node.board.gameState() == YOUR_WIN) return false;
			else {
				boolean won = false;
				//LinkedList<MNKCell[]> applicableOperators = getApplicableOperators(node, MAX_CHILDREN, my_attacker);
				RankedThreats applicableOperators = getApplicableOperators(node.board, attacker);
				for(LinkedList<Threat> tier : applicableOperators) {
					for(Threat f : tier) {
						NodeBoard newChild = addDependentChild(node, f);
						try {
							file.write("-" + lev + "\t---\n");
							DbTest.printBoard(newChild.board, file, lev);
							file.write("---\n");
						} catch (Exception e) {}
						won = addDependentChildren(newChild, attacker, lev+1);
						if(won) {
							//save best node
							break;
						}
					}
				}
				return won;
			}
		}
		/**
		 * @param partner : fixed node for combination
		 * @param node : iterating node for combination
		 */
		protected boolean findAllCombinationNodes(NodeBoard partner, NodeBoard node, MNKCellState attacker) {
			if(node == null) return false;
			else if(node.board.gameState() == MY_WIN) {
				win_node = node;
				return true;
			} else {
				boolean won = false;
				if(isDependencyNode(node) && partner.validCombinationWith(node, attacker)) {
					won = addCombination(partner, node);
					//iterate through children and siblings
				}
				if(won) {
					win_node = node;
					return true;
				}
				else if(findAllCombinationNodes(partner, node.getSibling(), attacker)) return true;
				else return findAllCombinationNodes(partner, node.getFirstChild(), attacker);
			}
		}
		
	//#endregion ALGORITHM

	
	
	//#region AUXILIARY
	
		//#region BOOL
			protected boolean isDependencyNode(NodeBoard node) {
				return !node.is_combination;
			}
			/* tree is changed if either lastdCombination o lastDependency are not empty;
			 * however, dependency node are created from other dependency nodes only in the same level,
			 * so such iteration would be useless
			 */
			protected boolean isTreeChanged() {
				return lastCombination.size() > 0;
			}
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
			protected NodeBoard createRoot() {
				NodeBoard root = new NodeBoard(board, true);
				lastCombination.clear();
				lastCombination.add(root);
				return root;
			}
			protected NodeBoard addChild(NodeBoard parent, MNKCell[] f, boolean is_combination) {
				NodeBoard newChild = new NodeBoard(parent.board, f, is_combination);
				parent.addChild(newChild);
				return newChild;
			}
			protected NodeBoard addDependentChild(NodeBoard node, MNKCell[] f) {
				NodeBoard newChild = addChild(node, f, false);
				lastDependency.add(newChild);
				return newChild;
			}
			// ENHANCEMENT: ONLY ADD COMBINATIONS WITH AT LEAST ONE OPERATOR APPLICABLE, SO YOU
			// DON'T ADD USELESS NODES
			protected boolean addCombination(NodeBoard A, NodeBoard B) {
				//create combination with A's board (copied)
				NodeBoard combination = new NodeBoard(A.board, true);
				A.addChild(combination);
				B.addChild(combination);
				combination.combine(B, COMBINED);				//add to it's board marks on B board
				lastCombination.add(combination);
				try {
					file.write("first parent: \n");
					DbTest.printBoard(A.board, file);
					file.write(".\n");
					file.write("second parent: \n");
					DbTest.printBoard(B.board, file);
					file.write(".\n");
					DbTest.printBoard(combination.board, file);
					file.write("---\n");
					file.write("---\n");
				} catch (Exception e) {}
				return combination.board.gameState() == MY_WIN;
			}
		//#endregion CREATE

		//#region GET
			protected RankedThreats getApplicableOperators(DbBoard board, MNKCellState attacker) {
				MNKCellState defender = Auxiliary.opponent(attacker);
				RankedThreats res = new RankedThreats();
				for(AlignmentsList dir_lines : board.lines_per_dir) {
					for(BiList_OpPos line : dir_lines) {
						if(line != null) {
							BiNode<OperatorPosition> node = line.getFirst(attacker);
							while(node != null) {
								Threat cell_threat_operators = Operators.applied(board, node.item, attacker, defender);
								if(cell_threat_operators != null) res.add(cell_threat_operators, Operators.threatTier(node.item.type));
								node = node.next;
							}
						}
					}
				}
				return res;
			}
			/*protected LinkedList<AppliedOperator> getApplicableOperators(NodeBoard node, short max, boolean my_attacker) {
				return getApplicableOperators(node.board, max, my_attacker);
			}
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
			}*/
			protected MovePair[] getGoalSquares(DbBoard board, short max, MNKCellState attacker) {
				RankedThreats threats = getApplicableOperators(board, attacker);
				int min = (threats.size() < max) ? threats.size() : max;
				MovePair[] res = new MovePair[min];
				ListIterator<MNKCell[]> it = threats.listIterator();
				for(int i = 0; i < min; i++) {
					MNKCell goal_square = Operators.threat(it.next(), attacker);
					res[i] = new MovePair(goal_square);
				}
				return res;
			}
		//#endregion GET
		//#region SET
			protected MNKCell getBestMove() {
				int i = board.MC_n;
				//return first player's move after initial state
				while(win_node.board.getMarkedCell(i).state != MY_MNK_PLAYER)
					i++;
				return win_node.board.getMarkedCell(i);
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
		protected void initAttributes() {
			board = new DbBoard(M, N, K);
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			//current_root = newNode();
			lastCombination = new LinkedList<NodeBoard>();
			lastDependency = new LinkedList<NodeBoard>();

			COMBINED = new Combined(M, N);
			GOAL_SQUARES = new boolean[M][N];
			for(int i = 0; i < M; i++)
				for(int j = 0; j < N; j++) GOAL_SQUARES[i][j] = false;

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
		}

		//protected abstract void initThreats();

	//#endregion INIT



	//#region CLASSES

		public class Combined {
			int[][] board;
			int n;
			private Combined(int M, int N) {
				board = new int[M][N];
				n = 0;
				for(int i = 0; i < M; i++)
					for(int j = 0; j < N; j++) board[i][j] = n;
			}
		}
	
	//#endregion CLASSES

	//#region DEBUG

	//#endregion DEBUG

}
