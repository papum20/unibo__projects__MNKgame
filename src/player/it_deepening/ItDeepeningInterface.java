/*
 * CONTAINS SOME ATTRIBUTES AND METHODS COMMON TO OTHER IT_DEEPENING,
 * BUT DOESN'T REPRESENT A PLAYING CLASS
 */


package player.it_deepening;

import mnkgame.MNKCell;
import player.ArrayBoardHeuristic;
import player.minimax.AlphaBeta;



public class ItDeepeningInterface extends AlphaBeta {

	ArrayBoardHeuristic board;
	
	//protected int depth_max;				//depth where to stop (updated in execution)
	protected double score_tolerance;		//if abs(b-a)<score_tolerance, it's considered a=b
	protected MoveDouble bestMove;			//best move for current turn


	
	
	//#region CLASSES

	protected class MoveDouble implements Move<MoveDouble, Double> {
		public MNKCell position;	//move target
		public double score;		//score
		public MoveDouble(){};
		public MoveDouble(MNKCell position) {
			this.position = position;
		}
		public MoveDouble(MNKCell position, double score) {
			this.position = position;
			this.score = score;
		}
		public int compareTo(MoveDouble b) {
			double diff = b.score - score;
			if(diff < score_tolerance && diff > -score_tolerance) return 0;
			else if(score > b.score) return 1;
			else return -1;
		}
		public void copy(MoveDouble b) {
			position = b.position;
			score = b.score;
		}
		@Override
		public void increaseKey(Double delta) {
			score += delta;
		}
		@Override
		public void decreaseKey(Double delta) {
			score -= delta;
		}
		@Override
		public Double getKey() {
			return score;
		}
		@Override
		public void setKey(Double new_key) {
			score = new_key;
		}
	}
	protected class ItDeep_score implements Score<ItDeep_score> {
		protected double value;
		private ItDeep_score(double val) {
			this.value = val;
		}
		@Override public int getInt() {
			return (int)Math.round(value);
		}
		@Override public double getDouble() {
			return value;
		}
		@Override public int compareToScore(ItDeep_score b) {
			double diff = value - b.value;
			if(Math.abs(diff) < score_tolerance) return 0;
			else if(diff > 0) return 1;
			else return -1;
		}
	}

	//#endregion CLASSES






	//#region PLAYER	

		public ItDeepeningInterface() {
			super();
		}

		/**
   			* Returns the player name
   			*
			* @return string 
   		*/
		   public String playerName() {
			return "ItDeepeningInterface";
		}
	
	//#endregion PLAYER


	//#region INIT

		protected void initAttributes() {
			timer_end = timeout_in_millisecs - (4 * M * N);			// max time - 4ms times max tree depth (M * N = possible moves)
			board = new ArrayBoardHeuristic(M, N, K);
			bestMove = new MoveDouble();

			score_tolerance = .1;
		}
		
	//#endregion INIT

}
