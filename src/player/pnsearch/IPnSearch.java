package player.pnsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.ArrayBoard;
import player.pnsearch.structures.Nodes;
import player.pnsearch.structures.Nodes.Move;
import player.pnsearch.structures.Nodes.Node_t;
import player.pnsearch.structures.Nodes.Value;




public abstract class IPnSearch<M extends Move, N extends Node_t<M,N,A>, A> implements mnkgame.MNKPlayer {

	protected int M;
	protected int N;
	protected int K;
	protected boolean first;
	protected long timeout_in_millisecs;

	protected MNKGameState MY_WIN;
	protected int MY_PLAYER;
	protected final short PROOF_N_ZERO = Nodes.PROOF_N_ZERO;
	protected final short PROOF_N_INFINITE = Nodes.PROOF_N_INFINITE;

	ArrayBoard board;

	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Runtime runtime;

	protected N current_root;

	protected Debug debug;
	protected int nodes_created;
	protected int nodes_alive;
	protected int nodes_created_tot;
	protected int nodes_alive_tot;
	



	//#region PLAYER	
	
		public IPnSearch() {

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
			System.out.println("------------------");
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
				//update current_root (with last opponent move)
				//assumption: current_root != null
			N new_root = current_root.findChild(opponent_move);
			if(new_root != null) {
				System.out.println("found move in tree.");
					nodes_alive_tot -= current_root.getChildrenLength();
					current_root = new_root;
					current_root.setParent(null);
				}
				else current_root.setMove(newMove(opponent_move));
				
				// DEBUG
				System.out.println("last/opponent: " + MC[MC.length - 1]);
			}
			// DEBUG
			debug.markedCells(0);
			if(current_root.getMove() != null) System.out.println(current_root.getPosition());
			//recursive call for each possible move
			visit(current_root);
			// DEBUG
			debug.markedCells(0);
			if(current_root.getMove() != null) System.out.println(current_root.getPosition());
			nodes_created_tot += nodes_created;
			nodes_alive_tot += nodes_alive;
			
			N best_node = getBestNode();
			MNKCell res = FC[0];
			if(best_node != null) {
				res = best_node.getPosition();
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
			return "IPnSearch";
		}

		//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * 
		 * @param root
		 */
		protected void visit(N root) {
			evaluate(root);
			setProofAndDisproofNumbers(root, true);
			while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {

				debug.node(root);
				
				N mostProvingNode = selectMostProving(root);
				
				debug.markedCells(0);
				debug.freeCells(0);
				debug.node(mostProvingNode);

				if(!isTimeEnded()) {
					developNode(mostProvingNode);
					updateAncestors(mostProvingNode);
				} else
					resetBoard(mostProvingNode, root);
				
				debug.node(mostProvingNode);
			}
			if(root.proof == 0) root.value = Value.TRUE;
			else if(root.disproof == 0) root.value = Value.FALSE;			
			else root.value = Value.UNKNOWN;
		}

		/**
		 * 
		 * @param node
		 */
		protected void evaluate(N node) {
			Value current = gameState_to_value(board.gameState());
			node.value = current;
		}
		/**
		 * 
		 * @param V
		 * @param my_turn
		 */
		protected void setProofAndDisproofNumbers(N node, boolean my_turn) {
			if(node.isExpanded()) {
				if(my_turn) node.setProofDisproof(node.getChildren_minProof(), node.getChildren_sumDisproof());
				else node.setProofDisproof(node.getChildren_sumProof(), node.getChildren_minDisproof());
			}
			else if(node.value != Value.UNKNOWN) {
				if(node.value == Value.TRUE) node.setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
				else node.setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			}
			else node.setProofDisproof((short)1, (short)1);
		}
		/**
		 * 
		 * @param node
		 * @return
		 */
		protected N selectMostProving(N node) {
			if(!node.isExpanded()) return node;
			else {
				N res = null;
				if(isMyTurn()) res = node.findChildProof(node.proof);
				else res = node.findChildDisproof(node.disproof);
				board.markCell(res.getPosition().i, res.getPosition().j);

				debug.nestedNode(node, 0);
				
				return selectMostProving(res);
			}
		}
		/**
		 * 
		 * @param node
		 */
		protected abstract void developNode(N node);
		/**
		 * 
		 * @param node
		 * @param my_turn
		 */
		protected void updateAncestors(N node) {
			while(node != null) {
				setProofAndDisproofNumbers(node, isMyTurn());

				debug.nestedNode(node, 0);

				if(node.getParent() != null) board.unmarkCell();
				node = node.getParent();
			}
		}
		/**
		 * 
		 * @param node
		 */
		protected void generateAllChildren(N node) {
			for(int i = 0; i < board.FreeCells_length(); i++)
				node.addChild(board.getFreeCell(i));
		}
		/**
		 * unmarks cells in board until current node reaches root node
		 * precondition: root is ancestor of current
		 * @param current : current node in game tree
		 * @param root : node to go back to, in game tree
		 */
		protected void resetBoard(N current, N root) {
			while(current != root) {
				current = current.getParent();
				board.unmarkCell();
			}
		}
		
	//#endregion ALGORITHM
	
	
	//#region AUXILIARY
	
	protected Value gameState_to_value(MNKGameState s) {
		if(s == MNKGameState.OPEN) return Value.UNKNOWN;
			else if(s == MY_WIN) return Value.TRUE;
			else return Value.FALSE;
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
		protected abstract N getBestNode();
		
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
			current_root = newNode();

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
			debug = new Debug("debug/debug-" + playerName(), false);
		}

		// create N object
		protected abstract M newMove(MNKCell move);
		protected abstract N newNode();

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
			protected void freeCells(int minus) {
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
			protected void markedCells(int minus) {
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
			protected void node(N node) {
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
			protected void nestedNode(N node, int minus) {
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
			protected void info() {
				System.out.println("time" + Long.toString(System.currentTimeMillis() - timer_start));
				System.out.println("nodes created:\t\t" + Integer.toString(nodes_created));
				System.out.println("nodes alive:\t\t" + Integer.toString(nodes_alive));
				System.out.println("nodes created tot:\t" + Integer.toString(nodes_created_tot));
				System.out.println("nodes alive tot:\t" + Integer.toString(nodes_alive_tot));
			}
			// return string with a tab for each depth level
			protected String tabs(int minus) {
				String tab = "\t";
				for(int i = 0; i < board.MarkedCells_length(); i++) tab += "\t";
				return tab;
			}

		}
	
	//#endregion DEBUG


}
