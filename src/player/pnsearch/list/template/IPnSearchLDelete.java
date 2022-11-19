/*
* DOESNT IMMEDIATELY CREATE LISTS FOR NODES, JUST WHEN NEEDED;
 * DELETES POINTERS TO SOLVED NODES; (to implement in sublcasses)
 */

package player.pnsearch.list.template;

import java.util.LinkedList;
import mnkgame.MNKCell;
import player.pnsearch.structures.INodesC.Node_d;
import player.pnsearch.structures.Nodes.Move;



public abstract class IPnSearchLDelete<N extends Node_d<N>> extends IPnSearchL<Move, N, LinkedList<N>> {
	


	//#region PLAYER	
	
		public IPnSearchLDelete() {
			super();
		}
				
		/**
		 * Returns the player name
		 * @return string 
		 */
		public String playerName() {
			return "PnSearchLDelete";
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
		protected abstract N newNode();
		
	//#endregion INIT
	

}
