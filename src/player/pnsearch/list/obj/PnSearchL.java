/*
 * MOST BASIC IMPLEMENTATION OF PnSearch, WITHOUT ANY ENHANCEMENT
 */


package player.pnsearch.list.obj;

import java.util.LinkedList;
import mnkgame.MNKCell;
import player.pnsearch.list.template.IPnSearchL;
import player.pnsearch.structures.INodes.Move;
import player.pnsearch.structures.NodesC.Node;



public class PnSearchL extends IPnSearchL<Move, Node, LinkedList<Node>> {


	//#region PLAYER

		public PnSearchL() {
			super();
		}
		
	//#endregion PLAYER

	//#region INIT

		@Override
		protected Move newMove(MNKCell move) {
			return new Move(move);
		}
		@Override
		protected Node newNode() {
			return new Node();
		}

	//#endregion INIT
	
}
