package player.pnsearch.array.obj;

import mnkgame.MNKCell;
import player.pnsearch.array.template.IPnSearchADeleteD;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesA.NodeAD;



public class PnSearchADeleteD extends IPnSearchADeleteD<MovePair, Value, NodeAD> {
	
	//#region PLAYER

		public PnSearchADeleteD() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT
	
		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
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
