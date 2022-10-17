/*
 * ALPHABETA ALGORITHM
 */


package player.minimax;

import mnkgame.MNKCell;


public class AlphaBeta extends MiniMax {
	

	//#region PLAYER

		public AlphaBeta() {
			super();
		}
		
		/**
			* Select a position among those listed in the <code>FC</code> array
			*
			* @param FC Free Cells: array of free cells
			* @param MC Marked Cells: array of already marked cells, ordered with respect
			* to the game moves (first move is in the first position, etc)
			*
			* @return an element of <code>FC</code>
		*/
		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

			// DEBUG
			//System.out.println("------------------");
			
			//start conting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(!first || MC.length > 0) {
				MNKCell opponent_move = MC[MC.length - 1];
				board.markCell(opponent_move.i, opponent_move.j);		//mark opponent cell
			}
			//recursive call for each possible move
			bestMove.position = FC[0];
			bestMove.score = getMinScore();
			visit(true, 0, getMinScore(), getMaxScore());
			
			MNKCell res = getBestMove();
			//update my istance of board
			board.markCell(res.i, res.j);								//mark my cell

			// DEBUG
			System.out.println(System.currentTimeMillis() - timer_start);

			return res;
		}
		
		/**
			* Returns the player name
			*
			* @return string 
		*/
		public String playerName() {
			return "AlphaBeta";
		}
		
	//#endregion PLAYER


	//#region ALGORITHM

		/**
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 * @param my_turn = true
		 * @param depth = 0
		 * @param alpha = getMinScore()		//min
		 * @param beta = getMaxScore()		//max
		 */
		protected int visit(boolean my_turn, int depth, int alpha, int beta) {
			//check if someone won or there was a draw
			int state_score = checkGameEnded();
			//else make a move
			if(state_score == STATE_SCORE_OPEN) {
				//final score obtained from current position, assuming both player make their best moves
				if(my_turn) state_score = getMinScore();		//my turn->worst score for me
				else state_score = getMaxScore();				//your turn->worst score for you
				//try all moves and update state_score
				FC_iterator it = new FC_iterator(board);
				while(alpha < beta && !it.ended() && !isTimeEnded())
				{
					MoveInt next = new MoveInt(board.getFreeCell(it.i));		//get next move				
					board.markCell(next.position.i, next.position.j);

					// DEBUG
					/*
					String str = "";
					for(int i = 0; i < board.FreeCells_length(); i++) str += "(" + board.getFreeCell(i).i + "," + board.getFreeCell(i).j + ") ";
					//System.out.println(str);
					System.out.println(str + " " + Integer.toString(next.position.i) + " " + Integer.toString(next.position.j) + "/" + Integer.toString(alpha)+ " " + Integer.toString(beta));
					//Scanner s = new Scanner(System.in);
					//s.next();
					//s.close();
					*/
					
					
					//recursive calls				
					if(my_turn) {
						next.score = visit(!my_turn, depth + 1, alpha, beta);	//calculate score for next move
						state_score = max(state_score, next.score);
						alpha = max(alpha, next.score);
					} else {
						next.score = visit(!my_turn, depth + 1, alpha, beta);	//calculate score for next move
						state_score = min(state_score, next.score);
						beta = min(beta, next.score);
					}
					board.unmarkCell();
					setBestMove(next, bestMove, depth);							//sets current best move
					
					// DEBUG
					//System.out.println((my_turn ? "ME" : "YOU") + " " + Integer.toString(next.position.i) + " " + Integer.toString(next.position.j) + ":" + Integer.toString(alpha)+ " " + Integer.toString(beta) + "/" + Integer.toString(next.score));
					
					it.iterate();
				}
			}
			return state_score;
		}

	//#endregion ALGORITHM


	//#region INIT

	protected void initAttributes() {
		super.initAttributes();
		timer_end = timeout_in_millisecs - (1 * M * N);	// max time - 4ms times max tree depth (M * N = possible moves)
	}

	//#endregion INIT

}
