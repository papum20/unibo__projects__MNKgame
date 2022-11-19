/*
 * MOST BASIC IMPLEMENTATION OF PnSearch, WITHOUT ANY ENHANCEMENT
 */


package player.pnsearch;

import mnkgame.MNKCell;
import player.pnsearch.structures.Nodes.Move;
import player.pnsearch.structures.Nodes.NodeA;



public class PnSearchA extends IPnSearchA<Move, NodeA> {


	//#region PLAYER

		public PnSearchA() {
			
		}
		
		public String playerName() {
			return "PnSearchA";
		}

	//#endregion PLAYER



	//#region INIT

		@Override
		protected Move newMove(MNKCell move) {
			return new Move(move);
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
