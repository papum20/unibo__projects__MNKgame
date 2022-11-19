package player.pnsearch.array.template;

import player.pnsearch.structures.INodesA.Node_ads;



public abstract class IPnSearchAStoreD<N extends Node_ads<N>> extends IPnSearchAUpdateD<N> {

	//#region PLAYER

		public IPnSearchAStoreD() {
			super();
		}

		@Override
		public String playerName() {
			return "PnSearchAStoreD";
		}
		
	//#endregion PLAYER

	//#region ALGORITHM

		@Override
		protected void setProofAndDisproofNumbers(N node, boolean my_turn) {
			if(node.isExpanded()) {
				N most_proving;
				if(my_turn) {
					most_proving = node.getChildren_minProof();
					node.setProofDisproof(most_proving.proof, node.getChildren_sumDisproof());
				}
				else {
					most_proving = node.getChildren_minDisproof();
					node.setProofDisproof(node.getChildren_sumProof(), most_proving.disproof);
				}
				node.most_proving = most_proving;
			}
			else
				super.setProofAndDisproofNumbers(node, my_turn);
		}
	
		@Override
		protected N selectMostProving(N node) {
			if(!node.isExpanded()) return node;
			else if(node.most_proving != null) return node.most_proving;
			else {
				N res = super.selectMostProving(node);
				node.most_proving = res;
				return res;
			}
		}

	//#endregion ALGORITHM
	
}
