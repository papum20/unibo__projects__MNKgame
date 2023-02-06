package player.dbsearch;

import mnkgame.MNKCellState;
import player.dbsearch.BiList.BiNode;



public class BiList_OpPos {
	


	private BiList<OperatorPosition> p1;
	private BiList<OperatorPosition> p2;



	public BiList_OpPos() {
		p1 = new BiList<OperatorPosition>();
		p2 = new BiList<OperatorPosition>();
	}
	// WARNING: doesn't create new instances of each OperatorPosition, just uses the same
	public BiList_OpPos(BiList_OpPos copy) {
		p1 = new BiList<OperatorPosition>();
		p2 = new BiList<OperatorPosition>();
		copy(p1, copy.p1.getFirst());
		copy(p2, copy.p2.getFirst());
	}

	private void copy(BiList<OperatorPosition> dest, BiNode<OperatorPosition> from_node) {
		if(from_node != null) {
			copy(dest, from_node.next);
			dest.addFirst(from_node.item);
		}
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
