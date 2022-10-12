/*
 * ITERATIVE DEEPENING USING MINIMAX's VISIT;
 * ATTEMPTS A VISIT UP TO DEPTH depth_max AS LONG AS THERE'S ENOUGH TIME,
 * THEN INCREASING depth_max;
 * SAVES THE POSITIONS SO IT DOESN'T HAVE TO ALWAYS START FROM DEPTH 0:
 * MORE DETAILED IMPLEMENTATION:
 * creates a game tree that develops level by level, where to each node is associated a priority-queue
 * containing elements, representing game tree nodes (i.e. board states), that simply save the score for such node;
 * also, each priority-queue has two pointers, one to the parent node of its elements, one to the priority queue where the
 * parent node is located (that is, the priority-queue containing the parent node with its brothers, which share the same parent);
 * there's also a queue containing the game states (boards) for the current depth;
 * execution: in each turn, the algorithm takes (and removes) the first element in the queue as the current board, and creates a priority queue for its children;
 * then tries all possible move from that state (in a future implementation, it could also try to go a few depths ahead instead of just one);
 * for each new state: (if the game is not ended) adds it to the back of the queue (for the inspection of the next level/depth),
 * adds it to the newly generated priority-queue as a score, then recursively updates the scores for the parent nodes and priority queue
 * (it first updates the score for the parent node, performing then an increaseKey on its priority-queue; then if the max/min value for
 * that priority-queue has changed, it goes in recursion to change the score for the parent of the parent, and so on, up to the root;
 * if the root's score is changed, it means that the final output will be changed).
 */


package player.it_deepening;

import java.util.LinkedList;
import mnkgame.MNKCell;
import player.ArrayBoardHeuristic;



public class ItDeepeningMM extends ItDeepeningInterface {

	protected int depth_min;				//first depth to look at


	//#region PLAYER
	
		ItDeepeningMM() {
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
			depth_max = 1;
			LinkedList<ArrayBoardHeuristic> states_at_depth = new LinkedList<ArrayBoardHeuristic>();
			states_at_depth.add(board);
			while(!isTimeEnded() && !states_at_depth.isEmpty()) {
				board = states_at_depth.poll();
				depth_max = board.MarkedCells_length() + 1;		//check at depth=(already made moves)+1
				visitAtDepth(true, depth_max - 1, states_at_depth);
			}
			
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
			return "ItDeepeningMM";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 * @param my_turn = true
		 * @param depth = 0
		 */
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
