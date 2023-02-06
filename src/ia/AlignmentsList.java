package ia;

import java.util.ArrayList;

import mnkgame.MNKCellState;
import ia.BiList.BiNode;



public class AlignmentsList extends ArrayList<BiList_OpPos> {



	public AlignmentsList(int size) {
		super(size);
		for(int i = 0; i < size; i++)
			add(null);
	}
	public AlignmentsList(AlignmentsList copy) {
		super(copy.size());
		int size = copy.size();
		for(int i = 0; i < size; i++) {
			if(copy.get(i) == null) add(null);
			else add(new BiList_OpPos(copy.get(i)));
		}
	}

	public BiNode<OperatorPosition> add(MNKCellState player, int index, OperatorPosition f) {
		BiList_OpPos list = get(index);
		if(list == null) {
			list = new BiList_OpPos();
			set(index, list);
		}
		BiNode<OperatorPosition> res = list.add(player, f);
		return res;
	}
	public void remove(MNKCellState player, int index, BiNode<OperatorPosition> node) {
		get(index).remove(player, node);
	}
	public BiNode<OperatorPosition> getFirst(MNKCellState player, int index) {
		BiNode<OperatorPosition> res = get(index).getFirst(player);
		return res;
	}

}
