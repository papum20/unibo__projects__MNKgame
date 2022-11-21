/*
* DOESNT IMMEDIATELY CREATE LISTS FOR NODES, JUST WHEN NEEDED;
 * DELETES POINTERS TO SOLVED NODES; (to implement in sublcasses)
 */

package player.pnsearch.list.template;

import java.util.LinkedList;
import player.pnsearch.structures.INodesC.Node_ld;
import player.pnsearch.structures.INodes.IMove;



public abstract class IPnSearchLDelete<M extends IMove, V, N extends Node_ld<M,V,N>> extends IPnSearchL<M,V,N,LinkedList<N>> {
	


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
		protected abstract N newNode();
		
	//#endregion INIT
	

}
