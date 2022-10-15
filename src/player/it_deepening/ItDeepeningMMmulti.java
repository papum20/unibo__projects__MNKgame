/*
 * LIKE ItDeepeningMM BUT CAN CALCULATE SEVERAL DEPTHS OF THE GAME TREE AT A TIME,
 * WHICH MEANS, GOES A FEW LEVELS DEEP AT A TIME INSTEAD OF EVALUATING LEVEL BY LEVEL
 */


package player.it_deepening;

import java.util.Collection;
import java.util.LinkedList;

import mnkgame.MNKCell;
import player.ArrayBoardHeuristic;
import structures.PHOrder;
import structures.PriorityHeap;



public class ItDeepeningMM extends ItDeepeningInterface {

	protected int depth_min;				//first depth to look at





	//#region PLAYER
	
		ItDeepeningMM() {
			super();
		}

		/**
   			* Returns the player name
   			*
			* @return string 
   		*/
		public String playerName() {
			return "ItDeepeningMMmulti";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 * @param my_turn = true
		 * @param depth = 0
		 */
		protected double visitInLine() {
			depth_max = 1;

			LinkedList<ArrayBoardHeuristic> states_at_depth = new LinkedList<ArrayBoardHeuristic>();
			LinkedList<MoveDouble> moves_at_depth = new LinkedList<MoveDouble>();
			states_at_depth.add(board);
			while(!isTimeEnded() && !states_at_depth.isEmpty()) {
				board = states_at_depth.poll();
				depth_max = board.MarkedCells_length() + 1;		//check at depth=(already made moves)+1
				visitAtDepth(true, depth_max - 1, states_at_depth);
			}
		}
		protected double visitAtDepth(boolean my_turn, int depth, LinkedList<ArrayBoardHeuristic> boards) {
			//check if someone won or there was a draw
			double state_score = checkGameEnded();
			//else
			if(state_score == STATE_SCORE_OPEN) {
				//else if arrived at depth_max
				if(depth == depth_max) {
					//save current state
					boards.add(new ArrayBoardHeuristic(board));
					//return evaluation
					return board.evaluate();
				}
				//else make a move
				else {
					//final score obtained from current position, assuming both player make their best moves
					if(my_turn) state_score = getMinScore();		//my turn->worst score for me
					else state_score = getMaxScore();				//your turn->worst score for you
					//try all moves and update state_score
					FC_iterator it = new FC_iterator(board);
					while(!it.ended() && !isTimeEnded())
					{
						MoveDouble next = new MoveDouble(board.getFreeCell(it.i));		//get next move				
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
						
						next.score = visit(!my_turn, depth + 1);	//calculate score for next move
						board.unmarkCell();

						if(my_turn) state_score = max(state_score, next.score);
						else state_score = min(state_score, next.score);
						setBestMove(next, bestMove, depth);							//sets current best move
						
						// DEBUG
						//System.out.println((my_turn ? "ME" : "YOU") + " " + Integer.toString(next.position.i) + " " + Integer.toString(next.position.j) + ":" + Integer.toString(alpha)+ " " + Integer.toString(beta) + "/" + Integer.toString(next.score));
						
						it.iterate();
					}
				}
			}
			return state_score;
		}


	//#endregion ALGORITHM


	//#region INIT

		protected void initAttributes() {
			super.initAttributes();
			depth_min = 1;
		}

		//#endregion INIT


	
}
