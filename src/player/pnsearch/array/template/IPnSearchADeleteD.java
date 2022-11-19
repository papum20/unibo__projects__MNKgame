package player.pnsearch.array.template;

import player.pnsearch.structures.INodesA.Node_ad;
import player.pnsearch.structures.Nodes.Value;



public abstract class IPnSearchADeleteD<N extends Node_ad<N>> extends IPnSearchADelete<N> {

	//#region PLAYER

	public IPnSearchADeleteD() {
		super();
	}

	/**
		 * Returns the player name
		 *
		* @return string 
	*/
	public String playerName() {
		return "PnSearchADeleteD";
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
				//remove all children but the one with the same value
				node.reduce();
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
