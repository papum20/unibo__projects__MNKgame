package player.pnsearch.list.obj;

import player.pnsearch.list.template.IPnSearchLUpdate;
import player.pnsearch.structures.NodesC.NodeD;



public class PnSearchLUpdate extends IPnSearchLUpdate<NodeD> {
	
	//#region PLAYER

		public PnSearchLUpdate() {
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
