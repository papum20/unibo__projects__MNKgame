/*
 * LIKE ItDeepeningMMmultiSaves, BUT FIXES ITS PROBLEM IN SAVING;
 * DOESN'T SAVE BOARDS BUT JUST MOVES MADE	
 */


package player.it_deepening;

import java.util.Collection;
import java.util.LinkedList;
import mnkgame.MNKCell;
import structures.PHElement;
import structures.PHOrder;



public class ItDeepeningMMmultiSavesMoves extends ItDeepeningMMmulti {

	// priorityHeap containing the last made move
	protected StatesPHcheck2_double firstPH;
	// saved boards, that where being checked last turn
	protected LinkedList<StatesPHcheck2_double> states_at_depth;

	protected boolean foundRoot;		//for visitInLine()



	//#region CLASSES
	
		protected class StatesPHcheck2<K, T extends PHElement<T, K>, S extends StatesPHcheck2<K,T,S>> extends States_priorityHeap<K, T, S> {
			// PARENTS' ATTRIBUTES:
			//final PriorityHeap<K, T> PH;
			//S parent;
			//T parentMove;
			protected CheckState valid;
			protected boolean completed;	//if analyzed all child moves

			public StatesPHcheck2(PHOrder order, S parent, T parentMove) {
				super(order, parent, parentMove);
				valid = CheckState.IDK;
				completed = false;
			}
			public StatesPHcheck2(Collection<T> V, PHOrder order, S parent, T parentMove) {
				super(V, order, parent, parentMove);
				valid = CheckState.IDK;
				completed = false;
			}
			public CheckState isValid() {
				return valid;
			}
			public void setValid(CheckState c) {
				valid = c;
			}
			public boolean isCompleted() {
				return completed;
			}
			public void setCompleted(boolean b) {
				completed = b;
			}
			public T findChildMove(T move) {
				return PH.get(PH.find2(move));
			}
		}
		/**
		 * subclass of MoveDouble for StatesPHcheck2_double
		 * @param <S> : self (this class)
		 * @param <P> : PH (states_priority_heap) type
		 */
		protected class MoveDoubleChild<S extends MoveDoubleChild<S,P>, P extends States_priorityHeap<Double, S, P>> extends IMoveDouble<S> {
			protected P childPH;
			public MoveDoubleChild() { };
			public MoveDoubleChild(MNKCell position) {
				super(position);
			}
			public MoveDoubleChild(MNKCell position, double score) {
				super(position, score);
			}
			public P getPH() {
				return childPH;
			}
		}
		// INSTANCE of MoveDoubleChild
		protected class MoveDoubleChild_t extends MoveDoubleChild<MoveDoubleChild_t, StatesPHcheck2_double> {
			public MoveDoubleChild_t() { };
			public MoveDoubleChild_t(MNKCell position) {
				super(position);
			}
			public MoveDoubleChild_t(MNKCell position, double score) {
				super(position, score);
			}
		}
		// INSTANCE of StatesPHcheck2
		protected class StatesPHcheck2_double extends StatesPHcheck2<Double, MoveDoubleChild_t, StatesPHcheck2_double> {
			public StatesPHcheck2_double(PHOrder order, StatesPHcheck2_double parent, MoveDoubleChild_t parentMove) {
				super(order, parent, parentMove);
			}
			public StatesPHcheck2_double(Collection<MoveDoubleChild_t> V, PHOrder order, StatesPHcheck2_double parent, MoveDoubleChild_t parentMove) {
				super(V, order, parent, parentMove);
			}
		}

	//#endregion CLASSES


	//#region PLAYER
	
		ItDeepeningMMmultiSavesMoves() {
			super();
		}

