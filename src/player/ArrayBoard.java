/*
 * MNKBoard MADE IN MORE EFFICIENT WAY FOR PLAYER'S USE
 * i.e. USES ARRAYS FOR THINGS SUCH AS FC AND MC, AND USES
 * CONSTANT COST FOR OPERATIONS LIKE MODIFYING FC AND MC
 * 
 * FOR EFFICIENCY PURPOSES, THE CLASS ASSUMES A GOOD USE,
 * i.e. DOESN?T PROVIDE ANY ERROR-THROWING AND SIMILIAR STUFF
 * ON BAD USES
 */


package player;



import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;



public class ArrayBoard {

	public final int M;		// rows
	public final int N;		// columns
	public final int K;		// Number of symbols to be aligned (horizontally, vertically, diagonally) for a win


	protected final MNKCellState[][] B;	// board
	protected MNKCell[] MC; 			// Marked Cells
	protected MNKCell[] FC; 			// Free Cells
	protected int MC_n;					// marked cells number
	protected int FC_n;					// free cells number
	private int[][] FC_indexes;			// cell i,x=index to element i,x in FC

	private final MNKCellState[] Player = {MNKCellState.P1, MNKCellState.P2};
	protected int currentPlayer;		// currentPlayer plays next move
	private MNKGameState gameState;
  

	


  	/**
 	 * Create a board of size MxN and initialize the game parameters
 	 * 
 	 * @param M Board rows
	 * @param N Board columns
	 * @param K Number of symbols to be aligned (horizontally, vertically, diagonally) for a win
   	 *
	 */
  	public ArrayBoard (int M, int N, int K) {
	  	this.M  = M;
	  	this.N  = N;
	  	this.K  = K;
		this.gameState = MNKGameState.OPEN;

	  	B  = new MNKCellState[M][N];
	  	FC = new MNKCell[M*N]; 
	  	MC = new MNKCell[M*N]; 

	  	reset();
	}

 	/**
	 * Resets the MNKBoard
	 */
	public void reset() {
	  	currentPlayer = 0;
	  	initBoard();
	  	initFreeCellList();
	  	initMarkedCellList();
 	}
  
 
 	/**
 	 * Marks the selected cell for the current player
 	 * @param y i-th row
 	 * @param x x-th column
 	 * @return State of the game after the move
  	 */
 	public void markCell(int y, int x) {
		removeFC(y, x);
		addMC(y, x);
		B[y][x] = Player[currentPlayer];
		currentPlayer = (currentPlayer + 1) % 2;
		//update gameState
		if(isWinningCell(y, x))
			gameState = B[y][x] == MNKCellState.P1 ? MNKGameState.WINP1 : MNKGameState.WINP2;
		else if(FC_n == 0)
			gameState = MNKGameState.DRAW;
	}
	/**
 	 * Undoes last move
 	 */
	public void unmarkCell() {
		MNKCell oldc = MC[MC_n - 1];
		B[oldc.i][oldc.j] = MNKCellState.FREE;
		removeMC();
		addFC(oldc.i, oldc.j);
		currentPlayer = (currentPlayer + 1) % 2;
 	}

	
	//#region GET
		/**
			* Returns the state of cell <code>i,x</code>
			* @param i i-th row
			* @param x x-th column
			* @return State of the <code>i,x</code> cell (FREE,P1,P2)
		*/
		public MNKCellState cellState(int y, int x) {
			return B[y][x];
		}
	
  		/**
   			* Returns the current state of the game.
 			* @return MNKGameState enumeration constant (OPEN,WINP1,WINP2,DRAW)
 		*/
  		public MNKGameState gameState() {
  			return gameState;
		}
	
 		/**
 			* Returns the id of the player allowed to play next move. 
 			* @return 0 (first player) or 1 (second player)
 		*/
 		public int currentPlayer() {
			return currentPlayer;	
 		}
	
		/**
   			* Returns the marked cells list in array format.
   			* <p>This is the history of the game: the first move is in the
   			* array head, the last move in the array tail.</p>
   			* @return List of marked cells
   		*/ 
		   public MNKCell getMarkedCell(int i) {
			return MC[i];
 		}
 		/**
 			* Returns the free cells list in array format.
 			* <p>There is not a predefined order for the free cells in the array</p>
 			* @return List of free cells
 		*/
 		public MNKCell getFreeCell(int i) {
		  	return FC[i];
		}
		public int MarkedCells_length() {
			return MC_n;
		}
		public int FreeCells_length() {
			return FC_n;
		}
 	//#endregion GET

  	//#region AUXILIARIY

		//swaps two elements in an array
		protected <T> void swap(T[] V, int a, int b) {
			T tmp = V[a];
			V[a] = V[b];
			V[b] = tmp;
		}

		private void removeFC(int y, int x) {
			swap(FC, FC_indexes[y][x], FC_n - 1);
			FC_n--;
		}
		private void addFC(int y, int x) {
			FC[FC_n++] = new MNKCell(y, x);
		}
		private void removeMC() {
			MC_n--;
		}
		private void addMC(int y, int x) {
			MC[MC_n++] = new MNKCell(y, x);
		}

		// Check winning state from cell y, x
			private boolean isWinningCell(int y, int x) {
			MNKCellState s = B[y][x];
			int n;
			
				// Horizontal check
				n = 1;
				for(int k = 1; x-k >= 0 && B[y][x-k] == s; k++) n++; // backward check
				for(int k = 1; x+k <  N && B[y][x+k] == s; k++) n++; // forward check   
				if(n >= K) return true;
				// Vertical check
				n = 1;
				for(int k = 1; y-k >= 0 && B[y-k][x] == s; k++) n++; // backward check
				for(int k = 1; y+k <  M && B[y+k][x] == s; k++) n++; // forward check
				if(n >= K) return true;
				// Diagonal check
				n = 1;
				for(int k = 1; y-k >= 0 && x-k >= 0 && B[y-k][x-k] == s; k++) n++; // backward check
				for(int k = 1; y+k <  M && x+k <  N && B[y+k][x+k] == s; k++) n++; // forward check
				if(n >= K) return true;
				// Anti-diagonal check
				n = 1;
				for(int k = 1; y-k >= 0 && x+k < N  && B[y-k][x+k] == s; k++) n++; // backward check
				for(int k = 1; y+k <  M && x-k >= 0 && B[y+k][x-k] == s; k++) n++; // backward check
				if(n >= K) return true;
			
			return false;
		}
	//#endregion AUXILIARY
		
	//#region INIT
		// Sets to free all board cells
		private void initBoard() {
			for(int i = 0; i < M; i++)
			for(int x = 0; x < N; x++) B[i][x] = MNKCellState.FREE;
		}
			// Rebuilds the free cells set 
			private void initFreeCellList() {
				FC_n = M * N;
				int i = 0;
			for(int y = 0; y < M; y++) {
				for(int x = 0; x < N; x++) {
					FC[y] = new MNKCell(y, x);
					FC_indexes[y][x] = i++;
				}
			}
		}
		// Resets the marked cells list
			private void initMarkedCellList() {
			MC_n = 0;
		}
	//#endregion INIT
		
	
}
