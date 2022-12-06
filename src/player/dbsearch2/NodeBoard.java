package player.dbsearch2;

import java.util.LinkedList;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import player.dbsearch2.DbSearch.Combined;;



public class NodeBoard {
	
	public final DbBoard board;
	public final boolean is_combination;	//true if combination, else false

	protected NodeBoard first_child;
	protected NodeBoard sibling;

	
	
	public NodeBoard(int M, int N, int K, boolean is_combination) {
		this.board = new DbBoard(M, N, K);
		this.is_combination = is_combination;
	}
	public NodeBoard(DbBoard board, boolean is_combination) {
		this.board = new DbBoard(board);
		this.is_combination = is_combination;
	}
	public NodeBoard(DbBoard board, MNKCell[] operator, boolean is_combination) {
		this.board = new DbBoard(board);
		this.is_combination = is_combination;
		this.board.markCells(operator);
	}

	//#region INodeDB

		// copies all non-empty cells, assuming that they're not conflicting
		// with this board
		public void combine(NodeBoard node, Combined combined) {
			LinkedList<MNKCell> cells_to_add = node.getCombinedCells(this, combined);
			board.markCells(cells_to_add, combined);
		}

		/*public boolean equals(NodeBoard node) {
			return board.equals(node.board);
		}*/
		//check the two boards, return true if the same cell is occupied by a player in one board, the other player in the other board
		public boolean inConflict(NodeBoard node) {
			for(int i = 0; i < board.MC_n; i++) {
				MNKCell cell = board.getMarkedCell(i);
				if(cell.state != node.board.cellState(cell.i, cell.j) && node.board.cellState(cell.i, cell.j) != MNKCellState.FREE )
					return true;
			}
			return false;
		}
		
		//assumes the boards are compatible, thus not in conflict
		private LinkedList<MNKCell> getCombinedCells(NodeBoard node, Combined combined) {
			LinkedList<MNKCell> res = new LinkedList<MNKCell>();
			for(int i = 0; i < board.MC_n; i++) {
				MNKCell cell = board.getMarkedCell(i);
				if(cell.state != node.board.cellState(cell.i, cell.j)) {
					res.add(cell);
					combined.board[cell.i][cell.j] = combined.n;
				}
			}
			return res;
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
		//public void setBestChild(NodeBoard best) {first_child = best; }
	
	//#endregion SET
	
}
