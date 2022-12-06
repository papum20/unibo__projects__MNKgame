package player.dbsearch2;

import java.util.LinkedList;
import java.util.ListIterator;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import mnkgame.MNKPlayer;
import player.dbsearch2.BiList.BiNode;
import player.pnsearch.structures.INodes.MovePair;



public class DbSearch implements MNKPlayer {
	
/*
 * INSTEAD OF NODES CONTAINING BOARD, THEY COULD CONTAIN SEQUENCE OF OPERATORS APPLIED,
 * IN ORDER, SO THAT, IN A COMBINATION WITH NODES A AND B, IT IS SUFFICIENT TO USE THE OPERATORS
 * UP TO THE POINT IN COMMON (MAYBE SAVED AS LEVEL, POINTER OR OTHER)
 */

	/* used for combining nodes: at each combination, first COMBINED_N is increased, then the cells added from 
	 * a node's to another's board are marked in COMBINED as COMBINED_N;
	 * then the combination node, which must combine the two boards, starting from one, adds the other one's
	 * cells, only considering, for the implementation of markCells(), the cells in COMBINED marked as COMBINED_N
	 */
	private Combined COMBINED;
 
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

	protected DbBoard board;
	//protected MNKCell best_move;
	//protected MNKCell last_move;
	
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Runtime runtime;

	////protected N current_root;
	//protected Operator[][] threats;				//threats (as operators), partitioned in arrays by category
	//protected short max_tier;
	//protected final short MAX_CHILDREN = 10;

	protected LinkedList<NodeBoard> lastCombination;
	protected LinkedList<NodeBoard> lastDependency;

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

			//new root
			NodeBoard root = createRoot();
			boolean won = false;

			//recursive call for each possible move
			try{
				won = visit(root, true, getGoalSquares(board, SHORT_INFINITE, true), true, max_tier);
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
			
			if(best_move != null) System.out.println("FOUND BEST NODE");
			else best_move = FC[0];
			//update my istance of board
			board.markCell(best_move.i, best_move.j);								//mark my cell

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
		protected boolean visit(NodeBoard root, boolean my_attacker, MovePair[] goal_squares, boolean attacking, short max_tier) {
			//short level = 1;
			boolean won = false;
			while(!isTimeEnded() && isTreeChanged() && !won) {
				lastDependency.clear();
				won = addDependencyStage(my_attacker);			//uses lastCombination, fills lastDependency
				lastCombination.clear();
				if(!won) won = addCombinationStage(root);		//uses lasdtDependency, fills lastCombination
				//level++;
			}
			return won;
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		protected boolean addDependencyStage(boolean my_attacker) {
			boolean won = false;
			ListIterator<NodeBoard> it = lastCombination.listIterator();
			while(it.hasNext() && !won)
				won = addDependentChildren(it.next(), my_attacker);
			return won;
		}
		protected boolean addCombinationStage(NodeBoard root) {
			boolean won = false;
			ListIterator<NodeBoard> it = lastDependency.listIterator();
			while(it.hasNext() && !won)
				won = findAllCombinationNodes(it.next(), root);
			return won;
		}

		protected boolean addDependentChildren(NodeBoard node, boolean my_attacker) {
			//node.board.gameState()
			if(board.gameState() == MY_WIN) {
				setBestMove();
				return true;
			}
			else if(board.gameState() == YOUR_WIN) return false;
			else {
				boolean won = false;
				//LinkedList<MNKCell[]> applicableOperators = getApplicableOperators(node, MAX_CHILDREN, my_attacker);
				LinkedList<MNKCell[]> applicableOperators = getApplicableOperators(node.board, my_attacker);
				for(MNKCell[] f : applicableOperators) {
					NodeBoard newChild = addDependentChild(node, f);
					APPLY!!!
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
		protected boolean findAllCombinationNodes(NodeBoard partner, NodeBoard node) {
			//node.board
			if(board.gameState() == MY_WIN) {
				setBestMove();
				return true;
			}
			else if(node != null) {
				boolean won = false;
				if(!partner.inConflict(node)) {
					if(isDependencyNode(node)) won = addCombination(partner, node);
					//iterate through children and siblings
				}
				if(won) {
					setBestMove();
					return true;
				}
				else if(findAllCombinationNodes(partner, node.getSibling())) return true;
				else return findAllCombinationNodes(partner, node.getFirstChild());
			}
			else return false;
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
				NodeBoard newChild = new NodeBoard(board, f, is_combination);
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
				combination.combine(B);				//add to it's board marks on B board
				lastCombination.add(combination);
				return combination.board.gameState() == MY_WIN;
			}
		//#endregion CREATE

		//#region GET
			protected LinkedList<MNKCell[]> getApplicableOperators(DbBoard board, boolean my_attacker) {
				short tier = 0, i = 0, j = 0;
				MNKCellState attacker, defender;
				if(my_attacker) {
					attacker = MY_MNK_PLAYER;
					defender = YOUR_MNK_PLAYER;
				} else {
					attacker = YOUR_MNK_PLAYER;
					defender = MY_MNK_PLAYER;
				}
				LinkedList<MNKCell[]> res = new LinkedList<MNKCell[]>();
				for(AlignmentsList dir_lines : board.lines_per_dir) {
					for(BiList_OpPos line : dir_lines) {
						if(line != null) {
							BiNode<OperatorPosition> node = line.getFirst(attacker);
							while(node != null) {
								MNKCell[][] cell_operators = Operators.apply(board, node.item, attacker, defender);
								if(cell_operators != null)
									for(MNKCell[] operator : cell_operators) res.add(operator);
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
			protected MovePair[] getGoalSquares(DbBoard board, short max, boolean my_attacker) {
				//LinkedList<MNKCell[]> threats = getApplicableOperators(board, max, my_attacker);
				LinkedList<MNKCell[]> threats = getApplicableOperators(board, my_attacker);
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
		protected void initAttributes() {
			board = new DbBoard(M, N, K);
			last_move = null;
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			//current_root = newNode();
			lastCombination = new LinkedList<NodeBoard>();
			lastDependency = new LinkedList<NodeBoard>();

			COMBINED = new Combined(M, N);

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
