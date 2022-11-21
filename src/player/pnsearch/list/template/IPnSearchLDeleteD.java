/*
 * DELETES POINTERS TO SOLVED NODES
 */

package player.pnsearch.list.template;

import player.pnsearch.structures.INodesC.Node_ld;
import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodes.Value;



public abstract class IPnSearchLDeleteD<M extends IMove, V, N extends Node_ld<M,V,N>> extends IPnSearchLDelete<M,V,N> {

	
	//#region PLAYER

		public IPnSearchLDeleteD() {
			super();
		}
	
		/**
			 * Returns the player name
			 *
			* @return string 
		*/
		public String playerName() {
			return "PnSearchLDeleteD";
		}

	//#endregion PLAYER
	
	//#region ALGORITHM

		/**
		 * 
		 * @param V
		 * @param my_turn
		 */
		@Override
		protected void setProofAndDisproofNumbers(N node, boolean my_turn) {
			if(node.isExpanded()) {
				if(my_turn) node.setProofDisproof(node.getChildren_minProof().proof, node.getChildren_sumDisproof());
				else node.setProofDisproof(node.getChildren_sumProof(), node.getChildren_minDisproof().disproof);
				//if proved/disproved: delete all children but the next in order
				if(node.proof == 0 || node.disproof == 0) {
					nodes_alive -= node.getChildrenLength() - 1;
					node.reduce();
				}
			}
			else if(node.getValue() != Value.UNKNOWN) {
				if(node.getValue() == Value.TRUE) node.setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
				else node.setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			}
			else initProofAndDisproofNumbers(node);;
		}

	//#endregion ALGORITHM
	
}
