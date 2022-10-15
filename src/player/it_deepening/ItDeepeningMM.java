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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.ArrayBoard;
import player.ArrayBoardHeuristic;
import structures.PHElement;
import structures.PHOrder;
import structures.PriorityHeap;



public class ItDeepeningMM extends ItDeepeningSmartInterface {

	protected int depth_min;				//first depth to look at



	//#region CLASSES

	/**
	 * @param <B> board type
	 * @param <K> key type for move
	 * @param <S> move type
	 */
	protected class ScoreBoard<B extends ArrayBoard, K, M extends Move<M, K>> {
		public final B board;
		public final M lastMove;
		public ScoreBoard(B board, M lastMove) {
			this.board = board;
			this.lastMove = lastMove;
		}
	}
	protected class ScoreBoard_double extends ScoreBoard<ArrayBoardHeuristic, Double, MoveDouble> {
		public ScoreBoard_double(ArrayBoardHeuristic board, MoveDouble lastMove) {
			super(board, lastMove);
		}
	}
	/**
	 * CLASS REPRESENTING A LIST OF GAME STATES, AS PAIRS OF BOARD-MOVE WITH SCORE, ALL DERIVING FROM THE SAME GAME STATE
	 * (i.e. THEY DIFFER FROM THE PARENT STATE BY ONE MOVE);
	 * THE CLASS SAVES THIS LIST AND ITS ASSOCIATE PriorityHeap (RELATIVE TO ITS ELEMENTS)
	 * THE LIST IS USED LIKE A QUEUE
	 * @param <B> type of score-board pair
	 * @param <K> type of key for move
	 * @param <M> type of move for Score-board pair
	 * @param <SB> type of Score-board pair
	 * @param <H> type of (States_)priorityHeap
	 */
	protected class ChildStates<B extends ArrayBoard, K, M extends Move<M, K>, SB extends ScoreBoard<B, K, M>, H extends States_priorityHeap<K, M, H>> {
		protected final LinkedList<SB> boards;	//boards with lastMove/score
		protected final H PH;					//own relative priorityHeap

		public ChildStates(H PH) {
			boards = new LinkedList<SB>();
			this.PH = PH;
		}
		public ChildStates(H PH, SB scoreBoard) {
			boards = new LinkedList<SB>();
			boards.add(scoreBoard);
			this.PH = PH;
		}
		public ChildStates(H PH, LinkedList<SB> scoreBoards) {
			this.boards = scoreBoards;
			this.PH = PH;
		}
		/**
		 * @param board : element to add as last
		 */
		public void push(SB scoreBoard) {
			boards.addLast(scoreBoard);
		}
		/**
		 * @return first board added
		 */
		public SB pop() {
			return boards.removeFirst();
		}
		public boolean isEmpty() {
			return boards.size() == 0;
		}
		public final LinkedList<SB> getBoards()	{return boards;}
		public final H getPH()					{return PH;}
	}
	// INSTANCE OF ChildStates CLASS
	protected class ChildStates_double extends ChildStates<ArrayBoardHeuristic, Double, MoveDouble, ScoreBoard_double, States_PH_double> {
		public ChildStates_double(States_PH_double PH) {
			super(PH);
		}
		public ChildStates_double(States_PH_double PH, ScoreBoard_double scoreBoard) {
			super(PH, scoreBoard);
		}
		public ChildStates_double(States_PH_double PH, LinkedList<ScoreBoard_double> scoreBoards) {
			super(PH, scoreBoards);
		}
	}
	
	//#endregion CLASSES


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
			return "ItDeepeningMM";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

		/**
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 * @param my_turn = true
		 * @param depth = 0
		 */
		@Override
		protected void visitInLine() {
			depth_max = 1;

			//
			MoveDouble[] firstLevel = {new MoveDouble()};
			States_PH_double currentPH = new States_PH_double(Arrays.asList(firstLevel), PHOrder.GREATER, null, null);
			// list of boards to be evaluated at current depth, grouped by parent game state 
			LinkedList<ChildStates_double> states_at_depth = new LinkedList<ChildStates_double>();
			states_at_depth.add(new ChildStates_double(currentPH, new ScoreBoard_double(board, bestMove)));

			// for each set of "brothers" (nodes in game tree at same depth, sharing same parent node)
			while(!isTimeEnded() && !states_at_depth.isEmpty()) {
				ChildStates_double currentSet = states_at_depth.remove();
				// for each "brother"
				while(!isTimeEnded() && !currentSet.isEmpty()) {
					//get structures for child nodes to check at next depth
					ScoreBoard_double currentBoard = currentSet.pop();
					LinkedList<MoveDouble> child_moves = new LinkedList<MoveDouble>();
					LinkedList<ScoreBoard_double> child_boards = new LinkedList<ScoreBoard_double>();
					//retrieve scores for next depth
					visitAtDepth(currentBoard.board, child_moves, child_boards);
					//update "global" structures
					if(!child_moves.isEmpty()) {
						//new priorityHeap for children of the node i'm checking
						States_PH_double childrenPH = new States_PH_double(child_moves, checkTurn(currentBoard.board) ? PHOrder.GREATER : PHOrder.LESS, currentSet.getPH(), currentBoard.lastMove);
						childrenPH.updateParent();
						//new list of boards to check later, at next depth
						if(!child_boards.isEmpty()) states_at_depth.addLast(new ChildStates_double(childrenPH, child_boards));
					}
				}
			}
		}
		/**
		 * 
		 * @param currentBoard
		 * @param child_moves
		 * @param child_boards
		 * @param my_turn
		 * @param depth
		 */
		protected void visitAtDepth(ArrayBoardHeuristic currentBoard, LinkedList<MoveDouble> child_moves, LinkedList<ScoreBoard_double> child_boards) {
			// doesn't check if gameEnded() : unnecessary, as it doesn't even add ended games to the queue to check them later
			//try all moves and update structures in input
			FC_iterator it = new FC_iterator(board);
			while(!it.ended() && !isTimeEnded())
			{
				MoveDouble next = new MoveDouble(currentBoard.getFreeCell(it.i));		//get next move				
				currentBoard.markCell(next.position.i, next.position.j);

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
				
				next.score = currentBoard.evaluate();	//calculate score for next move
				//update child_boards: add current board with last move, if game is not ended
				if(currentBoard.gameState() == MNKGameState.OPEN) child_boards.addLast(new ScoreBoard_double(new ArrayBoardHeuristic(currentBoard), next));
				currentBoard.unmarkCell();				//then release last move
				//update child_moves: add this move with its score
				child_moves.addLast(next);

				// DEBUG
				//System.out.println((my_turn ? "ME" : "YOU") + " " + Integer.toString(next.position.i) + " " + Integer.toString(next.position.j) + ":" + Integer.toString(alpha)+ " " + Integer.toString(beta) + "/" + Integer.toString(next.score));
				
				it.iterate();
			}
		}


	//#endregion ALGORITHM


	//#region INIT

		protected void initAttributes() {
			super.initAttributes();
			depth_min = 1;
		}

		//#endregion INIT


	
}
