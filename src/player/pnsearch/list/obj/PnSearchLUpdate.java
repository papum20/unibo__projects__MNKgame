package player.pnsearch.list.obj;

import mnkgame.MNKCell;
import player.pnsearch.list.template.IPnSearchLUpdate;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesC.NodeLD;



public class PnSearchLUpdate extends IPnSearchLUpdate<MovePair, Value, NodeLD> {
	
	//#region PLAYER

		public PnSearchLUpdate() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
		@Override
		protected NodeLD newNode() {
			return new NodeLD();
		}

	//#endregion INIT
	
}
