/*
 * PnSearch Update WITH PnSearch Delete2 ENHANCEMENT
 */

package player.pnsearch.list.template;

import player.pnsearch.structures.INodesC.Node_ld;
import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodes.Value;



public abstract class IPnSearchLUpdateD<M extends IMove, V, N extends Node_ld<M,V,N>> extends IPnSearchLUpdate<M,V,N> {	
	
	
	//#region PLAYER

	public IPnSearchLUpdateD() {
		super();
	}

	@Override
	public String playerName() {
		return "PnSearchLUpdateD";
	}
	
	//#endregion PLAYER

	//#region ALGORITHM

		/**
		 * 
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 */
		@Override
		protected N updateAncestorsUpto(N node) {
			N previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = (oldProof != node.proof || oldDisproof != node.disproof);

				debug.nestedNode(node, 0);

				// delete children
				if((node.proof == 0 || node.disproof == 0) && node != current_root && node.getParent() != current_root) {
					nodes_alive -= node.getChildrenLength();
					node.prove( (node.proof == 0) ? Value.TRUE : Value.FALSE);
				}
				// update ancestors
				previousNode = node;
				node = node.getParent();
				if(node != null && changed) board.unmarkCell();
			}
			return previousNode;
		}
	
	//#endregion ALGORITHM


}
