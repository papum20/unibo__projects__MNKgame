package player.pnsearch.list.obj;

import mnkgame.MNKCell;
import player.pnsearch.list.template.IPnSearchLDelete;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesC.NodeLD;



public class PnSearchLDelete extends IPnSearchLDelete<MovePair, Value, NodeLD> {
	
	//#region PLAYER

		public PnSearchLDelete() {
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
