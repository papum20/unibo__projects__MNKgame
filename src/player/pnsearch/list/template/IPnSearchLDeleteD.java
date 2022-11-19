/*
 * DELETES POINTERS TO SOLVED NODES
 */

package player.pnsearch.list.template;

import player.pnsearch.structures.INodesC.Node_d;
import player.pnsearch.structures.Nodes.Value;



public abstract class IPnSearchLDeleteD<N extends Node_d<N>> extends IPnSearchLDelete<N> {

	
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
				if(my_turn) node.setProofDisproof(node.getChildren_minProof(), node.getChildren_sumDisproof());
				else node.setProofDisproof(node.getChildren_sumProof(), node.getChildren_minDisproof());
				//if proved/disproved: delete all children but the next in order
				if(node.proof == 0 || node.disproof == 0) {
					nodes_alive -= node.getChildrenLength() - 1;
					N next = null;
					for(N child : node.children) {
						if(child.proof == node.proof || child.disproof == node.disproof) {
							next = child;
							break;
						}
					}
					node.value = (node.proof == 0) ? Value.TRUE : Value.FALSE;
					node.children.clear();
					node.children.add(next);
				}
			}
			else if(node.value != Value.UNKNOWN) {
				if(node.value == Value.TRUE) node.setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
				else node.setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			}
			else node.setProofDisproof((short)1, (short)1);
		}

	//#endregion ALGORITHM
	
}