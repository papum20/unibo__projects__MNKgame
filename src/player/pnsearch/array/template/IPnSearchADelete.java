package player.pnsearch.array.template;

import mnkgame.MNKCell;
import player.pnsearch.structures.INodes.Move;
import player.pnsearch.structures.INodesA.Node_ad;



public abstract class IPnSearchADelete<N extends Node_ad<N>> extends IPnSearchA<Move, N> {


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
				board.markCell(child.getPosition().i, child.getPosition().j);
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
		protected Move newMove(MNKCell move) {
			return new Move(move);
		}
		@Override
		protected abstract N newNode();
		@Override
		protected abstract N newNode(int children_max);

	//#endregion INIT

}
