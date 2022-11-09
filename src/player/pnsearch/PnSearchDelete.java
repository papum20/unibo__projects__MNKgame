/*
 * DELETES POINTERS TO SOLVED NODES;
 * DOESNT IMMEDIATELY CREATE LISTS FOR NODES, JUST WHEN NEEDED
 */

package player.pnsearch;

import java.util.LinkedList;

import mnkgame.MNKCell;
import player.pnsearch.Nodes.Move;
import player.pnsearch.Nodes.NodeD;



public class PnSearchDelete extends IPnSearch<Move, NodeD> {
	


	//#region PLAYER	
	
		public PnSearchDelete() {
			super();
		}
				
		/**
		 * Returns the player name
		 * @return string 
		 */
		public String playerName() {
			return "PnSearchDelete";
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
