package player.pnsearch.list.obj;

import player.pnsearch.list.template.IPnSearchLDeleteD;
import player.pnsearch.structures.NodesC.NodeD;



public class PnSearchLDeleteD extends IPnSearchLDeleteD<NodeD> {
	
	//#region PLAYER

		public PnSearchLDeleteD() {
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
