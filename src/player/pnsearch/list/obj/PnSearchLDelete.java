package player.pnsearch.list.obj;

import player.pnsearch.list.template.IPnSearchLDelete;
import player.pnsearch.structures.NodesC.NodeD;



public class PnSearchLDelete extends IPnSearchLDelete<NodeD> {
	
	//#region PLAYER

		public PnSearchLDelete() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected NodeD newNode() {
			return new NodeD();
		}

	//#endregion INIT
	
}
