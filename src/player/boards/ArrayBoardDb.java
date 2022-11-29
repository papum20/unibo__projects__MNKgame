/*
 * MNKBoard MADE IN MORE EFFICIENT WAY FOR PLAYER'S USE
 * i.e. USES ARRAYS FOR THINGS SUCH AS FC AND MC, AND USES
 * CONSTANT COST FOR OPERATIONS LIKE MODIFYING FC AND MC
 * 
 * FOR EFFICIENCY PURPOSES, THE CLASS ASSUMES A GOOD USE,
 * i.e. DOESN'T PROVIDE ANY ERROR-THROWING AND SIMILIAR STUFF
 * ON BAD USES
 */


package player.boards;



import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import player.dbsearch.structures.Operator;



public class ArrayBoardDb implements IBoardDB {
	
	public final int M;		// rows
	public final int N;		// columns
	public final int K;		// Number of symbols to be aligned (horizontally, vertically, diagonally) for a win

	public final MNKCellState[][] B;	// board
	protected MNKCell[] MC; 			// Marked Cells
	protected MNKCell[] FC; 			// Free Cells
	protected int MC_n;					// marked cells number
	protected int FC_n;					// free cells number
	private int[][] FC_indexes;			// cell y,x=index to element y,x in FC

	private final MNKCellState[] Player = {MNKCellState.P1, MNKCellState.P2};
	protected int currentPlayer;		// currentPlayer plays next move (= 0 or 1)
	private MNKGameState gameState;
  

	


  	/**
 	 * Create a board of size MxN and initialize the game parameters
 	 * 
 	 * @param M Board rows
	 * @param N Board columns
	 * @param K Number of symbols to be aligned (horizontally, vertically, diagonally) for a win
   	 *
	 */
  	public ArrayBoardDb (int M, int N, int K) {
	  	this.M  = M;
	  	this.N  = N;
	  	this.K  = K;
		this.gameState = MNKGameState.OPEN;

	  	B  = new MNKCellState[M][N];
	  	FC = new MNKCell[M*N]; 
	  	MC = new MNKCell[M*N];
		FC_indexes = new int[M][N];

	  	reset();
	}
	public ArrayBoardDb(ArrayBoardDb AB) {
		this.M  = AB.M;
		this.N  = AB.N;
		this.K  = AB.K;
	  this.gameState = AB.gameState;
	
		B  = new MNKCellState[M][N];
		FC = new MNKCell[M*N]; 
		MC = new MNKCell[M*N];
		FC_indexes = new int[M][N];
	
		copyArrays(AB);
	}
  
 
 	/**
 	 * Marks the selected cell for the current player
 	 * @param y y-th row
 	 * @param x x-th column
 	 * @return State of the game after the move
	 * @PRECONDITION: GameState==OPEN
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
	 @PRECONDITION: MC.length > 0
 	 */
	public void unmarkCell() {
		MNKCell oldc = MC[MC_n - 1];
		B[oldc.i][oldc.j] = MNKCellState.FREE;
		removeMC();
		addFC(oldc.i, oldc.j);
		currentPlayer = (currentPlayer + 1) % 2;
		gameState = MNKGameState.OPEN;
 	}

	//#region DB
		@Override public boolean isOperatorInCell(int i, int j, short dir, Operator f, MNKCellState player) {
			int i_last = i + DIRECTIONS[dir].i() * f.precondition.length,
				j_last = i + DIRECTIONS[dir].j() * f.precondition.length;
			if(i_last < 0 || i_last >= M || j_last < 0 || j_last >= N) return false;
			else {
				MNKCellState[] precondition = Operator.toMNKCellState(f.precondition, player);
				for(int len = 0; len < f.precondition.length; len++) {
					if(cellState(i, j) != precondition[len]) return false;
					else {
						i += DIRECTIONS[dir].i();
						j += DIRECTIONS[dir].j();
					}
				}
				return true;
			}
		}
		@Override public void applyOperator(int i, int j, short dir, Operator f, MNKCellState attacker) {
			MNKCellState defender = (attacker == MNKCellState.P1)? MNKCellState.P1 : MNKCellState.P2;
			for(int k = 0; k < f.length(); k++) {
				if(f.add[k] != Operator.FREE)
					B[i][j] = (f.add[k] == Operator.ATTACKER)? attacker : defender;
				i += DIRECTIONS[dir].i();
				j += DIRECTIONS[dir].j();
			}
		}
		@Override public void undoOperator(int i, int j, short dir, Operator f, MNKCellState attacker) {
			MNKCellState defender = (attacker == MNKCellState.P1)? MNKCellState.P1 : MNKCellState.P2;
			for(int k = 0; k < f.length(); k++) {
				if(f.precondition[k] == Operator.FREE) B[i][j] = MNKCellState.FREE;
				else B[i][j] = (f.precondition[k] == Operator.ATTACKER)? attacker : defender;
				i += DIRECTIONS[dir].i();
				j += DIRECTIONS[dir].j();
			}
		}
	//#endregion DB

	
	//#region GET

