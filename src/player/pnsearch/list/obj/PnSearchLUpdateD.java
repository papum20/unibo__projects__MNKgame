package player.pnsearch.list.obj;

import mnkgame.MNKCell;
import player.pnsearch.list.template.IPnSearchLUpdateD;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesC.NodeLD;



public class PnSearchLUpdateD extends IPnSearchLUpdateD<MovePair, Value, NodeLD> {
	
		//#region PLAYER

		public PnSearchLUpdateD() {
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
