package player.pnsearch;

import player.pnsearch.structures.Nodes.NodeAD;
import player.pnsearch.structures.Nodes.Value;



public class PnSearchAUpdate extends PnSearchADelete {
	
		//#region PLAYER

		public PnSearchAUpdate() {
			super();
		}

		/**
		 * Returns the player name
		 *
		* @return string
		*/
		public String playerName() {
			return "PnSearchAUpdate";
		}

	//#endregion PLAYER

	//#region ALOGRITHM

		protected void visit(NodeAD root) {
			evaluate(root);
			setProofAndDisproofNumbers(root, true);
			NodeAD currentNode = root;
			while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
			
				debug.node(root);

				NodeAD mostProvingNode = selectMostProving(currentNode);
				
				debug.markedCells(0);
				debug.freeCells(0);
				debug.node(mostProvingNode);

				if(!isTimeEnded()) {
					developNode(mostProvingNode);
					currentNode = updateAncestorsUpto(mostProvingNode);
				} else
					resetBoard(mostProvingNode, root);

				debug.node(mostProvingNode);
			}
			// unmark all cells up to root
			resetBoard(currentNode, root);
			// set root value
			if(root.proof == 0) root.value = Value.TRUE;
			else if(root.disproof == 0) root.value = Value.FALSE;			
			else root.value = Value.UNKNOWN;
		}
		/**
		 * 
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 */
		protected NodeAD updateAncestorsUpto(NodeAD node) {
			NodeAD previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = (oldProof != node.proof || oldDisproof != node.disproof);

				debug.nestedNode(node, 0);
				
				previousNode = node;
				node = node.getParent();
				if(node != null && changed) board.unmarkCell();
			}
			return previousNode;
		}
	
	//#endregion ALGORITHM

	//#region INIT

	//#endregion INIT
	
}
