import player.dbsearch2.NodeBoard;

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
	private int found_win_sequences;
	private LinkedList<NodeBoard> possible_winning_sequences;
	protected NodeBoard win_node;
	/* used for combining nodes: at each combination, first COMBINED_N is increased, then the cells added from 
	 * a node's to another's board are marked in COMBINED as COMBINED_N;
	 * then the combination node, which must combine the two boards, starting from one, adds the other one's
	 * cells, only considering, for the implementation of markCells(), the cells in COMBINED marked as COMBINED_N
	 */
	private Combined COMBINED;
	private boolean[][] GOAL_SQUARES;


	FileWriter file = null;
	//protected Debug debug;
	protected int nodes_created;
	protected int nodes_alive;
	protected int nodes_created_tot;
	protected int nodes_alive_tot;
	

	
	
	//#region PLAYER


		public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
			initParameters(M, N, K, first, timeout_in_secs);
			initCellStates(first);								//init MY_PLAYER, ... : constants
			initAttributes();									//init possible winning sequences, combined
		}

		
		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
			nodes = 0;
			
			//start counting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(MC.length > 0) {
				MNKCell last_move = MC[MC.length - 1];
				//mark opponent cell
				board.markCell(last_move.i, last_move.j);
			}

			//new root
			NodeBoard root = createRoot();
			boolean won = false;
			found_win_sequences = 0;
			possible_winning_sequences.clear();

			//recursive call for each possible move
			won = visit(root, MY_MNK_PLAYER, true, Operators.MAX_TIER, null);

			nodes_tot += nodes;
			
			MNKCell best_move;
			if(won) {
				best_move = getBestMove();
			}
			else best_move = FC[0];
			//update my istance of board
