package player.dbsearch_old;

import java.util.LinkedList;

import player.boards.ArrayBoardDb;
import player.dbsearch_old.structures.NodeBoard;
import player.dbsearch_old.structures.Operator;

public class DbSearchBoard extends IDbSearchQ<ArrayBoardDb, NodeBoard> {


	
	//#region PLAYER

		DbSearchBoard() { }

		@Override public String playerName() {
			return "DbSearchBoard";
		}
	
	//#endregion PLAYER


	//#region ALGORITHM

		protected boolean findAllCombinationNodes(NodeBoard partner, NodeBoard node) {
			//node.board
			if(board.gameState() == MY_WIN) {
				setBestMove();
				return true;
			}
			else if(node != null) {
				boolean won = false;
				if(!partner.inConflict(node)) {
					if(isDependencyNode(node)) won = addCombination(partner, node);
					//iterate through children and siblings
				}
				if(won) {
					setBestMove();
					return true;
				}
				else if(findAllCombinationNodes(partner, node.getSibling())) return true;
				else return findAllCombinationNodes(partner, node.getFirstChild());
			}
			else return false;
		}
	
	//#endregion ALGORITHM

	
	//#region AUXILIARY
		//#region BOOL
			@Override protected boolean isDependencyNode(NodeBoard node) {
				return !node.is_combination;
			}
			@Override protected boolean addCombination(NodeBoard A, NodeBoard B) {
				//create combination with A's board (copied)
				NodeBoard combination = new NodeBoard(A.board, true);
				combination.combine(B);				//add to it's board marks on B board
				lastCombination.add(combination);
				return combination.board.gameState() == MY_WIN;
			}

		//#endregion BOOL

		//#region CREATE
			@Override protected NodeBoard addChild(NodeBoard parent, AppliedOperator f, boolean is_combination) {
				NodeBoard newChild = new NodeBoard(board, is_combination);
				parent.addChild(newChild);
				return newChild;
			}
		//#endregion CREATE

		//#region GET
			@Override protected LinkedList<AppliedOperator> getApplicableOperators(NodeBoard node, short max, boolean my_attacker) {
				return getApplicableOperators(node.board, max, my_attacker);
			}
		//#endregion GET
				
	//#endregion AUXILIARY


	
	//#region INIT
		@Override protected void initAttributes() {
			board = new ArrayBoardDb(M, N, K);
			super.initAttributes();
		}
		@Override protected void initThreats() {
			max_tier = 2;
			//threats = new Operator[][];
		}
		@Override protected NodeBoard newNode(ArrayBoardDb board, boolean is_combination) {
			return new NodeBoard(board, is_combination);
		}
	//#endregion INIT
		
}
