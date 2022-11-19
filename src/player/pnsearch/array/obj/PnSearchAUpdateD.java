package player.pnsearch.array.obj;

import player.pnsearch.array.template.IPnSearchAUpdateD;
import player.pnsearch.structures.NodesA.NodeAD;



public class PnSearchAUpdateD extends IPnSearchAUpdateD<NodeAD> {
	
	//#region PLAYER

		public PnSearchAUpdateD() {
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
