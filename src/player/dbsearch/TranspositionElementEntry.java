package player.dbsearch;

import mnkgame.MNKGameState;

public class TranspositionElementEntry {

	public MNKGameState[] state;	//0=attacker, 1=defender
	
	private TranspositionElementEntry(MNKGameState state_a, MNKGameState state_d) {
			this.state = new MNKGameState[]{state_a, state_d};
	}

	public static final TranspositionElementEntry ELEMENT_ENTRIES[] = new TranspositionElementEntry[]{
		new TranspositionElementEntry(null, null),			//0
		new TranspositionElementEntry(null, MNKGameState.OPEN),
		new TranspositionElementEntry(null, MNKGameState.DRAW),
		new TranspositionElementEntry(null, MNKGameState.WINP1),
		new TranspositionElementEntry(null, MNKGameState.WINP2),
		new TranspositionElementEntry(MNKGameState.OPEN, null),		//5
		new TranspositionElementEntry(MNKGameState.OPEN, MNKGameState.OPEN),
		new TranspositionElementEntry(MNKGameState.OPEN, MNKGameState.DRAW),
		new TranspositionElementEntry(MNKGameState.OPEN, MNKGameState.WINP1),
		new TranspositionElementEntry(MNKGameState.OPEN, MNKGameState.WINP2),
		new TranspositionElementEntry(MNKGameState.DRAW, null),		//10
		new TranspositionElementEntry(MNKGameState.DRAW, MNKGameState.OPEN),
		new TranspositionElementEntry(MNKGameState.DRAW, MNKGameState.DRAW),
		new TranspositionElementEntry(MNKGameState.DRAW, MNKGameState.WINP1),
		new TranspositionElementEntry(MNKGameState.DRAW, MNKGameState.WINP2),
		new TranspositionElementEntry(MNKGameState.WINP1, null),		//15
		new TranspositionElementEntry(MNKGameState.WINP1, MNKGameState.OPEN),
		new TranspositionElementEntry(MNKGameState.WINP1, MNKGameState.DRAW),
		new TranspositionElementEntry(MNKGameState.WINP1, MNKGameState.WINP1),
		new TranspositionElementEntry(MNKGameState.WINP1, MNKGameState.WINP2),
		new TranspositionElementEntry(MNKGameState.WINP2, null),		//20
		new TranspositionElementEntry(MNKGameState.WINP2, MNKGameState.OPEN),
		new TranspositionElementEntry(MNKGameState.WINP2, MNKGameState.DRAW),
		new TranspositionElementEntry(MNKGameState.WINP2, MNKGameState.WINP1),
		new TranspositionElementEntry(MNKGameState.WINP2, MNKGameState.WINP2)

	};
	
}
