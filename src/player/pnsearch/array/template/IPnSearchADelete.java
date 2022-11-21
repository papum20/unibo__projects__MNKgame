package player.pnsearch.array.template;

import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodesA.Node_ad;



public abstract class IPnSearchADelete<M extends IMove, V, N extends Node_ad<M,V,N>> extends IPnSearchA<M,V,N> {


	//#region PLAYER

		public IPnSearchADelete() {
				
		}
		
		public String playerName() {
			return "PnSearchADelete";
		}

	//#endregion PLAYER

	//#region ALGORITHM

		/**
		 * 
		 * @param node
		 */
		protected void developNode(N node) {
			node.expand(board.FreeCells_length());
			generateAllChildren(node);
			for(int i = 0; i < node.getChildrenLength(); i++) {
				N child = node.children[i];
				board.markCell(child.i(), child.j());
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
				board.unmarkCell();
			}
			nodes_created += node.getChildrenLength();
			nodes_alive += node.getChildrenLength();
		}

	//#endregion ALGORITHM

	//#region INIT
	
		@Override
		protected abstract N newNode();
		@Override
		protected abstract N newNode(int children_max);

	//#endregion INIT

}
