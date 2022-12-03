package player.dbsearch2;

import mnkgame.MNKCellState;
import player.dbsearch2.BiList.BiNode;

public class BiList_OpPos {
	


	private BiList<OperatorPosition> p1;
	private BiList<OperatorPosition> p2;



	public BiList_OpPos() {
		p1 = new BiList<OperatorPosition>();
		p2 = new BiList<OperatorPosition>();
	}

	public BiNode<OperatorPosition> add(MNKCellState player, OperatorPosition f) {
		BiList<OperatorPosition> list = (player == MNKCellState.P1) ? p1 : p2;
		BiNode<OperatorPosition> res = list.addFirst(f);
		return res;
	}
	public void remove(MNKCellState player, BiNode<OperatorPosition> node) {
		BiList<OperatorPosition> list = (player == MNKCellState.P1) ? p1 : p2;
		list.remove(node);
	}
	public boolean isEmpty(MNKCellState player) {
		return (player == MNKCellState.P1) ? p1.isEmpty() : p2.isEmpty();
	}
	public BiNode<OperatorPosition> getFirst(MNKCellState player) {
		return (player == MNKCellState.P1) ? p1.getFirst() : p2.getFirst();
	}

}
