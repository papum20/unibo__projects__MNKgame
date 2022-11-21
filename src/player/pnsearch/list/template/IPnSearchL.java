/*
 * IPnSearch (STILL AN "INTERFACE-CLASS", NOT TRUE CLASS)
 * WHICH USES A java Collection AS Node.children
 */


package player.pnsearch.list.template;

import java.util.Collection;

import player.pnsearch.IPnSearch;
import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodesC.Node_c;



public abstract class IPnSearchL<M extends IMove, V, N extends Node_c<M,V,N,A>, A extends Collection<N>> extends IPnSearch<M,V,N,A> {
	

	//#region PLAYER

		@Override
		public String playerName() {
			return "PnSearchL";
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
			for(N child : node.children) {
				board.markCell(child.i(), child.j());
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
				board.unmarkCell();
			}
			nodes_created += node.getChildrenLength();
			nodes_alive += node.getChildrenLength();
		}

	//#endregion ALGORITHM

	//#region AUXILIARY

		//returns move to make on this turn
		protected N getBestNode() {
			// if found winning move: return it (i.e. the child move that is winning too)
			if(!current_root.isExpanded() || current_root.getChildrenLength() == 0) return null;
			else {
				N best = current_root.getFirstChild();
				if(current_root.proof == 0) {
					for(N child : current_root.children) {
						if(child.proof == 0) {
							best = child;
							break;
						}
					}
				}
				// else: return the move with highest (proof-disproof)
				else {
					for(N child : current_root.children)
						if(child.proof - child.disproof > best.proof - best.disproof) best = child;
				}
				return best;
			}
		}
		
	//#endregion AUXILIARY

}
