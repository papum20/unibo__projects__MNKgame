/*
 * PN SEARCH WHICH STOPS UPDATING PARENTS PROOF NUMBERS WHEN NO CHANGE IS RECORDED,
 * AND USES THAT SAME NODE WHERE THE updateAncestors STOPPED TO RESTART LOOKING FOR THE NEW
 * MOST PROVING NODE, IN THE NEXT ITERATION
 */


package player.pnsearch;

import player.pnsearch.structures.Nodes.NodeD;
import player.pnsearch.structures.Nodes.Value;



public class PnSearchUpdate extends PnSearchDelete {
	
	//#region PLAYER

		public PnSearchUpdate() {
			super();
		}

		/**
		 * Returns the player name
		 *
		* @return string 
		*/
		public String playerName() {
			return "PnSearchUpdate";
		}

	//#endregion PLAYER



	//#region ALOGRITHM

		protected void visit(NodeD root) {
			evaluate(root);
			setProofAndDisproofNumbers(root, true);
			NodeD currentNode = root;
			while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
			
				debug.node(root);

				NodeD mostProvingNode = selectMostProving(currentNode);
				
				debug.markedCells(0);
				debug.freeCells(0);
				debug.node(mostProvingNode);

				//if(!isTimeEnded()) {
					developNode(mostProvingNode);
					currentNode = updateAncestorsUpto(mostProvingNode);

				debug.node(mostProvingNode);
				//}
			}
			// unmark all cells up to root
			while(currentNode != root) {
				currentNode = currentNode.getParent();
				board.unmarkCell();
			}
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
		protected NodeD updateAncestorsUpto(NodeD node) {
			NodeD previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = oldProof != node.proof || oldDisproof != node.disproof;

				debug.nestedNode(node, 0);
				
				if(node.getParent() != null && changed) board.unmarkCell();
				previousNode = node;
				node = node.getParent();
			}
			return previousNode;
		}
	
	//#endregion ALGORITHM

	
	
	//#region INIT

	//#endregion INIT

}
