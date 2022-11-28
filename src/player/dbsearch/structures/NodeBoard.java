package player.dbsearch.structures;

import player.ArrayBoardDb;



public class NodeBoard implements INodeDB<NodeBoard> {
	
	protected ArrayBoardDb board;

	
	
	public NodeBoard(int M, int N, int K) {
		this.board = new ArrayBoardDb(M, N, K);
	}
	public NodeBoard(ArrayBoardDb board) {
		this.board = new ArrayBoardDb(board);
	}

	//#region INodeDB

		public boolean equals(NodeBoard node) {
			return board.equals(node.board);
		}

	//#endregion INodeDb
	
}
