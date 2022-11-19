package player.pnsearch.array.obj;

import player.pnsearch.array.template.IPnSearchAUpdate;
import player.pnsearch.structures.NodesA.NodeAD;



public class PnSearchAUpdate extends IPnSearchAUpdate<NodeAD> {

	//#region PLAYER

		public PnSearchAUpdate() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected NodeAD newNode() {
			return new NodeAD();
		}
		@Override
		protected NodeAD newNode(int children_max) {
			return new NodeAD();
		}

	//#endregion INIT	

	
}
