/*
 * LIKE ItDeepeningMM BUT CAN CALCULATE SEVERAL DEPTHS OF THE GAME TREE AT A TIME,
 * WHICH MEANS, GOES A FEW LEVELS DEEP AT A TIME INSTEAD OF EVALUATING LEVEL BY LEVEL
 */


package player.it_deepening;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.plaf.metal.MetalBorders.ScrollPaneBorder;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import player.ArrayBoardHeuristic;
import structures.PHElement;
import structures.PHOrder;
import structures.PriorityHeap;



public class ItDeepeningMMmultiSaves extends ItDeepeningMMmulti {

	// priorityHeap containing the last made move
	protected StatesPHcheck_double firstPH;
	// saved boards, that where being checked last turn
	protected LinkedList<ChildStates_check_d> states_at_depth;



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
		// INSTANCE OF ChildStates CLASS
		protected class ChildStates_check_d extends ChildStates<ArrayBoardHeuristic, Double, MoveDouble, ScoreBoard_double, StatesPHcheck_double> {
			public ChildStates_check_d(StatesPHcheck_double PH) {
				super(PH);
			}
			public ChildStates_check_d(StatesPHcheck_double PH, ScoreBoard_double scoreBoard) {
				super(PH, scoreBoard);
			}
			public ChildStates_check_d(StatesPHcheck_double PH, LinkedList<ScoreBoard_double> scoreBoards) {
				super(PH, scoreBoards);
			}
		}
	
	//#endregion CLASSES


	//#region PLAYER
	
		ItDeepeningMMmultiSaves() {
			super();
		}

