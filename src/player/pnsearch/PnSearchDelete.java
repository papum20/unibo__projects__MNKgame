/*
* DOESNT IMMEDIATELY CREATE LISTS FOR NODES, JUST WHEN NEEDED;
 * DELETES POINTERS TO SOLVED NODES; (to implement in sublcasses)
 */

package player.pnsearch;

import java.util.LinkedList;
import mnkgame.MNKCell;
import player.pnsearch.structures.Nodes.Move;
import player.pnsearch.structures.Nodes.NodeD;



public class PnSearchDelete extends IPnSearchL<Move, NodeD, LinkedList<NodeD>> {
	


	//#region PLAYER	
	
		public PnSearchDelete() {
			super();
		}
				
		/**
		 * Returns the player name
		 * @return string 
		 */
		public String playerName() {
			return "PnSearchADelete";
		}

		
		//#endregion PLAYER



	//#region ALGORITHM


	//#endregion ALGORITHM



	//#region INIT

		@Override
		protected Move newMove(MNKCell move) {
			return new Move(move);
		}

		@Override
		protected NodeD newNode() {
			return new NodeD();
		}
		
	//#endregion INIT
	

}