		/**
   			* Returns the player name
   			*
			* @return string 
   		*/
		public String playerName() {
			return "ItDeepeningMMmultiSavesMoves";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 */
		protected void visitInLine() {

			// UPDATE firstPH
			init_firstPH();

			//

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
		/*
		protected double visitAtDepth(ScoreBoard_double scoreBoard, StatesPHcheck_double currentPH, LinkedList<ChildStates_check_d> boards, boolean my_turn, int depth, int depth_max) {
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
					StatesPHcheck_double childrenPH = new StatesPHcheck_double(my_turn ? PHOrder.GREATER : PHOrder.LESS, currentPH, scoreBoard.lastMove, currentId());
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
						*
						
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

					// IF TIME ENDED, BUT DIDN'T CHECK ALL CHILD MOVES: ADD THEM TO childrenPH AND child_boards SO YOU CAN CHECK THEM NEXT TURN, IN CASE
					
					//update structures
					childrenPH.addAll(child_moves);
					//if next depth is max_depth:
					if(depth == depth_max - 1)
						//new list of boards to check later, at next depth
						if(!child_boards.isEmpty()) boards.addLast(new ChildStates_check_d(childrenPH, child_boards));
					//else if this is the first depth:
					else if(depth == 0)
						//update this prorityHeap
						currentPH.update(scoreBoard.lastMove, state_score);
				}
			}
			return state_score;
		}
		*/


	//#endregion ALGORITHM


	//#region AUXILIARY

		/**
		 * checks if a priority heap of game states derives from the current board (attribute), and, in case,
		 * recursively sets encountered PHs to not valid, so that the function called with another PH will end
		 * sooner.
		 * @param PH != null
		 * @param moveCount : moves made up to this game state
		 * @return true if PH derives from the game state saved in board (attribute)
		 */
		protected <P extends StatesPHcheck_double> boolean checkValidPH(P PH, int moveCount) {
			if	(PH == null
				|| PH.getParent() == null																		//arrived at the root of tree of PHs
				|| PH.isValid() == CheckState.NOK																//already checked not ok
				|| PH.isValid() != CheckState.OK																//not checked ok, i.e. it's IDK (not known)
				|| (moveCount > board.MarkedCells_length() + 1 && !checkValidPH(PH.getParent(), moveCount - 1))	//ph is deeper than the last board's move and the recursive call returned false
				|| moveCount == board.MarkedCells_length()														//PH either contains the last move (so it's more efficient to rebuild PH) or not (so it's not valid)
				|| (moveCount - 1 == board.MarkedCells_length() && !compareMC(PH, moveCount))					//it's the depth next to the board's, but the previous moves are different
			) {
				PH.setValid(CheckState.NOK);
				return false;
			} else {							//parent was already checked ok or it's just been confirmed after recursive calls
				PH.setValid(CheckState.OK);
				return true;
			}
		}

		protected boolean compareMoves_fast(MNKCell a, MNKCell b) {
			return a.i == b.i && a.j == b.j;
		}
		/**
		 * PRECONDITION: moveCount == board.MarkedCells_length()
		 * checks from the last made move, so it can stop immediately if it founds two different moves
		 * @param PH :
		 * @param moveCount : moves made up to this game state (in PH)
		 * @return true if the sequences of made moves are the same
		 */
		protected <P extends States_priorityHeap<Double,MoveDouble,P>> boolean compareMC(P PH, int moveCount) {
			if(moveCount <= 1) return true;
			else if(!compareMoves_fast(PH.getParentMove().position, board.getMarkedCell(moveCount - 2))) return false;
			else return compareMC(PH.getParent(), moveCount - 1);
		}

		protected int currentId() {
			return board.MarkedCells_length();
		}

	//#endregion AUXILIARY


	//#region INIT

		/*protected void initAttributes() {
			super.initAttributes();
			//firstPH
			firstPH = null;
			//states_at_depth
			states_at_depth = new LinkedList<StatesPHcheck_double>();
		}*/

		protected void init_firstPH() {
			MoveDoubleChild_t parentMove;
			if(firstPH == null) {
				if(board.MarkedCells_length() > 0) parentMove = new MoveDoubleChild_t(board.getMarkedCell(board.MarkedCells_length() - 1), getMinScore()); 	//if not first turn: use last move
				else parentMove = null;																	//else: empty move
				firstPH = new StatesPHcheck2_double(null, PHOrder.GREATER, null, parentMove);
			}
			else {
				parentMove = firstPH.findChildMove(new MoveDoubleChild_t(board.getMarkedCell(board.MarkedCells_length() - 2), 0));
				parentMove = parentMove.getPH().findChildMove(new MoveDoubleChild_t(board.getMarkedCell(board.MarkedCells_length() - 2), 0));
				firstPH = parentMove.getPH();
			}
		}

	//#endregion INIT


	
}
