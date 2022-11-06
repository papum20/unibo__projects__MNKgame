/*
 * PN SEARCH WHICH STOPS UPDATING PARENTS PROOF NUMBERS WHEN NO CHANGE IS RECORDED,
 * AND USES THAT SAME NODE WHERE THE updateAncestors STOPPED TO RESTART LOOKING FOR THE NEW
 * MOST PROVING NODE, IN THE NEXT ITERATION
 */


package player.pnsearch;

import java.io.IOException;



public class PnSearchUpdate extends PnSearch {
	
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

		protected <M extends Move, N extends Node_t<M,N>> void visit(N root) {
			evaluate(root);
			setProofAndDisproofNumbers(root, true);
			N currentNode = root;
			while(root.proof != 0 && root.disproof != 0 && !isTimeEnded()) {
			
				// DEBUG
				/*
				System.out.println(Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + "(r)" + ((currentNode.move == null) ? "root" : currentNode.move.position) + " " + Short.toString(currentNode.proof) + " " + Short.toString(currentNode.disproof) );
				try {
					debugFile.write(Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + "(r)" + ((currentNode.move == null) ? "root" : currentNode.move.position) + " " + Short.toString(currentNode.proof) + " " + Short.toString(currentNode.disproof) + "\n" );
				} catch(IOException e) {
					System.out.println("WTF");
				}
				*/
				N mostProvingNode = selectMostProving(currentNode);
				// DEBUG
				/*
				System.out.println( Integer.toString(board.MarkedCells_length()) + ((mostProvingNode.move == null || isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) );
				try {
					String made = "", free = "";
					for(int i = 0; i < board.MarkedCells_length(); i++)
						made += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
					for(int i = 0; i < board.FreeCells_length(); i++)
						free += Integer.toString(board.getFreeCell(i).i) + Integer.toString(board.getFreeCell(i).j) + " ";
					debugFile.write("MC: " + made + "\n" + "FC: " + free + "\n");
					debugFile.write( Integer.toString(board.MarkedCells_length()) + ((mostProvingNode.move == null || isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) + "\n" );
				} catch(IOException e) {
					System.out.println("WTF");
				}
				*/
				//if(!isTimeEnded()) {
					developNode(mostProvingNode);
					currentNode = updateAncestorsUpto(mostProvingNode);
					// DEBUG
					/*
					System.out.println( Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) );
					try {
						String made = "", free = "";
						for(int i = 0; i < board.MarkedCells_length(); i++)
							made += Integer.toString(board.getMarkedCell(i).i) + Integer.toString(board.getMarkedCell(i).j) + " ";
						for(int i = 0; i < board.FreeCells_length(); i++)
							free += Integer.toString(board.getFreeCell(i).i) + Integer.toString(board.getFreeCell(i).j) + " ";
						debugFile.write("MC: " + made + "\n" + "FC: " + free + "\n");
						debugFile.write( Integer.toString(board.MarkedCells_length()) + ((isMyTurn()) ? "P" : "D") + ((mostProvingNode.move == null) ? "root" : mostProvingNode.move.position) + " " + Short.toString(mostProvingNode.proof) + " " + Short.toString(mostProvingNode.disproof) + "\n" );
						debugFile.write("\n");
					} catch(IOException e) {
						System.out.println("WTF");
					}
					*/
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
		protected <M extends Move, N extends Node_t<M,N>> N updateAncestorsUpto(N node) {
			N previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = oldProof != node.proof || oldDisproof != node.disproof;

				if(node.getParent() != null && changed) board.unmarkCell();

				// DEBUG
				/*
				String tab = "\t";
				for(int i = 0; i < board.MarkedCells_length() - ((node.getParent() == null || !changed) ? 1 : 0); i++) tab += "\t";
				System.out.println( tab + ((node.getParent() == null || !isMyTurn()) ? "P" : "D") + ((node.getParent() == null) ? "root" : node.move.position) + " " + Short.toString(node.proof) + " " + Short.toString(node.disproof) );
				try {
					debugFile.write( tab + ((node.getParent() == null || !isMyTurn()) ? "P" : "D") + ((node.getParent() == null) ? "root" : node.move.position) + " " + Short.toString(node.proof) + " " + Short.toString(node.disproof) + "\n" );
				} catch(IOException e) {
					System.out.println("WTF");
				}
				*/
				previousNode = node;
				node = node.getParent();
			}
			return previousNode;
		}
	
	//#endregion ALGORITHM

	
	
	//#region INIT

		@Override
		protected void initAttributes() {
			super.initAttributes();
			debugName = "debug-pnsearch-update";
		}

	//#endregion INIT

}
