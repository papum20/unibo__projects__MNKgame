package player.dbsearch2;

import mnkgame.MNKCellState;
import player.pnsearch.structures.INodes.MovePair;



public class AppliedThreat {
	public final MovePair atk;
	public final MovePair[] def;
	public final MNKCellState attacker;
	public final byte type;

	AppliedThreat(MovePair atk, MovePair[] def, MNKCellState attacker, byte type) {
		this.atk = atk;
		this.def = def;
		this.attacker = attacker;
		this.type = type;
	}
}
