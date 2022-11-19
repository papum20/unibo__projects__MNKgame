/*
 * STORES MOST PROVING NODE WHEN PERFORMING SetProofAndDisproof;
 * BY DOING SO, SelectMostProvingNode WILL BE LINEAR IN TIME IN THE TREE DEPTH
 */

package player.pnsearch.list.template;

import player.pnsearch.structures.INodesC.Node_ds;



public abstract class IPnSearchLStoreD<N extends Node_ds<N>> extends IPnSearchLUpdateD {
	
	//#region PLAYER

		public IPnSearchLStoreD() {
			super();
		}

		@Override
		public String playerName() {
			return "PnSearchLStoreD";
		}
	
	//#endregion PLAYER

	//#region ALGORITHM

	
	
	//#endregion ALGORITHM

}
