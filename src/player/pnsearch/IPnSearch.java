package player.pnsearch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.ArrayBoard;
import player.pnsearch.Nodes.Move;
import player.pnsearch.Nodes.Node_t;
import player.pnsearch.Nodes.Value;




public class IPnSearch<M extends Move, N extends Node_t<M,N>> implements mnkgame.MNKPlayer {

	protected int M;
	protected int N;
	protected int K;
	protected boolean first;
	protected long timeout_in_millisecs;

	protected MNKGameState MY_WIN;
	protected int MY_PLAYER;

	ArrayBoard board;
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer

	protected N current_root;

	protected FileWriter debugFile;
	protected String debugName;
	


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
			/*
			try {
				debugFile = new FileWriter(debugName + MC.length + ".txt");
			} catch (IOException e) {
				System.out.println("WTF");
			}
			*/		

			//start conting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(MC.length > 0) {
				MNKCell opponent_move = MC[MC.length - 1];
				//mark opponent cell
				board.markCell(opponent_move.i, opponent_move.j);
				//update current_root (with last opponent move)
				//assumption: current_root != null
				Node new_root = current_root.findChild(opponent_move);
				if(new_root != null) {
					current_root = new_root;
					current_root.setParent(null);
				}
				else current_root.setMove(new Move(opponent_move));
			}
			// DEBUG
			String made = "";
			for(int i = 0; i < board.MarkedCells_length(); i++)
			made += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
			System.out.println("MC: " + made);
			if(current_root.move != null) System.out.println(current_root.move.position);
			//recursive call for each possible move
			visit(current_root);
			// DEBUG
			made = "";
			for(int i = 0; i < board.MarkedCells_length(); i++)
			made += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
			System.out.println("MC: " + made);
			if(current_root.move != null) System.out.println(current_root.move.position);
			
			Node best_node = getBestNode();
			MNKCell res = FC[0];
			if(best_node != null) res = best_node.move.position;
			//update my istance of board
			board.markCell(res.i, res.j);								//mark my cell
			//update current_root (with my last move)
			current_root = best_node;

			// DEBUG
			System.out.println("time" + Long.toString(System.currentTimeMillis() - timer_start));
			/*
			try {
				debugFile.close();
			} catch(IOException e) {
				System.out.println("WTF");
			}
			*/

			return res;
		}
		
		/**
			 * Returns the player name
			 *
			* @return string 
		*/
		public String playerName() {
			return "PnSearch";
		}

		//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * 
		 * @param <N>
		 * @param root
		 */
		protected <M extends Move, N extends Node_t<M,N>> void visit(N root) {
			evaluate(root);
			setProofAndDisproofNumbers(root, true);
			while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
				// DEBUG
				/*
				System.out.println( "\t" + Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + "(r)" + ((root.move == null) ? "root" : root.move.position) + " " + Short.toString(root.proof) + " " + Short.toString(root.disproof) );
				try {
					debugFile.write( "\t" + Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + "(r)" + ((root.move == null) ? "root" : root.move.position) + " " + Short.toString(root.proof) + " " + Short.toString(root.disproof) + "\n" );
				} catch(IOException e) {
					System.out.println("WTF");
				}
				*/
				N mostProvingNode = selectMostProving(root);
				// DEBUG
				/*
				System.out.println( Integer.toString(board.MarkedCells_length()) + ((mostProvingNode.move == null || isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) );
				try {
					String made = "", free = "";
					for(int i = 0; i < board.MarkedCells_length(); i++)
					made += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
					for(int i = 0; i < board.FreeCells_length(); i++)
					free += Integer.toString(board.getFreeCell(i).i) + Integer.toString(board.getFreeCell(i).j) + " ";
					debugFile.write("MC: " + made + "\n" + "FC: " + free + "\n");
					debugFile.write( Integer.toString(board.MarkedCells_length()) + ((mostProvingNode.move == null || isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) + "\n" );
						} catch(IOException e) {
							System.out.println("WTF");
						}
						*/
						//if(!isTimeEnded()) {
							developNode(mostProvingNode);
							updateAncestors(mostProvingNode);
					// DEBUG
					/*
					System.out.println( Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) );
					try {
						debugFile.write( Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) + "\n" );
					} catch(IOException e) {
						System.out.println("WTF");
					}
					*/
				//}
			}
			if(root.proof == 0) root.value = Value.TRUE;
			else if(root.disproof == 0) root.value = Value.FALSE;			
			else root.value = Value.UNKNOWN;
		}

		/**
		 * 
		 * @param <N>
		 * @param V
		 */
		protected <M extends Move, N extends Node_t<M,N>> void evaluate(N node) {
			Value current = gameState_to_value(board.gameState());
			node.value = current;
		}
		/**
		 * 
		 * @param <N>
		 * @param V
		 * @param my_turn
		 */
		protected <M extends Move, N extends Node_t<M,N>> void setProofAndDisproofNumbers(N node, boolean my_turn) {
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
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 * @return
		 */
		protected <M extends Move, N extends Node_t<M,N>> N selectMostProving(N node) {
			if(!node.isExpanded()) return node;
			else {
				// DEBUG
				/*
				String mc = "";
				for(int i = 0; i < board.MarkedCells_length(); i++) mc += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
				System.out.println("mc: " + mc);
				System.out.println(node.move.position);
				*/

				N res = null;
				if(isMyTurn()) {
					for(N child : node.children) {
						/*System.out.println( "\t" + child.move.position + " " + Short.toString(child.proof) + Short.toString(child.disproof) );
						try {
							debugFile.write( "\t" + child.move.position + " " + Short.toString(child.proof) + Short.toString(child.disproof) + "\n" );
						} catch(IOException e) {
							System.out.println("WTF");
						}*/
						if(child.proof == node.proof) {
							res = child;
							break;
						}
					}
				} else {
					for(N child : node.children) {
						/*System.out.println( "\t" + child.move.position + " " + Short.toString(child.proof) + Short.toString(child.disproof) );
						try {
							debugFile.write( "\t" + child.move.position + " " + Short.toString(child.proof) + Short.toString(child.disproof) + "\n" );
						} catch(IOException e) {
							System.out.println("WTF");
						}*/
						if(child.disproof == node.disproof) {
							res = child;
							break;
						}
					}
				}
				// DEBUG
				/*
				String tab = "\t";
				for(int i = 0; i < board.MarkedCells_length(); i++) tab += "\t";
				System.out.println( tab + ((!isMyTurn()) ? "P" : "D") + res.move.position + " " + Short.toString(res.proof) + " " + Short.toString(res.disproof) );
				try {
					debugFile.write( tab + ((!isMyTurn()) ? "P" : "D") + res.move.position + " " + Short.toString(res.proof) + " " + Short.toString(res.disproof) + "\n" );
				} catch(IOException e) {
					System.out.println("WTF");
				}
				*/
				board.markCell(res.move.position.i, res.move.position.j);
				return selectMostProving(res);
			}
		}
		
		protected <M extends Move, N extends Node_t<M,N>> void developNode(N node) {
			node.expand();
			generateAllChildren(node);
			for(N child : node.children) {
				board.markCell(child.move.position.i, child.move.position.j);
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
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
		protected <M extends Move, N extends Node_t<M,N>> void updateAncestors(N node) {
			while(node != null) {
				setProofAndDisproofNumbers(node, isMyTurn());
				if(node.getParent() != null) board.unmarkCell();

				// DEBUG
				/*
				String tab = "\t";
				for(int i = 0; i < board.MarkedCells_length() - ((node.getParent() == null) ? 1 : 0); i++) tab += "\t";
				System.out.println( tab + ((node.getParent() == null || !isMyTurn()) ? "P" : "D") + ((node.getParent() == null) ? "root" : node.move.position) + " " + Short.toString(node.proof) + " " + Short.toString(node.disproof) );
				try {
					debugFile.write( tab + ((node.getParent() == null || !isMyTurn()) ? "P" : "D") + ((node.getParent() == null) ? "root" : node.move.position) + " " + Short.toString(node.proof) + " " + Short.toString(node.disproof) + "\n" );
				} catch(IOException e) {
					System.out.println("WTF");
				}
				*/
				node = node.getParent();
			}
		}
		
		protected <M extends Move, N extends Node_t<M,N>> void generateAllChildren(N node) {
			for(int i = 0; i < board.FreeCells_length(); i++)
				node.addChild(board.getFreeCell(i));
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
		protected boolean isMyTurn() {
			return board.currentPlayer() == MY_PLAYER;
		}
		//returns move to make on this turn
		protected Node getBestNode() {
			// if found winning move: return it (i.e. the child move that is winning too)
			if(current_root.children.size() == 0) return null;
			else {
				Node best = current_root.children.getFirst();
				if(current_root.proof == 0) {
					for(Node child : current_root.children) {
						if(child.proof == 0) {
							best = child;
							break;
						}
					}
				}
				// else: return the move with highest (proof-disproof)
				else {
					for(Node child : current_root.children)
						if(child.proof - child.disproof > best.proof - best.disproof) best = child;
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
			current_root = new Node();
			
			debugName = "debug-pnsearch";
			/*try {
				debugFile = new FileWriter("debug-pnsearch.txt");
			} catch (IOException e) {
				System.out.println("WTF");
			}*/
		}

	//#endregion INIT


}
