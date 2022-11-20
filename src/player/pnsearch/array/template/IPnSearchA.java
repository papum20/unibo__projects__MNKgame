/*
 * IPnSearch (STILL AN "INTERFACE-CLASS", NOT TRUE CLASS)
 * WHICH USES A java Collection AS Node.children
 */


package player.pnsearch.array.template;



import player.ArrayBoard;
import player.pnsearch.IPnSearch;
import player.pnsearch.structures.INodes.Move;
import player.pnsearch.structures.INodesA.Node_a;



/**
 * @param <A> : array of N
 */
public abstract class IPnSearchA<M extends Move, N extends Node_a<M,N>> extends IPnSearch<M,N,N[]> {
	

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
				board.markCell(child.getPosition().i, child.getPosition().j);
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
