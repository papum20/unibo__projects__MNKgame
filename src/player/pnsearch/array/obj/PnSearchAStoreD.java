package player.pnsearch.array.obj;

import player.pnsearch.array.template.IPnSearchAStoreD;
import player.pnsearch.structures.NodesA.NodeADS;



public class PnSearchAStoreD extends IPnSearchAStoreD<NodeADS> {
	
	//#region PLAYER

		public PnSearchAStoreD() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected NodeADS newNode() {
			return new NodeADS();
		}
		@Override
		protected NodeADS newNode(int children_max) {
			return new NodeADS();
		}

	//#endregion INIT	
	
}
