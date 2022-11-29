/*
 * ARRAY BOARD WHICH CAN EVALUATE A POSITION, GIVING IT A SCORE
 * AS A REAL NUMBER IN RANGE -1 AND 1 (WHERE THE BOUNDS ARE CERTAIN VALUES,
 * i.e. A WIN FOR A PLAYER)
 */


package player.boards;



public class ArrayBoardHeuristic extends ArrayBoard {
	
	protected final double SCORE_MIN;
	protected final double SCORE_MAX;

	public ArrayBoardHeuristic(int M, int N, int K) {
		super(M, N, K);
		SCORE_MIN = -1;
		SCORE_MAX = 1;
	}
	public ArrayBoardHeuristic(ArrayBoardHeuristic AB) {
		super(AB);
		SCORE_MIN = -1;
		SCORE_MAX = 1;
	}


	/**
	 * evaluates current position, assuming it's not an ended state
	 * @return current position's evaluation
	 */
	public double evaluate() {
		return 0;
	}
	public double evaluate(boolean first) {
		int my_player = 0;
		if(!first) my_player = 1;
		if(currentPlayer() == my_player) return SCORE_MAX;	//if last move was from opponent
		else return SCORE_MIN;
	}
	
}
