package player.pnsearch;

import player.pnsearch.structures.Nodes.NodeAD;
import player.pnsearch.structures.Nodes.Value;

public class PnSearchAUpdateD extends PnSearchAUpdate {
	
	//#region PLAYER

	public PnSearchAUpdateD() {
		super();
	}

	@Override
	public String playerName() {
		return "PnSearchAUpdate2";
	}
	
	//#endregion PLAYER

	//#region ALGORITHM

		@Override
		protected void developNode(NodeAD node) {
			super.developNode(node);
		}
		/**
		 * 
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 */
		@Override
		protected NodeAD updateAncestorsUpto(NodeAD node) {
			NodeAD previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = (oldProof != node.proof || oldDisproof != node.disproof);

				debug.nestedNode(node, 0);

				// delete children
				if((node.proof == 0 || node.disproof == 0) && node != current_root && node.getParent() != current_root) {
					node.value = (node.proof == 0) ? Value.TRUE : Value.FALSE;
					nodes_alive -= node.getChildrenLength();
					node.children = null;
				}
				// update ancestors
				previousNode = node;
				node = node.getParent();
				if(node != null && changed) board.unmarkCell();
			}
			return previousNode;
		}
	
	//#endregion ALGORITHM

}
