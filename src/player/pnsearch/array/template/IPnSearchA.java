/*
 * IPnSearch (STILL AN "INTERFACE-CLASS", NOT TRUE CLASS)
 * WHICH USES A java Collection AS Node.children
 */


package player.pnsearch.array.template;



import mnkgame.MNKCell;
import player.ArrayBoard;
import player.pnsearch.IPnSearch;
import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodesA.Node_a;



/**
 * @param <A> : array of N
 */
public abstract class IPnSearchA<M extends IMove, V, N extends Node_a<M,V,N>> extends IPnSearch<M,V,N,N[]> {
	

	//#region PLAYER

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
				else current_root.reset(opponent_move, MC.length);
				
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
	
	//#endregion PLAYER
	
	//#region ALGORITHM

		/**
		 * 
		 * @param node
		 */
		protected void developNode(N node) {
			node.expand();
			generateAllChildren(node);
			for(int i = 0; i < node.getChildrenLength(); i++) {
				N child = node.children[i];
				board.markCell(child.i(), child.j());
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
				board.unmarkCell();
			}
			nodes_created += node.getChildrenLength();
			nodes_alive += node.getChildrenLength();
		}

	//#endregion ALGORITHM

	//#region INIT

		@Override
		protected void initAttributes() {
			board = new ArrayBoard(M, N, K);
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			current_root = newNode();

			nodes_created_tot = 0;
			nodes_alive_tot = 0;
			debug = new Debug("debug/debug-" + playerName(), false);
		}

		protected abstract N newNode(int children_max);

	//#endregion INIT

	//#region AUXILIARY

		//returns move to make on this turn
		protected N getBestNode() {
			// if found winning move: return it (i.e. the child move that is winning too)
			if(!current_root.isExpanded() || current_root.getChildrenLength() == 0) return null;
			else {
				N best = current_root.getFirstChild();
				if(current_root.proof == 0) {
					for(int i = 0; i < current_root.getChildrenLength(); i++) {
						N child = current_root.children[i];
						if(child.proof == 0) {
							best = child;
							break;
						}
					}
				}
				// else: return the move with highest (proof-disproof)
				else {
					for(int i = 0; i < current_root.getChildrenLength(); i++) {
						N child = current_root.children[i];
						if(child.proof - child.disproof > best.proof - best.disproof) best = child;
					}
				}
				return best;
			}
		}
		
	//#endregion AUXILIARY

}
