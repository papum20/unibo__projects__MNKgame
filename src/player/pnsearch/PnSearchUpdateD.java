/*
 * PnSearch Update WITH PnSearch Delete2 ENHANCEMENT
 */

package player.pnsearch;

import player.pnsearch.structures.Nodes.NodeD;
import player.pnsearch.structures.Nodes.Value;



public class PnSearchUpdateD extends PnSearchUpdate {
	
	//#region PLAYER

	public PnSearchUpdateD() {
		super();
	}
	
	@Override
	public String playerName() {
		return "PnSearchUpdate2";
	}
	
	//#endregion PLAYER

	//#region ALGORITHM

		/**
		 * 
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 */
		@Override
		protected NodeD updateAncestorsUpto(NodeD node) {
			NodeD res = super.updateAncestorsUpto(node);
			//if proved/disproved: delete children
			if(node.proof == 0 || node.disproof == 0) {
				node.value = (node.proof == 0) ? Value.TRUE : Value.FALSE;
				node.children = null;
			}
			return res;
		}
	
	//#endregion ALGORITHM

}
