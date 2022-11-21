package player.pnsearch.array.obj;

import mnkgame.MNKCell;
import player.pnsearch.array.template.IPnSearchADelete;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesAE.NodeAED;



public class PnSearchADeleteE extends IPnSearchADelete<MovePair, Value, NodeAED> {
	
	//#region PLAYER

		public PnSearchADeleteE() {
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
