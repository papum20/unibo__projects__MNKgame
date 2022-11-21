/*
 * MOST BASIC IMPLEMENTATION OF PnSearch, WITHOUT ANY ENHANCEMENT
 */


package player.pnsearch.array.obj;

import mnkgame.MNKCell;
import player.pnsearch.array.template.IPnSearchA;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesA.NodeA;



public class PnSearchA extends IPnSearchA<MovePair, Value, NodeA> {


	//#region PLAYER

		public PnSearchA() {
			
		}
		
		public String playerName() {
			return "PnSearchA";
		}

	//#endregion PLAYER



	//#region INIT

		@Override
		protected MovePair newMove(MNKCell move) {
			return new MovePair(move);
		}
		@Override
		protected NodeA newNode() {
			return new NodeA(board.FreeCells_length());
		}
		@Override
		protected NodeA newNode(int children_max) {
			return new NodeA(children_max);
		}

	//#endregion INIT
	
}
