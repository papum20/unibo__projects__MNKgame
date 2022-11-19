package player.pnsearch.list.obj;

import player.pnsearch.list.template.IPnSearchLUpdateD;
import player.pnsearch.structures.NodesC.NodeD;



public class PnSearchLUpdateD extends IPnSearchLUpdateD<NodeD> {
	
		//#region PLAYER

		public PnSearchLUpdateD() {
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
