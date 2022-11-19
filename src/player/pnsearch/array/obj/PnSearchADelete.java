package player.pnsearch.array.obj;

import player.pnsearch.array.template.IPnSearchADelete;
import player.pnsearch.structures.NodesA.NodeAD;



public class PnSearchADelete extends IPnSearchADelete<NodeAD> {
	
	//#region PLAYER

		public PnSearchADelete() {
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
