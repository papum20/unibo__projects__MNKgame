/*
 * MOST BASIC IMPLEMENTATION OF PnSearch, WITHOUT ANY ENHANCEMENT
 */


package player.pnsearch.list.obj;

import java.util.LinkedList;
import mnkgame.MNKCell;
import player.pnsearch.list.template.IPnSearchL;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesC.NodeL;



public class PnSearchL extends IPnSearchL<MovePair, Value, NodeL, LinkedList<NodeL>> {


	//#region PLAYER

		public PnSearchL() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
		@Override
		protected NodeL newNode() {
			return new NodeL();
		}

	//#endregion INIT
	
}
