package player.pnsearch.list.obj;

import mnkgame.MNKCell;
import player.pnsearch.list.template.IPnSearchLStoreD;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesC.NodeLDS;



public class PnSearchLStoreD extends IPnSearchLStoreD<MovePair, Value, NodeLDS> {
	
	//#region PLAYER

		public PnSearchLStoreD() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
		@Override
		protected NodeLDS newNode() {
			return new NodeLDS();
		}

	//#endregion INIT
	
}