		/**
   			* Returns the player name
   			*
			* @return string 
   		*/
		public String playerName() {
			return "ItDeepeningMMmultiSaves";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 */
		protected void visitInLine() {

			// IF lastPH = NULL : INIT IT (IF NOT YOUR FIRST TURN: INIT WITH LAST MOVE; ELSE IF FIRST=FALSE: WITH FIRST ENEMY MOVE; ELSE IF FIRST=TRUE: SOMETHING)
			if(firstPH == null)
				init_firstPH();
			// ELSE, IT MEANS lastPH HAS ITS ROOT ON YOUR LAST MOVE, WHICH MEANS YOU HAVE TO MAKE THE ROOT ENEMY'S LAST MOVE, KEEPING ALL THE PHs CREATED LAST TURN
			// CHECK ALL SAVED BOARDS (SAVED LAST TURN, TO CHECK NOW), WITH checkValidPH() :
				// IF RETURN FALSE, DISCARD
				// IF, AT THE END, LAST ENEMY MOVE WAS NOT FOUND (i.e. NOONE RETURNED TRUE), CREATE lastPH ONLY CONTAINING LAST ENEMY MOVE, AND MAYBE UPDATING ITS PARENT TO NULL
				// WHEN FIND LAST ENEMY MOVE, SAVE IT SO (IMMEDIATELY OR AFTER ALL THE CHECKS) YOU CAN SET lastPH TO IT, REMOVING ALL ELEMENTS EXCEPT THAT
				// DURING THE CHECK, SAVE ALL FOUND MOVES WHICH CAME JUST AFTER LAST ENEMY MOVE, IN AN ARRAY/MATRIX (CONSTANT COST), AND COUNT THESE MOVES
				// IF THEY'RE NOT EQUAL TO freeCells_length, IT MEANS SOME ARE MISSING, SO START CALLS ON THESE AND IN CASE ADD THE RESULTS TO THE BOARDS TO CHECK
			// (RECAP: lastPH IS SET, ALL HIS CHILDREN HAVE BEEN CHECKED, THUS THEY OR THEIR DESCENDANTS ARE IN A PH/BOARD TO CHECK)
			ListIterator<ChildStates_check_d> it = states_at_depth.listIterator();
			while(it.hasNext()) {
				ChildStates_check_d list = it.next();
				StatesPHcheck_double parentPH = list.getPH().getParent();
				// if parentPH = null, it means it's still the root, thus no move was calculated
				if(parentPH == null) break;
				
				boolean checkPH = checkValidPH(parentPH, list.getBoards().getFirst().board.MarkedCells_length());
				if(!checkPH) {
					
				}
			}

			// NOW YOU CAN RESTART CHECKING AS LONG AS YOU HAVE TIME WITH visitAtDepth()

			// array containing true if a cell is to check (because it's free)
			// later it will be updated, removing cells already checked in the previous turn(s)
			boolean[][] toCheck = new boolean[M][N];
			for(int y = 0; y < M; y++) {
				for(int x = 0; x < N; x++) {
					if(board.cellState(y, x) == MNKCellState.FREE) toCheck[y][x] = true;
					else toCheck[y][x] = false;
				}
			}
			// PH for second level, containing the next best move (to return)
			StatesPHcheck_double secondPH = null;

			// CHECK BOARDS SAVED IN PREVIOUS TURN(S)
			if(!states_at_depth.isEmpty()) {
				ChildStates_check_d currentSet;
				boolean foundRoot = false;
				do {
					currentSet = states_at_depth.remove();
					ScoreBoard_double currentState = currentSet.getBoards().getFirst();
					boolean checkPH = checkValidPH(currentSet.getPH().getParent(), currentState.board.MarkedCells_length());
					// only use the saved board if it derives from the last made move
					if(checkPH) {
						foundRoot = true;
						while(!isTimeEnded() && !currentSet.isEmpty()) {
							ScoreBoard_double currentBoard = currentSet.pop();
							//retrieve scores for next depth
							visitAtDepth(currentBoard, currentSet.getPH(), states_at_depth, checkTurn(currentBoard.board), 0, depth_max);
						}
						//update parent priorityHeaps
						currentSet.getPH().updateParent();
						//save ph containing final bestMove
						if(currentSet.getPH().getParent() == firstPH) {
							secondPH = currentSet.getPH();							//save secondPH (even if already found before)
							MNKCell childMove = currentState.lastMove.position;
							toCheck[childMove.i][childMove.j] = false;				//mark move (child of last made move) as not to check
						}
					}
				} while(currentSet.getPH().getId() != currentId() && !isTimeEnded() && !states_at_depth.isEmpty());
				// if didn't find last made move as parent: create new first ph
				if(!foundRoot) init_firstPH();
			}

			// ADD MISSING MOVES TO CHECK
			// create structures:
			LinkedList<MoveDouble> child_moves = new LinkedList<MoveDouble>();					//list of moves for priority heap
			LinkedList<ScoreBoard_double> child_boards = new LinkedList<ScoreBoard_double>();	//list of boards

			for(int y = 0; y < M; y++) {
				for(int x = 0; x < N; x++) {
					if(toCheck[y][x]) {
						board.markCell(y, x);
						MoveDouble childMove = new MoveDouble(new MNKCell(y, x), getMinScore());
						child_moves.addLast(childMove);
						child_boards.addLast(new ScoreBoard_double(new ArrayBoardHeuristic(board), childMove));
					}
					// update structures:
					secondPH.addAll(child_moves);
					if(!child_boards.isEmpty()) states_at_depth.addLast(new ChildStates_check_d(secondPH, child_boards));
				}
			}

			// START CHECKING NEW BOARDS
			// for each set of "brothers" (nodes in game tree at same depth, sharing same parent node)
			while(!isTimeEnded() && !states_at_depth.isEmpty()) {
				ChildStates_check_d currentSet = states_at_depth.remove();
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

			// UPDATE bestMove
			if(secondPH != null) bestMove = secondPH.getBest();
			
			
			// WARNING: missing check on all recursive calls where didn't check all possible child moves (excluding secondPH)
			// OSS: secondPH needed??

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

		protected void initAttributes() {
			super.initAttributes();
			//firstPH
			firstPH = null;
			//states_at_depth
			states_at_depth = new LinkedList<ChildStates_check_d>();
		}

		protected void init_firstPH() {
			MoveDouble[] V = new MoveDouble[1];
			if(board.MarkedCells_length() > 0) V[0] = new MoveDouble(new MNKCell(-1, -1), getMinScore());			//if not first turn: use last move
			else V[0] = new MoveDouble(new MNKCell(-1, -1), getMinScore());											//else if first turn: empty move
			firstPH = new StatesPHcheck_double(Arrays.asList(V), PHOrder.GREATER, null, null, board.MarkedCells_length());
		}

	//#endregion INIT


	
}
