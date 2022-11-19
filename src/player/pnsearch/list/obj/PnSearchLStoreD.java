package player.pnsearch.list.obj;

import player.pnsearch.list.template.IPnSearchLStoreD;
import player.pnsearch.structures.NodesC.NodeDS;



public class PnSearchLStoreD extends IPnSearchLStoreD<NodeDS> {
	
	//#region PLAYER

		public PnSearchLStoreD() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected NodeDS newNode() {
			return new NodeDS();
		}

	//#endregion INIT
	
}
