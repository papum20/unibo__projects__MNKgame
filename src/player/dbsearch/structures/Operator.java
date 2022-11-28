package player.dbsearch.structures;

import mnkgame.MNKCellState;

public class Operator {

	public static final short ATTACKER = 0;
	public static final short DEFENDER = 1;
	public static final short FREE = 2;

	public final short[] precondition;
	public final short[] delete;
	public final short[] add;

	

	public Operator(final short[] precondition, final short[] delete, final short[] add) {
		this.precondition = precondition;
		this.delete = delete;
		this.add = add;
	}

	public static MNKCellState[] toMNKCellState(short[] V, MNKCellState attacker) {
		MNKCellState[] res = new MNKCellState[V.length];
		for(int i = 0; i < V.length; i++) res[i] = shortToMNKCellState(V[i], attacker);
		return res;
	}
	public static MNKCellState shortToMNKCellState(short n, MNKCellState attacker) {
		if(n == FREE) return MNKCellState.FREE;
		else if(n == ATTACKER) return attacker;
		else if(attacker == MNKCellState.P1) return MNKCellState.P2;
		else return MNKCellState.P1;
	}

}
