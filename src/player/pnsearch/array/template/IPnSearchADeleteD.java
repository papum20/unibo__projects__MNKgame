package player.pnsearch.array.template;

import player.pnsearch.structures.INodesA.Node_ad;
import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodes.Value;



public abstract class IPnSearchADeleteD<M extends IMove, V, N extends Node_ad<M,V,N>> extends IPnSearchADelete<M,V,N> {

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
			if(my_turn) node.setProofDisproof(node.getChildren_minProof().proof, node.getChildren_sumDisproof());
			else node.setProofDisproof(node.getChildren_sumProof(), node.getChildren_minDisproof().disproof);
			//if proved/disproved: delete all children but the next in order
			if(node.proof == 0 || node.disproof == 0) {
				nodes_alive -= node.getChildrenLength() - 1;
				//remove all children but the one with the same value
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
