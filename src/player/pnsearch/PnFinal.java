package player.pnsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.boards.ArrayBoard;



public class PnFinal implements mnkgame.MNKPlayer {
	
	protected int M;
	protected int N;
	protected int K;
	protected boolean first;
	protected long timeout_in_millisecs;

	protected MNKGameState MY_WIN;
	protected int MY_PLAYER;
	protected final short PROOF_N_ZERO = PnNode.PROOF_N_ZERO;
	protected final short PROOF_N_INFINITE = PnNode.PROOF_N_INFINITE;

	protected ArrayBoard board;

	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Runtime runtime;

	protected PnNode current_root;

	protected Debug debug;
	protected int nodes_created;
	protected int nodes_alive;
	protected int nodes_created_tot;
	protected int nodes_alive_tot;


	//#region PLAYER

		public PnFinal() {

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
				MovePair opponent_move = new MovePair(MC[MC.length - 1]);
				//mark opponent cell
				board.markCell(opponent_move.i(), opponent_move.j());
				//update current_root (with last opponent move)
				//assumption: current_root != null
				PnNode new_root = current_root.findChild(opponent_move);
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
			
			PnNode best_node = getBestNode();
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

		public String playerName() {
			return "PnSearchAStoreD";
		}
		
	//#endregion PLAYER

	//#region ALGORITHM

		
		protected void visit(PnNode root) {
			String exception = "";
			long select_time_start = 0;
			long select_time_end = 0;
			try {
				exception = "evaluate root";
				evaluate(root);
				exception = "set proof root";
				setProofAndDisproofNumbers(root, true);
				PnNode currentNode = root;
				while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
				
					debug.node(root);

					exception = "select most proving";
					select_time_start = System.currentTimeMillis() - timer_start;
					PnNode mostProvingNode = selectMostProving(currentNode);
					select_time_end = System.currentTimeMillis() - timer_start;
					
					debug.markedCells(0);
					debug.freeCells(0);
					debug.node(mostProvingNode);

					exception = "reset board 1";
					if(!isTimeEnded()) {
						exception = "develop";
						developNode(mostProvingNode);
						exception = "ancestors up to";
						currentNode = updateAncestorsUpto(mostProvingNode);
					} else
						resetBoard(mostProvingNode, root);

					debug.node(mostProvingNode);
				}
				// unmark all cells up to root
				exception += ", reset board 2";
				resetBoard(currentNode, root);
				// set root value
				/*
				if(root.proof == 0) root.value = Value.TRUE;
				else if(root.disproof == 0) root.value = Value.FALSE;			
				else root.value = Value.UNKNOWN;
				*/
			} finally {
				System.out.println("VISIT: " + exception);
				System.out.println("VISIT: last select:");
				System.out.println("\tstart =\t" + select_time_start);
				System.out.println("\tend =\t" + select_time_end);
			}
		}

		
		/**
		 * 
		 * @param node
		 */
		protected void evaluate(PnNode node) {
			PnNode.Value current = gameState_to_value(board.gameState());
			node.setValue(current);
		}



		/**
		 * 
		 * @param V
		 * @param my_turn
		 */
		protected void setProofAndDisproofNumbers(PnNode node, boolean my_turn) {
			if(node.isExpanded()) {
				PnNode most_proving;
				if(my_turn) {
					most_proving = node.getChildren_minProof();
					node.setProofDisproof(most_proving.proof, node.getChildren_sumDisproof());
				}
				else {
					most_proving = node.getChildren_minDisproof();
					node.setProofDisproof(node.getChildren_sumProof(), most_proving.disproof);
				}
				node.most_proving = most_proving;
			}
			else if(node.getValue() != PnNode.Value.UNKNOWN) {
				if(node.getValue() == PnNode.Value.TRUE) node.setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
				else node.setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			}
			else initProofAndDisproofNumbers(node);
		}
	
