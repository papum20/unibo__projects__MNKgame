package player.dbsearch_old.structures;

import mnkgame.MNKCellState;

public class Operator {

	public static final char ATTACKER = 'A';
	public static final char DEFENDER = 'D';
	public static final char FREE = 'F';

	public final char[] precondition;
	public final char[] add;

	

	public Operator(final char[] precondition, final char[] add) {
		this.precondition = precondition;
		this.add = add;
	}

	public int length() {
		return precondition.length;
	}
	
	public static MNKCellState[] toMNKCellState(char[] V, MNKCellState attacker) {
		MNKCellState[] res = new MNKCellState[V.length];
		for(int i = 0; i < V.length; i++) res[i] = shortToMNKCellState(V[i], attacker);
		return res;
	}
	public static MNKCellState shortToMNKCellState(char n, MNKCellState attacker) {
		if(n == FREE) return MNKCellState.FREE;
		else if(n == ATTACKER) return attacker;
		else if(attacker == MNKCellState.P1) return MNKCellState.P2;
		else return MNKCellState.P1;
	}

}
