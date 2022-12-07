package player.dbsearch2;

import mnkgame.MNKCellState;
import player.dbsearch2.BiList.BiNode;

public class BiList_NodeOpPos {
	


	private BiList<BiNode<OperatorPosition>> p1;
	private BiList<BiNode<OperatorPosition>> p2;



	public BiList_NodeOpPos() {
		p1 = new BiList<BiNode<OperatorPosition>>();
		p2 = new BiList<BiNode<OperatorPosition>>();
	}
	

	public BiNode<BiNode<OperatorPosition>> add(MNKCellState player, BiNode<OperatorPosition> node) {
		BiList<BiNode<OperatorPosition>> list = (player == MNKCellState.P1) ? p1 : p2;
		BiNode<BiNode<OperatorPosition>> res = list.addFirst(node);
		return res;
	}
	public void remove(MNKCellState player, BiNode<BiNode<OperatorPosition>> node) {
		BiList<BiNode<OperatorPosition>> list = (player == MNKCellState.P1) ? p1 : p2;
		list.remove(node);
	}
	public boolean isEmpty(MNKCellState player) {
		return (player == MNKCellState.P1) ? p1.isEmpty() : p2.isEmpty();
	}
	public BiNode<BiNode<OperatorPosition>> getFirst(MNKCellState player) {
		return (player == MNKCellState.P1) ? p1.getFirst() : p2.getFirst();
	}

}
