package player.pnsearch;

import mnkgame.MNKCell;
import player.pnsearch.Nodes.Move;
import player.pnsearch.Nodes.Node;



public class PnSearch extends IPnSearch<Move, Node> {


	//#region PLAYER

		PnSearch() {
			
		}
		
		public String playerName() {
			return "PnSearch";
		}

	//#endregion



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