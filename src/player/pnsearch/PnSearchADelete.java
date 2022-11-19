package player.pnsearch;

import mnkgame.MNKCell;
import player.pnsearch.structures.Nodes.Move;
import player.pnsearch.structures.Nodes.NodeAD;

public class PnSearchADelete extends IPnSearchA<Move, NodeAD> {


	//#region PLAYER

		public PnSearchADelete() {
				
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
		protected void developNode(NodeAD node) {
			node.expand(board.FreeCells_length());
			generateAllChildren(node);
			for(int i = 0; i < node.getChildrenLength(); i++) {
				NodeAD child = node.children[i];
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
		protected NodeAD newNode() {
			return new NodeAD();
		}
		@Override
		protected NodeAD newNode(int children_max) {
			return new NodeAD();
		}

	//#endregion INIT

}
