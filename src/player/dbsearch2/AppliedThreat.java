package player.dbsearch2;

import mnkgame.MNKCellState;
import player.dbsearch2.Operators.Threat;
import player.pnsearch.structures.INodes.MovePair;



public class AppliedThreat {
	public final Threat threat;
	public final int atk;
	public final MNKCellState attacker;

	AppliedThreat(Threat threat, int atk, MNKCellState attacker) {
		this.threat = threat;
		this.atk = atk;
		this.attacker = attacker;
	}
}
