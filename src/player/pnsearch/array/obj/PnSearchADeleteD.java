package player.pnsearch.array.obj;

import player.pnsearch.array.template.IPnSearchADeleteD;
import player.pnsearch.structures.NodesA.NodeAD;



public class PnSearchADeleteD extends IPnSearchADeleteD<NodeAD> {
	
	//#region PLAYER

		public PnSearchADeleteD() {
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
