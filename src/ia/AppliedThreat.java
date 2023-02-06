package ia;

import mnkgame.MNKCellState;
import ia.Operators.Threat;



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