		//probably the else is useless, because that condition will never happen; check needed
		protected PnNode selectMostProving(PnNode node) {
			if(!node.isExpanded()) return node;
			else if(node.most_proving != null) {
				board.markCell(node.most_proving.i(), node.most_proving.j());
				return selectMostProving(node.most_proving);
			}
			else {
				PnNode res = null;
				if(isMyTurn()) res = node.findChildProof(node.proof);
				else res = node.findChildDisproof(node.disproof);
				board.markCell(res.i(), res.j());

				debug.nestedNode(node, 0);

				node.most_proving = res;
				return res;
			}
		}

		/**
		 * 
		 * @param node
		 */
		protected void developNode(PnNode node) {
			node.expand(board.FreeCells_length());
			generateAllChildren(node);
			for(int i = 0; i < node.getChildrenLength(); i++) {
				PnNode child = node.children[i];
				board.markCell(child.i(), child.j());
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
				board.unmarkCell();
			}
			nodes_created += node.getChildrenLength();
			nodes_alive += node.getChildrenLength();
		}

		/**
		 * 
		 * @param node
		 */
		protected void generateAllChildren(PnNode node) {
			for(int i = 0; i < board.FreeCells_length(); i++)
				node.addChild(new MovePair(board.getFreeCell(i)));
		}
		/**
		 * unmarks cells in board until current node reaches root node
		 * precondition: root is ancestor of current
		 * @param current : current node in game tree
		 * @param root : node to go back to, in game tree
		 */
		protected void resetBoard(PnNode current, PnNode root) {
			while(current != root) {
				current = current.getParent();
				board.unmarkCell();
			}
		}

		/**
		 * 
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 */
		protected PnNode updateAncestorsUpto(PnNode node) {
			PnNode previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = (oldProof != node.proof || oldDisproof != node.disproof);
				
				debug.nestedNode(node, 0);
				
				// delete children
				if((node.proof == 0 || node.disproof == 0) && node != current_root && node.getParent() != current_root) {
					nodes_alive -= node.getChildrenLength();
					node.prove( (node.proof == 0) ? PnNode.Value.TRUE : PnNode.Value.FALSE);
				}
				// update ancestors
				previousNode = node;
				node = node.getParent();
				if(node != null && changed) board.unmarkCell();
			}
			return previousNode;
		}

	//#endregion ALGORITHM

	//#region AUXILIARY

		/**
		 * initializes proof and disproof for node
		 * @param node
		 */
		protected void initProofAndDisproofNumbers(PnNode node) {
			node.setProofDisproof((short)1, (short)1);
		}
	
		protected PnNode.Value gameState_to_value(MNKGameState s) {
			if(s == MNKGameState.OPEN) return PnNode.Value.UNKNOWN;
				else if(s == MY_WIN) return PnNode.Value.TRUE;
				else return PnNode.Value.FALSE;
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
		protected boolean isMyTurn() {
			return board.currentPlayer() == MY_PLAYER;
		}

		//returns move to make on this turn
		protected PnNode getBestNode() {
			// if found winning move: return it (i.e. the child move that is winning too)
			if(!current_root.isExpanded() || current_root.getChildrenLength() == 0) return null;
			else {
				PnNode best = current_root.getFirstChild();
				if(current_root.proof == 0) {
					for(int i = 0; i < current_root.getChildrenLength(); i++) {
						PnNode child = current_root.children[i];
						if(child.proof == 0) {
							best = child;
							break;
						}
					}
				}
				// else: return the move with highest (proof-disproof)
				else {
					for(int i = 0; i < current_root.getChildrenLength(); i++) {
						PnNode child = current_root.children[i];
						if(child.proof - child.disproof > best.proof - best.disproof) best = child;
					}
				}
				return best;
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
			current_root = new PnNode();

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
			debug = new Debug("debug/debug-" + playerName(), false);
		}

	//#endregion INIT	
	
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
			public void node(PnNode node) {
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
			public void nestedNode(PnNode node, int minus) {
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
