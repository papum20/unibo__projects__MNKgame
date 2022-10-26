/*
 * LIKE ItDeepeningMM BUT CAN CALCULATE SEVERAL DEPTHS OF THE GAME TREE AT A TIME,
 * WHICH MEANS, GOES A FEW LEVELS DEEP AT A TIME INSTEAD OF EVALUATING LEVEL BY LEVEL
 */


package player.it_deepening;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.ArrayBoardHeuristic;
import structures.PHElement;
import structures.PHOrder;



public class ItDeepeningMMmulti extends ItDeepeningSmartInterface {

	protected int depth_max;				//number of depths/level of game tree to check at a time



	//#region CLASSES

		protected enum CheckState {
			OK,		//ok
			NOK,	//not ok
			IDK;	//not known
		}
		/**
		 * PRIORITY HEAP OF GAME STATES WITH A BOOLEAN FIELD
		 */
		protected class StatesPHcheck<K, T extends PHElement<T, K>, S extends StatesPHcheck<K,T,S>> extends States_priorityHeap<K, T, S> {
			protected CheckState valid;
			protected int id;

			public StatesPHcheck(PHOrder order, S parent, T parentMove, int id) {
				super(order, parent, parentMove);
				valid = CheckState.IDK;
				this.id = id;
			}
			public StatesPHcheck(Collection<T> V, PHOrder order, S parent, T parentMove, int id) {
				super(V, order, parent, parentMove);
				valid = CheckState.IDK;
				this.id = id;
			}
			public CheckState isValid() {
				return valid;
			}
			public void setValid(CheckState c) {
				valid = c;
			}
			public int getId() {
				return id;
			}
		}
		// INSTANCE of StatesPHcheck
		protected class StatesPHcheck_double extends StatesPHcheck<Double, MoveDouble, StatesPHcheck_double> {
			public StatesPHcheck_double(PHOrder order, StatesPHcheck_double parent, MoveDouble parentMove, int id) {
				super(order, parent, parentMove, id);
			}
			public StatesPHcheck_double(Collection<MoveDouble> V, PHOrder order, StatesPHcheck_double parent, MoveDouble parentMove, int id) {
				super(V, order, parent, parentMove, id);
			}
		}

		
		//#endregion CLASSES

		

		//#region PLAYER
		
			ItDeepeningMMmulti() {
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
		 */
		protected void visitInLine() {
			//
			MoveDouble[] firstLevel = {bestMove};
			States_PH_double firstPH = new States_PH_double(Arrays.asList(firstLevel), PHOrder.GREATER, null, null);
			States_PH_double secondPH = null;
			// list of boards to be evaluated at current depth, grouped by parent game state 
			LinkedList<ChildStates_double> states_at_depth = new LinkedList<ChildStates_double>();
			states_at_depth.add(new ChildStates_double(firstPH, new ScoreBoard_double(board, bestMove)));

			// for each set of "brothers" (nodes in game tree at same depth, sharing same parent node)
			while(!isTimeEnded() && !states_at_depth.isEmpty()) {
				ChildStates_double currentSet = states_at_depth.remove();
				// for each "brother"
				while(!isTimeEnded() && !currentSet.isEmpty()) {
					ScoreBoard_double currentBoard = currentSet.pop();
					//retrieve scores for next depth
					visitAtDepth(currentBoard, currentSet.getPH(), states_at_depth, checkTurn(currentBoard.board), 0, depth_max);
				}
				//update parent priorityHeaps
				currentSet.getPH().updateParent();
				//save ph containing final bestMove
				if(currentSet.getPH().getParent() == firstPH) secondPH = currentSet.getPH();
			}
			//update bestMove
			if(secondPH != null) bestMove = secondPH.getBest();
		}
		/**
		 * @param scoreBoard pair score-board
		 * @param lastPH priorityHeap of last/parent depth
		 * @param boards list of boards to check in visitInLine
		 * @param my_turn = checkTurn(scoreBoard.board)
		 * @param depth = 0
		 * @param depth_max = last depth to check (where heuristic evaluation is called)
		 * @return
		 */
		protected double visitAtDepth(ScoreBoard_double scoreBoard, States_PH_double currentPH, LinkedList<ChildStates_double> boards, boolean my_turn, int depth, int depth_max) {
			//check if someone won or there was a draw
			double state_score = checkGameEnded();
			//else
			if(state_score == STATE_SCORE_OPEN) {
				//else if arrived at depth_max
				if(depth == depth_max) return scoreBoard.board.evaluate();		//return evaluation
				//else make a move
				else {
					//final score obtained from current position, assuming both player make their best moves
					if(my_turn) state_score = getMinScore();		//my turn->worst score for me
					else state_score = getMaxScore();				//your turn->worst score for you

					//create structures:
					// list of moves for priority heap
					LinkedList<MoveDouble> child_moves = new LinkedList<MoveDouble>();
					// priority heap for next depth
					States_PH_double childrenPH = new States_PH_double(my_turn ? PHOrder.GREATER : PHOrder.LESS, currentPH, scoreBoard.lastMove);
					// list of boards
					LinkedList<ScoreBoard_double> child_boards = new LinkedList<ScoreBoard_double>();
					
					//try all moves and update state_score
					FC_iterator it = new FC_iterator(scoreBoard.board);
					while(!it.ended() && !isTimeEnded())
					{
						MoveDouble next = new MoveDouble(scoreBoard.board.getFreeCell(it.i));		//get next move				
						scoreBoard.board.markCell(next.position.i, next.position.j);
						
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
						
						next.score = visitAtDepth(scoreBoard, childrenPH, boards, !my_turn, depth+1, depth_max);	//calculate score for next move
						//update child_boards: add current board with last move, if game is not ended
						if(scoreBoard.board.gameState() == MNKGameState.OPEN) child_boards.addLast(new ScoreBoard_double(new ArrayBoardHeuristic(scoreBoard.board), next));
						scoreBoard.board.unmarkCell();				//then release last move
						//update child_moves: add this move with its score
						child_moves.addLast(next);

						//update score for current state (to return)
						if(my_turn) state_score = max(state_score, next.score);
						else state_score = min(state_score, next.score);
						
						// DEBUG
						//System.out.println((my_turn ? "ME" : "YOU") + " " + Integer.toString(next.position.i) + " " + Integer.toString(next.position.j) + ":" + Integer.toString(alpha)+ " " + Integer.toString(beta) + "/" + Integer.toString(next.score));
						
						it.iterate();
					}
					
					//update structures
					childrenPH.addAll(child_moves);
					//if next depth is max_depth:
					if(depth == depth_max - 1)
						//new list of boards to check later, at next depth
						if(!child_boards.isEmpty()) boards.addLast(new ChildStates_double(childrenPH, child_boards));
					//else if this is the first depth:
					else if(depth == 0)
						//update this prorityHeap
						currentPH.update(scoreBoard.lastMove, state_score);
				}
			}
			return state_score;
		}


	//#endregion ALGORITHM


	//#region AUXILIARY

	@Override
	protected MNKCell getBestMove() {
		return super.getBestMove();
	}
	
	//#endregion AUXILIARY


	//#region INIT

		protected void initAttributes() {
			super.initAttributes();
			depth_max = 3;
		}

		//#endregion INIT


	
}
