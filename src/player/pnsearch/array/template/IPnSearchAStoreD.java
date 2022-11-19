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
		protected N selectMostProving(N node) {
			
		}
	
	//#endregion ALGORITHM
	
}
