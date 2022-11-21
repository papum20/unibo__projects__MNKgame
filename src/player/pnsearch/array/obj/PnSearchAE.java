/*
 * MOST BASIC IMPLEMENTATION OF PnSearch, WITHOUT ANY ENHANCEMENT
 */


package player.pnsearch.array.obj;

import mnkgame.MNKCell;
import player.pnsearch.array.template.IPnSearchA;
import player.pnsearch.structures.INodes.MovePair;
import player.pnsearch.structures.INodes.Value;
import player.pnsearch.structures.NodesAE.NodeAE;



public class PnSearchAE extends IPnSearchA<MovePair, Value, NodeAE> {


	//#region PLAYER

		public PnSearchAE() {
			
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
		protected NodeAE newNode() {
			return new NodeAE(board.FreeCells_length());
		}
		@Override
		protected NodeAE newNode(int children_max) {
			return new NodeAE(children_max);
		}

	//#endregion INIT
	
}
