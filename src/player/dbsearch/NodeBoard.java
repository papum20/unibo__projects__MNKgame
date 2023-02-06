package player.dbsearch;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;



public class NodeBoard {
	
	public final DbBoard board;
	public final boolean is_combination;	//true if combination, else false
	public final byte max_tier;				//max tier to find

	protected NodeBoard first_child;
	protected NodeBoard sibling;


	//#region CLASSES
		public static enum BoardsRelation {
			CONFLICT, USELESS, USEFUL
		}

	//#endregion CLASSES

	
	
	public NodeBoard(int M, int N, int K, boolean is_combination, byte max_tier) {
		this.board = new DbBoard(M, N, K);
		this.is_combination = is_combination;
		this.max_tier = (byte)(max_tier);
		this.first_child = null;
		this.sibling = null;
		//this.flag = false;
	}
	public NodeBoard(DbBoard board, boolean is_combination, byte max_tier) {
		this.board = board;
		this.is_combination = is_combination;
		this.max_tier = (byte)(max_tier);
		this.first_child = null;
		this.sibling = null;
		//this.flag = false;
	}
	/*public NodeBoard(DbBoard board, MNKCell[] operator, boolean is_combination, int max_tier) {
		this.board = new DbBoard(board, true);
		this.is_combination = is_combination;
		this.board.markCells(operator);
		this.max_tier = (byte)(max_tier);
	}*/
	public static NodeBoard copy(DbBoard board, boolean is_combination, byte max_tier, boolean copy_threats) {
		return new NodeBoard(new DbBoard(board, copy_threats), is_combination, max_tier);
	}

	//#region INodeDB

		/*public boolean equals(NodeBoard node) {
			return board.equals(node.board);
		}*/
		//check the two boards, return true if the same cell is occupied by a player in one board, the other player in the other board
		public boolean inConflict(NodeBoard node) {
			for(int i = 0; i < board.MC_n; i++) {
				MNKCell cell = board.getMarkedCell(i);
				if(cell.state != node.board.cellState(cell.i, cell.j) && node.board.cellState(cell.i, cell.j) != MNKCellState.FREE)
					return true;
			}
			return false;
		}
		//like inConflict(), but also
		//calculates wether a combination is useful, i.e., returns true if no attacker's mark would be added with respect to both parent nodes
		public BoardsRelation validCombinationWith(NodeBoard node, MNKCellState attacker) {
			boolean added_own = false, added_other = false;	//wether at least an attacker mark was added from own board, and from other (parameter) board
			//check if own boards adds something
			int i;
			MNKCell cell;
			for(i = 0; i < board.MC_n; i++) {
				cell = board.getMarkedCell(i);
				if(cell.state != node.board.cellState(cell.i, cell.j)) {
					if(node.board.cellState(cell.i, cell.j) != MNKCellState.FREE) return BoardsRelation.CONFLICT;		//conflict: two different marks on same cell
					else if(cell.state == attacker) added_own = true;
				}
			}
			if(!added_own) return BoardsRelation.USELESS;
			else {
				//check if other board adds something
				for(i = 0; i < node.board.MC_n; i++) {
					cell = node.board.getMarkedCell(i);
					if(cell.state != board.cellState(cell.i, cell.j)) {
						if(board.cellState(cell.i, cell.j) != MNKCellState.FREE) return BoardsRelation.CONFLICT;		//conflict: two different marks on same cell
						else if(cell.state == attacker) added_other = true;
					}
				}
				if(added_other) return BoardsRelation.USEFUL;
				else return BoardsRelation.USELESS;
			}
		}
		
		//assumes the boards are compatible, thus not in conflict
		/*private LinkedList<MNKCell> getCombinedCells(NodeBoard node, Combined combined) {
			LinkedList<MNKCell> res = new LinkedList<MNKCell>();
			for(int i = 0; i < board.MC_n; i++) {
				MNKCell cell = board.getMarkedCell(i);
				if(cell.state != node.board.cellState(cell.i, cell.j)) {
					res.add(cell);
					combined.board[cell.i][cell.j] = combined.n;
				}
			}
			return res;
		}*/

	//#endregion INodeDb

	//#region GET

		public NodeBoard getFirstChild() {return first_child; }
		public NodeBoard getSibling() {return sibling; }
		//public Boolean flagged() {return flag; }
	
	//#endregion GET

	//#region SET

		public void addChild(NodeBoard child) {
			if(first_child == null) first_child = child;
			else first_child.addSibling(child);
		}
		public void addSibling(NodeBoard sibling) {
			if(sibling == this) return;
			else if(this.sibling == null) this.sibling = sibling;
			else this.sibling.addSibling(sibling);
		}
		//public void setBestChild(NodeBoard best) {first_child = best; }
		/*
		public void flag() {flag = true; }
		// UN-FLAG RECURSIVELY, ASSUMING THAT THIS WAS FLAGGED IF AND ONLY IF ITS PARENT WAS
		public void unflag_r() {
			if(flag) {
				flag = false;
				if(first_child != null) first_child.unflag_r();
				if(sibling != null) sibling.unflag_r();
			}
		}
		*/
	
	//#endregion SET
	
}
