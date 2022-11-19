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
			String exception = "";
			long select_time_start = 0;
			long select_time_end = 0;
			try {
				exception = "evaluate root";
				evaluate(root);
				exception = "set proof root";
				setProofAndDisproofNumbers(root, true);
				NodeD currentNode = root;
				while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
					
					debug.node(root);
					
					exception = "select most proving";

					select_time_start = System.currentTimeMillis() - timer_start;
					NodeD mostProvingNode = selectMostProving(currentNode);
					select_time_end = System.currentTimeMillis() - timer_start;
					
					debug.markedCells(0);
					debug.freeCells(0);
					debug.node(mostProvingNode);
					
					exception = "reset board 1";
					if(!isTimeEnded()) {
						exception = "develop";
						developNode(mostProvingNode);
						exception = "ancestors up to";
						currentNode = updateAncestorsUpto(mostProvingNode);
					} else
						resetBoard(mostProvingNode, root);

					debug.node(mostProvingNode);
				}
				// unmark all cells up to root
				exception += ", reset board 2";
				resetBoard(currentNode, root);
				// set root value
				if(root.proof == 0) root.value = Value.TRUE;
				else if(root.disproof == 0) root.value = Value.FALSE;			
				else root.value = Value.UNKNOWN;
			} catch(Exception e) {
				System.out.println("VISIT: " + exception);
			} finally {
				System.out.println("VISIT: last select:");
				System.out.println("\tstart =\t" + select_time_start);
				System.out.println("\tend =\t" + select_time_end);
			}
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
