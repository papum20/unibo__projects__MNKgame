package player.pnsearch;

import java.util.Collection;

import mnkgame.MNKGameState;
import player.pnsearch.structures.Nodes.Move;
import player.pnsearch.structures.Nodes.Node_c;
import player.pnsearch.structures.Nodes.Value;

public abstract class IPnSearchL<M extends Move, N extends Node_c<M,N,A>, A extends Collection<N>> extends IPnSearch<M,N,A> {
	

	//#region ALGORITHM

		/**
		 * 
		 * @param node
		 */
		protected void developNode(N node) {
			node.expand();
			generateAllChildren(node);
			for(N child : node.children) {
				board.markCell(child.getPosition().i, child.getPosition().j);
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
				board.unmarkCell();
			}
			nodes_created += node.getChildrenLength();
		}

	//#endregion ALGORITHM

	//#region AUXILIARY

		//returns move to make on this turn
		protected N getBestNode() {
			// if found winning move: return it (i.e. the child move that is winning too)
			if(!current_root.isExpanded() || current_root.children.size() == 0) return null;
			else {
				N best = current_root.children.getFirst();
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
