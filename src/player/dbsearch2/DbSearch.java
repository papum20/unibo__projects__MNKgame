package player.dbsearch2;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.sound.midi.MidiChannel;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import mnkgame.MNKPlayer;
import player.dbsearch2.BiList.BiNode;
import player.dbsearch2.NodeBoard.BoardsRelation;
import player.dbsearch2.Operators.RankedThreats;
import player.dbsearch2.Operators.Threat;
import player.dbsearch2.Operators.USE;
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
			
			//start counting time for this turn
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
			found_win_sequences = 0;
			possible_winning_sequences.clear();

			//recursive call for each possible move
			try{
				won = visit(root, MY_MNK_PLAYER, true, Operators.MAX_TIER, null);
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
			LinkedList<NodeBoard> lastDependency = new LinkedList<NodeBoard>(), lastCombination = new LinkedList<NodeBoard>();
			initLastCombination(root, lastCombination);
			short level = 1;
			boolean won = false, found_sequences = false;
			while(!isTimeEnded() && isTreeChanged(lastCombination) && !won && found_win_sequences < MAX_THREAT_SEQUENCES) {
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
				
				found_sequences = addDependencyStage(attacker, attacking, lastDependency, lastCombination);			//uses lastCombination, fills lastDependency
				
				try {
					file.write("--------\tDEFENSES\t--------\n");
				} catch(Exception e) {}

				//check for global defenses
				if(attacking && found_sequences) {
					won = !visitGlobalDefenses(root, attacker);
					found_sequences = false;
				}
				if(!won) {
					lastCombination.clear();
					
					try {
						file.write("--------\tCOMBINATION\t--------\n");
					} catch(Exception e) {}
					
					found_sequences = addCombinationStage(root, attacker, attacking, lastDependency, lastCombination);		//uses lasdtDependency, fills lastCombination
				}
				if(attacking && found_sequences) {
					won = visitGlobalDefenses(root, attacker);
					found_sequences = false;
				}
				level++;

				try {
					file.close();
				} catch (Exception e) {}
			}
			return won;
		}
		private boolean visitGlobalDefenses(NodeBoard root, MNKCellState attacker) {
			ListIterator<NodeBoard> it = possible_winning_sequences.listIterator();
			NodeBoard won_state = null;
			boolean won = false;
			while(!won && it.hasNext()) {
				//add each combination of attacker's made threats to each dependency node
				won_state = it.next();
				NodeBoard new_root = createDefensiveRoot(root, won_state.board.markedThreats);
				int first_threat_tier = won_state.board.getMarkedThreats().getFirst().type;
				//visit for defender
				markGoalSquares(won_state.board.getMarkedThreats(), true);
				won = !visit(new_root, Auxiliary.opponent(attacker), false, first_threat_tier);
				markGoalSquares(won_state.board.getMarkedThreats(), false);
			}
			if(won) win_node = won_state;
			return won;
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		protected boolean addDependencyStage(MNKCellState attacker, boolean attacking, LinkedList<NodeBoard> lastDependency, LinkedList<NodeBoard> lastCombination) {
			boolean won = false;
			ListIterator<NodeBoard> it = lastCombination.listIterator();
			while(it.hasNext() && !won) {
				NodeBoard node = it.next();
				try {
					file.write("parent: \n");
					DbTest.printBoard(node.board, file);
					file.write("children: \n");
				} catch (Exception e) {}
				won = addDependentChildren(node, attacker, attacking, 0, lastDependency);
			}
			return won;
		}
		protected boolean addCombinationStage(NodeBoard root, MNKCellState attacker, boolean attacking, LinkedList<NodeBoard> lastDependency, LinkedList<NodeBoard> lastCombination) {
			boolean won = false;
			ListIterator<NodeBoard> it = lastDependency.listIterator();
			while(it.hasNext() && !won) {
				NodeBoard node = it.next();
				try {
					file.write("parent: \n");
					DbTest.printBoard(node.board, file);
					file.write("children: \n");
				} catch (Exception e) {}
				won = findAllCombinationNodes(node, root, attacker, attacking, lastCombination);
			}
			return won;
		}

		protected boolean addDependentChildren(NodeBoard node, MNKCellState attacker, boolean attacking, int lev, LinkedList<NodeBoard> lastDependency) {
			MNKGameState state = node.board.gameState();
			if(state == MNKGameState.OPEN) {
				boolean won = false;
				//LinkedList<MNKCell[]> applicableOperators = getApplicableOperators(node, MAX_CHILDREN, my_attacker);
				RankedThreats applicableOperators = getApplicableOperators(node.board, attacker);
				for(LinkedList<Threat> tier : applicableOperators) {
					for(Threat threat : tier) {
						//if a goal square is marked, returns true, as goal squares are only used for defensive search, where only score matters
						int atk_index = threat.nextAtk(0);
						MovePair atk_cell = threat.related[atk_index];
						if(GOAL_SQUARES[atk_cell.i()][atk_cell.j()]) return true;
						else {
							NodeBoard newChild = addDependentChild(node, threat, atk_index, lastDependency);
							// DEBUG
							try {
								file.write("-" + lev + "\t---\n");
								DbTest.printBoard(newChild.board, file, lev);
								file.write("---\n");
							} catch (Exception e) {}

							won = addDependentChildren(newChild, attacker, attacking, lev+1, lastDependency);
							if(found_win_sequences >= MAX_THREAT_SEQUENCES) break;
						}
					}
				}
				return won;
			}
			else if(state == MNKGameState.DRAW) return !attacking;
			else if(state == Auxiliary.cellState2winState(attacker)) {
				if(attacking) {
					found_win_sequences++;
					possible_winning_sequences.add(node);
				}
				return true;
			}
			else return false;	//in case of loss or draw
		}
		/**
		 * @param partner : fixed node for combination
		 * @param node : iterating node for combination
		 */
		protected boolean findAllCombinationNodes(NodeBoard partner, NodeBoard node, MNKCellState attacker, boolean attacking, LinkedList<NodeBoard> lastCombination) {
			if(node == null || found_win_sequences >= MAX_THREAT_SEQUENCES) return false;
			else {
				MNKGameState state = node.board.gameState;
				if(state == MNKGameState.OPEN) {
					boolean won = false;
					//doesn't check if isDependencyNode() : also combinations of combination nodes could result in alignments
					NodeBoard.BoardsRelation relation = partner.validCombinationWith(node, attacker);
					if(relation != BoardsRelation.CONFLICT) {
						if(relation == BoardsRelation.USEFUL) won = addCombination(partner, node, lastCombination, attacker, attacking);
						if(findAllCombinationNodes(partner, node.getFirstChild(), attacker, attacking, lastCombination)) won = true;
					}
					if(findAllCombinationNodes(partner, node.getSibling(), attacker, attacking, lastCombination)) won = true;
					return won;
				}
				else if(state == MNKGameState.DRAW) return !attacking;
				else if(state == Auxiliary.cellState2winState(attacker)) {
					if(attacking) {
						found_win_sequences++;
						possible_winning_sequences.add(node);
					}
					return true;
				}
				else return false;
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
			protected boolean isTreeChanged(LinkedList<NodeBoard> lastCombination) {
				return lastCombination.size() > 0;
			}
			/*protected boolean isMyTurn() {
				return board.currentPlayer() == MY_PLAYER;
			}*/
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
			/*protected static boolean equalMNKMoves(MNKCell a, MNKCell b) {
				return a.i == b.i && a.j == b.j;
			}*/
		//#endregion BOOL
		
		//#region CREATE
			protected NodeBoard createRoot() {
				NodeBoard root = NodeBoard.copy(board, true, Operators.MAX_TIER, true);
				return root;
			}
			private NodeBoard createDefensiveRoot(NodeBoard root, LinkedList<AppliedThreat> threats) {
				ListIterator<AppliedThreat> it = threats.listIterator();
				AppliedThreat threat = it.next();
				//create defenisve root copying current root, using opponent as player and marking only the move made by the current attacker in the first threat
				NodeBoard def_root = NodeBoard.copy(root.board, true, (byte)(Operators.threatTier(threat.type) - 1), true);
				//all alignments are copied from root
				def_root.board.setPlayer(YOUR_MNK_PLAYER);
				//add a node for each threat, each node child/dependant from the previous one
				NodeBoard prev, node = def_root;
				while(it.hasNext()) {
					prev = node;
					prev.board.markCell(threat.atk);
					//update related alignments
					!
					node = NodeBoard.copy(prev.board, true, prev.max_threat, false);
					prev.addChild(node);
					node.board.markCells(threat.def);
					//update related alignments
					!
					threat = it.next();
				}
			}
			private void initLastCombination(NodeBoard node, LinkedList<NodeBoard> lastCombination) {
				if(node != null) {
					lastCombination.addLast(node);
					initLastCombination(node.getSibling(), lastCombination);
					initLastCombination(node.getFirstChild(), lastCombination);
				}
			}
			protected NodeBoard addDependentChild(NodeBoard node, Threat threat, int atk, LinkedList<NodeBoard> lastDependency) {
				DbBoard new_board = node.board.getDependant(threat, atk, USE.BTH, true, node.max_threat);
				NodeBoard newChild = new NodeBoard(new_board, false, node.max_threat);
				node.addChild(newChild);
				lastDependency.add(newChild);
				return newChild;
			}
			protected NodeBoard addCombinationChild(NodeBoard A, NodeBoard B, MNKCellState attacker, LinkedList<NodeBoard> lastCombination) {
				int max_threat = Math.min(A.max_threat, B.max_threat);
				DbBoard new_board = A.board.getCombined(B.board, attacker, max_threat);
				NodeBoard newChild = new NodeBoard(new_board, true, (byte)max_threat);
				A.addChild(newChild);
				B.addChild(newChild);
				lastCombination.add(newChild);
				return newChild;
			}
			// ENHANCEMENT: ONLY ADD COMBINATIONS WITH AT LEAST ONE OPERATOR APPLICABLE, SO YOU
			// DON'T ADD USELESS NODES
			protected boolean addCombination(NodeBoard A, NodeBoard B, LinkedList<NodeBoard> lastCombination, MNKCellState attacker, boolean attacking) {
				//create combination with A's board (copied)
				NodeBoard new_combination = addCombinationChild(A, B, attacker, lastCombination);
				try {
					file.write("first parent: \n");
					DbTest.printBoard(A.board, file);
					file.write(".\n");
					file.write("second parent: \n");
					DbTest.printBoard(B.board, file);
					file.write(".\n");
					DbTest.printBoard(new_combination.board, file);
					file.write("---\n");
					file.write("---\n");
				} catch (Exception e) {}
				MNKGameState state = new_combination.board.gameState();
				if(state == MNKGameState.OPEN) return false;
				if(state == MNKGameState.DRAW) return !attacking;
				else if(state == Auxiliary.cellState2winState(attacker)) {
					if(attacking) {
						found_win_sequences++;
						possible_winning_sequences.add(new_combination);
					}
					return true;
				}
				else return false;
			}
		//#endregion CREATE

		//#region GET
			protected RankedThreats getApplicableOperators(DbBoard board, MNKCellState attacker) {
				MNKCellState defender = Auxiliary.opponent(attacker);
				RankedThreats res = new RankedThreats();
				for(AlignmentsList dir_lines : board.lines_per_dir) {
					for(BiList_OpPos line : dir_lines) {
						if(line != null) {
							BiNode<OperatorPosition> alignment = line.getFirst(attacker);
							while(alignment != null) {
								Threat cell_threat_operator = Operators.applied(board, alignment.item, attacker, defender);
								if(cell_threat_operator != null) res.add(cell_threat_operator, Operators.threatTier(alignment.item.type));
								alignment = alignment.next;
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
			protected MNKCell getBestMove() {
				int i = board.MC_n;
				//return first player's move after initial state
				while(win_node.board.getMarkedCell(i).state != MY_MNK_PLAYER)
					i++;
				return win_node.board.getMarkedCell(i);
			}
		//#endregion GET
		//#region SET
			/**
			 * 
			 * @param threats : threats to mark as goal squares
			 * @param mark : true marks, false unmarks
			 */
			private void markGoalSquares(LinkedList<AppliedThreat> threats, boolean mark) {
				for(AppliedThreat t : threats) {
					GOAL_SQUARES[t.atk.i()][t.atk.j()] = mark;
					for(MovePair cell : t.def) GOAL_SQUARES[cell.i()][cell.j()] = mark;
				}
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
			possible_winning_sequences = new LinkedList<NodeBoard>();

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
