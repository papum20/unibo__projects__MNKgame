package player;

import mnkgame.MNKPlayer;

public class PlayerInterface<B extends ArrayBoard> implements MNKPlayer {
	public B board;					//saves board for efficiency in checkGameEnded()
}