		/**
		 * @param c : cell to convert
		 * @return : converts cell in single number (giving an index to each cell of the board from top left to bottom right)
		 */
		/*public int single(MNKCell c) {
			return c.i * N + c.j;
		}*/
		/**
		 * @param c : single index to convert in cell
		 * @return : converts  single number in cell (inverse of single())
		 */
		public MNKCell cell(int single) {
			return new MNKCell(single / N, single - (single / N) * N);
		}

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
			/*MNKCell lastc = FC[FC_n - 1];
			swap(FC, FC_indexes[y][x], FC_n - 1);
			FC_indexes[lastc.i][lastc.j] = FC_indexes[y][x];
			FC_n--;*/
			swap(FC, FC_indexes[y][x], FC_n-- - 1);
			MNKCell newc = FC[FC_indexes[y][x]];
			FC_indexes[newc.i][newc.j] = FC_indexes[y][x];
		}
		private void addFC(int y, int x) {
			swap(FC, FC_indexes[y][x], FC_n);
			MNKCell oldc = FC[FC_n];
			FC_indexes[oldc.i][oldc.j] = FC_n;
			FC_n++;
		}
		private void addFC_new(int y, int x) {
			FC[FC_indexes[y][x]] = 
			FC[FC_n] = FC[FC_indexes[y][x]];
			
			FC[FC_n] = new MNKCell(y, x);
			FC_indexes[y][x] = FC_n;
			FC_n++;
		}
		private void removeMC() {
			MC_n--;
		}
		private void addMC(int y, int x) {
			MC[MC_n++] = new MNKCell(y, x);
		}	
		//Checks if a cell is within the bounds of the matrix
		/*private boolean in_bounds(int y, int x){
			if (y >= 0 && y < M && x >= 0 && x < N)
				return true;
			else
				return false;
		}*/
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
			private void initFreeCells() {
				FC_n = 0;
			for(int y = 0; y < M; y++) 
				for(int x = 0; x < N; x++) addFC_new(y, x);
		}
		// Resets the marked cells list
			private void initMarkedCells() {
			MC_n = 0;
		}
		// Copy:
		private void copyBoard(ArrayBoardDb AB) {
			for(int i = 0; i < M; i++)
				for(int x = 0; x < N; x++) B[i][x] = AB.B[i][x];
		}
		private void copyFreeCells(ArrayBoardDb AB) {
			FC_n = AB.FC_n;
			for(int i = 0; i < FC_n; i++) FC[i] = copyCell(AB.FC[i]);
		}
		private void copyMarkedCells(ArrayBoardDb AB) {
			MC_n = AB.MC_n;
			for(int i = 0; i < MC_n; i++) MC[i] = copyCell(AB.MC[i]);
		}
		private void copyFCindexes(ArrayBoardDb AB) {
			for(int y = 0; y < M; y++) 
				for(int x = 0; x < N; x++) FC_indexes[y][x] = AB.FC_indexes[y][x];
		}
		// copies an MNKCell
		private MNKCell copyCell(MNKCell c) {
			return new MNKCell(c.i, c.j, c.state);
		}
		/**
		 * Resets the MNKBoard
		 */
		public void reset() {
			currentPlayer = 0;
			initBoard();
			initFreeCells();
			initMarkedCells();
		}
		public void copyArrays(ArrayBoardDb AB) {
			copyBoard(AB);
			copyFreeCells(AB);
			copyMarkedCells(AB);
			copyFCindexes(AB);
		}
	//#endregion INIT
		
	
}
