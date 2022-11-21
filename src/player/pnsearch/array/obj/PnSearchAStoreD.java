package player.pnsearch.array.obj;

import mnkgame.MNKCell;
import player.pnsearch.array.template.IPnSearchAStoreD;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesA.NodeADS;



public class PnSearchAStoreD extends IPnSearchAStoreD<MovePair, Value, NodeADS> {
	
	//#region PLAYER

		public PnSearchAStoreD() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
		@Override
		protected NodeADS newNode() {
			return new NodeADS();
		}
		@Override
		protected NodeADS newNode(int children_max) {
			return new NodeADS();
		}

	//#endregion INIT	
	
}
