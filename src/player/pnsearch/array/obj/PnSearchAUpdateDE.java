package player.pnsearch.array.obj;

import mnkgame.MNKCell;
import player.pnsearch.array.template.IPnSearchAUpdateD;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesAE.NodeAED;



public class PnSearchAUpdateDE extends IPnSearchAUpdateD<MovePair, Value, NodeAED> {
	
	//#region PLAYER

		public PnSearchAUpdateDE() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
		@Override
		protected NodeAED newNode() {
			return new NodeAED();
		}
		@Override
		protected NodeAED newNode(int children_max) {
			return new NodeAED();
		}

	//#endregion INIT	

}
