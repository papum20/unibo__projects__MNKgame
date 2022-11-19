package player.pnsearch.array.template;

import player.pnsearch.structures.INodesA.Node_ad;
import player.pnsearch.structures.INodes.Value;



public abstract class IPnSearchAUpdate<N extends Node_ad<N>> extends IPnSearchADelete<N> {
	
	//#region PLAYER

		public IPnSearchAUpdate() {
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

		protected void visit(N root) {
			String exception = "";
			long select_time_start = 0;
			long select_time_end = 0;
			try {
				exception = "evaluate root";
				evaluate(root);
				exception = "set proof root";
				setProofAndDisproofNumbers(root, true);
				N currentNode = root;
				while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
				
					debug.node(root);

					exception = "select most proving";
					select_time_start = System.currentTimeMillis() - timer_start;
					N mostProvingNode = selectMostProving(currentNode);
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
			} finally {
				System.out.println("VISIT: " + exception);
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
		protected N updateAncestorsUpto(N node) {
			N previousNode = node;
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

}