//UNCOMMENT!!!
			//board.markCell(best_move.i, best_move.j);								//mark my cell

			return best_move;
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
		@OK
		protected boolean visit(NodeBoard root, MNKCellState attacker, boolean attacking, int max_tier) {
			LinkedList<NodeBoard> lastDependency, lastCombination;
			initLastCombination(root, lastCombination);
			short level = 1;
			boolean won = false, found_sequences = false;
			while(!isTimeEnded() && isTreeChanged(lastCombination) && !won && found_win_sequences < MAX_THREAT_SEQUENCES) {

				lastDependency.clear();
				found_sequences = addDependencyStage(attacker, lastDependency, lastCombination);			//uses lastCombination, fills lastDependency
				
				//check for global defenses
				if(attacking && found_sequences) {
					won = !visitGlobalDefenses(root, attacker);
					found_sequences = false;
				}
				if(!won) {
					lastCombination.clear();
					found_sequences = addCombinationStage(root, attacker, lastDependency);		//uses lasdtDependency, fills lastCombination
				}

				if(attacking && found_sequences) {
					won = visitGlobalDefenses(root, attacker);
					found_sequences = false;
				}
				level++;
			}
			return won;
		}
		@OK
		private boolean visitGlobalDefenses(NodeBoard root, MNKCellState attacker) {
			while(!won && it.hasNext()) {
				//add each threat in the sequence as a child node of (defensive) root
				new_root = createDefensiveRoot(root, won_state.board.markedThreats);
				markGoalSquares(true);
				won = !visit(new_root);
				markGoalSquares(false);
			}
			if(won) win_node = won_state;
			return won;
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		@OK
		protected boolean addDependencyStage(MNKCellState attacker, LinkedList<NodeBoard> lastDependency, LinkedList<NodeBoard> lastCombination) {
			while(lastCombination.hasNext() && !won)
				won = addDependentChildren(node, attacker, 0, lastDependency);
			return won;
		}
		@OK
		protected boolean addCombinationStage(NodeBoard root, MNKCellState attacker, LinkedList<NodeBoard> lastDependency) {
			while(it.hasNext() && !won) {
				won = findAllCombinationNodes(node, root, attacker);
			}
			return won;
		}

		@OK
		protected boolean addDependentChildren(NodeBoard node, MNKCellState attacker, int lev, LinkedList<NodeBoard> lastDependency) {
			if(state != open) return result;
			else {
				for(Each i : getApplicableOperators(node.board, attacker)) {
					NodeBoard newChild = addDependentChild(node, f, lastDependency);
					won = addDependentChildren(newChild, attacker, lev+1, lastDependency);
				}
				return won;
			}
		}
		/**
		 * @param partner : fixed node for combination
		 * @param node : iterating node for combination
		 */
		@OK
		protected boolean findAllCombinationNodes(NodeBoard partner, NodeBoard node, MNKCellState attacker) {
			if(node == null) return false;
			if(state != open) return result;
			else {
				if(relation != CONFLICT) {
					if(relation == USEFUL) addCombination(partner, node);
					findAllCombinationNodes(partner, node.child);
				}
				findAllCombinationNodes(partner, node.sibling());
				return won;
			}
		}
		
	//#endregion ALGORITHM

	
	
	//#region AUXILIARY
	
		//#region BOOL
			/*protected boolean isDependencyNode(NodeBoard node) {
				return !node.is_combination;
			}*/
			/* tree is changed if either lastdCombination o lastDependency are not empty;
			 * however, dependency node are created from other dependency nodes only in the same level,
			 * so such iteration would be useless
			 */
			@OK
			protected boolean isTreeChanged(LinkedList<NodeBoard> lastCombination) {
				return lastCombination.size() > 0;
			}
			/*protected boolean isMyTurn() {
				return board.currentPlayer() == MY_PLAYER;
			}*/
			//returns true if it's time to end the turn
			@OK
			protected boolean isTimeEnded() {
				return (System.currentTimeMillis() - timer_start) >= timer_end;
			}
			//returns true if available memory is less than a small percentage of max memory
			@OK
			protected boolean isMemoryEnded() {
				// max memory useable by jvm - (allocatedMemory = memory actually allocated by system for jvm - free memory in totalMemory)
				long freeMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
				return freeMemory < runtime.maxMemory() * (5 / 100);
			}
			/*protected static boolean equalMNKMoves(MNKCell a, MNKCell b) {
				return a.i == b.i && a.j == b.j;
			}*/
		//#endregion BOOL
		
		//#region CREATE
			@OK
			protected NodeBoard createRoot() {
				NodeBoard root = new NodeBoard(board, true, Operators.MAX_TIER, true);
				return root;
			}
			@OK	//MISSING update related alignments for markCell
			private NodeBoard createDefensiveRoot(NodeBoard root, LinkedList<AppliedThreat> threats) {
				NodeBoard def_root = new NodeBoard(root);		//create defenisve root copying current root, using opponent as player
				//all alignments are copied from root
			}
			@OK
			private void initLastCombination(NodeBoard node, LinkedList<NodeBoard> lastCombination) {
				if(node != null) {
					lastCombination.addLast(node);
					initLastCombination(node.getSibling(), lastCombination);
					initLastCombination(node.getFirstChild(), lastCombination);
				}
			}
			//dependent child's board only has alignments for newly marked cells
			@OK
			protected NodeBoard addDependentChild(NodeBoard node, MNKCell[] f, LinkedList<NodeBoard> lastDependency) {};
			@OK
			protected NodeBoard addCombinationChild(NodeBoard node, MNKCell[] f, LinkedList<NodeBoard> lastDependency);
			// ENHANCEMENT: ONLY ADD COMBINATIONS WITH AT LEAST ONE OPERATOR APPLICABLE, SO YOU
			// DON'T ADD USELESS NODES
			@OK
			protected boolean addCombination(NodeBoard A, NodeBoard B, LinkedList<NodeBoard> lastCombination) {
				addCombinationChild(A, B);
				return won;
			}
		//#endregion CREATE

		//#region GET
			@OK
			protected RankedThreats getApplicableOperators(DbBoard board, MNKCellState attacker) {
				//get all threats from all alignments on the board
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
			/*protected MovePair[] getGoalSquares(DbBoard board, short max, MNKCellState attacker) {
				RankedThreats threats = getApplicableOperators(board, attacker);
				int min = (threats.size() < max) ? threats.size() : max;
				MovePair[] res = new MovePair[min];
				ListIterator<MNKCell[]> it = threats.listIterator();
				for(int i = 0; i < min; i++) {
					MNKCell goal_square = Operators.threat(it.next(), attacker);
					res[i] = new MovePair(goal_square);
				}
				return res;
			}*/
			//return first player's move after initial state
			protected MNKCell getBestMove();

		//#endregion GET

		//#region SET

			//true: marks, false: unmarks cells
			@OK
			private void markGoalSquares(LinkedList<AppliedThreat> threats, boolean mark);
			
		//#endregion SET
		
	//#endregion AUXILIARY
	

	//#region CLASSES

		public class Combined {
			int[][] board;
			int n;
			//init board to n, n=0
			private Combined(int M, int N) {}
		}
	
	//#endregion CLASSES


}
