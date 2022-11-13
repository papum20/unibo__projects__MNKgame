/*
 * DELETES POINTERS TO SOLVED NODES
 */

package player.pnsearch;

import player.pnsearch.structures.Nodes.NodeD;
import player.pnsearch.structures.Nodes.Value;



public class PnSearchDeleteD extends PnSearchDelete {
	
	//#region PLAYER

		public PnSearchDeleteD() {
			super();
		}
	
		/**
			 * Returns the player name
			 *
			* @return string 
		*/
		public String playerName() {
			return "PnSearchDelete2";
		}

	//#endregion PLAYER
	
	//#region ALGORITHM

		/**
		 * 
		 * @param V
		 * @param my_turn
		 */
		@Override
		protected void setProofAndDisproofNumbers(NodeD node, boolean my_turn) {
			if(node.isExpanded()) {
				if(my_turn) node.setProofDisproof(node.getChildren_minProof(), node.getChildren_sumDisproof());
				else node.setProofDisproof(node.getChildren_sumProof(), node.getChildren_minDisproof());
				//if proved/disproved: delete children
				if(node.proof == 0 || node.disproof == 0) {
					node.value = (node.proof == 0) ? Value.TRUE : Value.FALSE;
					node.children = null;
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
