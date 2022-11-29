package player.dbsearch.structures;

import mnkgame.MNKCellState;
import player.boards.ArrayBoardDb;



public class NodeBoard implements INodeDB<NodeBoard> {
	
	public final ArrayBoardDb board;
	public final boolean is_combination;	//true if combination, else false

	protected NodeBoard first_child;
	protected NodeBoard sibling;

	
	
	public NodeBoard(int M, int N, int K, boolean is_combination) {
		this.board = new ArrayBoardDb(M, N, K);
		this.is_combination = is_combination;
	}
	public NodeBoard(ArrayBoardDb board, boolean is_combination) {
		this.board = new ArrayBoardDb(board);
		this.is_combination = is_combination;
	}

	//#region INodeDB

		// copies all non-empty cells, assuming that they're not conflicting
		// with this board
		public void combine(NodeBoard node) {
			for(int i = 0; i < board.M; i++) {
				for(int j = 0; j < board.N; j++) {
					if(node.board.cellState(i, j) != MNKCellState.FREE)
						board.B[i][j] = node.board.cellState(i, j);
				}
			}
		}

		@Override public boolean equals(NodeBoard node) {
			return board.equals(node.board);
		}
		//check the two boards, return true if the same cell is occupied by a player in one board, the other player in the other board
		@Override public boolean inConflict(NodeBoard node) {
			for(int i = 0; i < board.M; i++) {
				for(int j = 0; j < board.N; j++) {
					if(board.cellState(i, j) != node.board.cellState(i, j) && board.cellState(i, j) != MNKCellState.FREE && node.board.cellState(i, j) != MNKCellState.FREE )
						return true;
				}
			}
			return false;
		}

	//#endregion INodeDb

	//#region GET

		public NodeBoard getFirstChild() {return first_child; }
		public NodeBoard getSibling() {return sibling; }
	
	//#endregion GET

	//#region SET

		public void addChild(NodeBoard child) {
			if(first_child == null) first_child = child;
			else first_child.addSibling(child);
		}
		public void addSibling(NodeBoard sibling) {
			if(sibling == null) this.sibling = sibling;
			else this.sibling.addSibling(sibling);
		}
		public void setBestChild(NodeBoard best) {first_child = best; }
	
	//#endregion SET
	
}
