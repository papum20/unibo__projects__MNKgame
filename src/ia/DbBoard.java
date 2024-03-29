package ia;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Random;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import ia.BiList.BiNode;
import ia.DbSearch.Combined;
import ia.Operators.Threat;
import ia.Operators.USE;



public class DbBoard {

	public static final MovePair DIRECTIONS[] = {
		new MovePair(-1, 0),
		new MovePair(-1, 1),
		new MovePair(0, 1),
		new MovePair(1, 1),
		new MovePair(1, 0),
		new MovePair(1, -1),
		new MovePair(0, -1),
		new MovePair(-1, -1)
	};

	public final int M;		// rows
	public final int N;		// columns
	public final int K;		// Number of symbols to be aligned (horizontally, vertically, diagonally) for a win
	protected static final MovePair MIN = new MovePair(0, 0);
	protected final MovePair MAX;

	protected MNKCellState[][] B;	// board
	protected MNKCell[] MC; 			// Marked Cells
	protected MNKCell[] FC; 			// Free Cells
	protected int MC_n;					// marked cells number
	protected int FC_n;					// free cells number
	protected LinkedList<AppliedThreat> markedThreats;
	protected int[][] FC_indexes;		// cell y,x=index to element y,x in FC

	protected static TranspositionTable TT;
	protected long hash;

	//AUXILIARY STRUCTURES (BOARD AND ARRAYS) FOR COUNTING ALIGNMENTS
	protected AlignmentsList lines_rows;
	protected AlignmentsList lines_cols;
	protected AlignmentsList lines_dright;		//diagonals from top-left to bottom-right
	protected AlignmentsList lines_dleft;		//diagonals from top-right to bottom-left
	/*
	 * horizontal:	dimension=M,		indexed: by row
	 * vertical:	dimension=N,		indexed: by col
	 * dright:		dimension=M+N-1,	indexed: by start of diagonal on the top row, i.e. from -M+1 to N-1
	 * dleft:		dimension=M+N-1,	indexed: by start of diagonal on the top row, i.e. from 0 to N+M-1
	 */
	protected AlignmentsList[] lines_per_dir;								//for each direction 0-3, contains the reference to the proper lines array(list)
	protected static final int[] lines_dirs = new int[]{2, 4, 3, 5};		//indexes in DIRECTIONS, with same order as lines_per_dir
	

	protected BiList_NodeOpPos[][] cells_lines;

	protected final MNKCellState[] Player = {MNKCellState.P1, MNKCellState.P2};
	protected int currentPlayer;		// currentPlayer plays next move (= 0 or 1)
	protected MNKGameState gameState;
  

	

	public DbBoard(int M, int N, int K) {
		this.M  = M;
		this.N  = N;
		this.K  = K;
	 	this.gameState = MNKGameState.OPEN;
		MAX = new MovePair(M, N);
		hash = 0;
		initStructures();

		initLinesStructures();
		reset();
	}
	public DbBoard(DbBoard board, boolean copy_threats) {
		this.M  = board.M;
		this.N  = board.N;
		this.K  = board.K;
		this.gameState = board.gameState;
		MAX = new MovePair(M, N);
		currentPlayer = board.currentPlayer;
		hash = board.hash;
		initStructures();
		
		if(copy_threats) copyLinesStructures(board);
		else initLinesStructures();
		copyArrays(board);
	}

	//public boolean isOperatorInCell(int i, int j, short dir, Operator f, MNKCellState player);
	//marks all involved cells (-delete +add) for current player, without changing current player
	//public void applyOperator(int i, int j, short dir, Operator f, MNKCellState attacker);
	//public void undoOperator(int i, int j, short dir, Operator f, MNKCellState attacker);

	//#region BOARD
	
		/**
		 * Marks the selected cell for the current player
		* @param y y-th row
		* @param x x-th column
		* @return State of the game after the move
		* @PRECONDITION: GameState==OPEN
		*/
		/*
		public void markCell(int y, int x) {
			MovePair c = new MovePair(y, x);
			removeFC(y, x);
			B[y][x] = Player[currentPlayer];
			addMC(y, x, B[y][x]);
			// UPDATE ALIGNMENTS
			// remove alignments for both players involving this cell
			removeAlignments(c, Auxiliary.opponent(B[y][x]));
			removeAlignments(c, B[y][x]);
			// add alignments for player
			for(int d = 0; d < lines_dirs.length; d++) addAlignments(c, B[y][x], d);
			//update gameState
			if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
			currentPlayer = (currentPlayer + 1) % 2;
		}
		*/
		// marks cells in order, all for current player; doesn't change current player
		// PRECONDITIONS: needed at least two moves; all must be in line
		/*public void markCells(MovePair[] c) {
			int i;
			MNKCellState player = Player[currentPlayer];
			for(i = 0; i < c.length; i++) {
				removeFC(c[i].i(), c[i].j());
				addMC(c[i].i(), c[i].j(), player);
				B[c[i].i()][c[i].j()] = player;
			}
			//currentPlayer = (currentPlayer + 1) % 2;
			//update gameState
			// UPDATE ALIGNMENTS
			// remove alignments for both players involving this cell
			for(i = 0; i < c.length; i++) {
				removeAlignments(c[i], Auxiliary.opponent(player));
				removeAlignments(c[i], player);
			}
			// add alignments for player
			MovePair dir = c[0].getDirection(c[1]);
			int dir_index = dirsIndexes(dir);
			for(int d = 0; d < lines_dirs.length; d++) {
				if(d != dir_index) {
					for(i = 0; i < c.length; i++) addAlignments(c[i], player, d);
				} else {
					addAlignments_from(c[0], MIN, player, d);
					for(i = 1; i < c.length; i++) addAlignments_from(c[i], c[i-1], player, d);
				}
			}
		}*/
		private void markCell(int i, int j, MNKCellState state) {
			removeFC(i, j);
			addMC(i, j, state);
			B[i][j] = state;
			hash = TT.getHash(hash, i, j, cellState2int(state));
		}
		public void markCell(MovePair cell) {markCell(cell.i(), cell.j(), Player[currentPlayer]);}
		public void markCell(MNKCell cell) {markCell(cell.i, cell.j, cell.state);}
		public void markCell(MovePair cell, MNKCellState player) {markCell(cell.i(), cell.j(), player);}
		public void markCells(MovePair[] cells, MNKCellState player) {
			for(MovePair c : cells) markCell(c.i(), c.j(), player);
		}
		public void markCells(MovePair[] threat, int atk) {
			int i;
			for(i = 0; i < threat.length; i++) {
				MNKCellState state = Player[(i == atk) ? currentPlayer : (1 - currentPlayer)];
				markCell(threat[i].i(), threat[i].j(), state);
			}
		}
		public void updateAlignments(MovePair cell, MNKCellState player) {
			// remove alignments for both players involving this cell
			removeAlignments(cell, Auxiliary.opponent(player));
			removeAlignments(cell, player);
			// add alignments for player
			for(int d = 0; d < lines_dirs.length; d++) addAlignments(cell, player, d, Operators.TIER_MAX);
			//update gameState
			if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
			//currentPlayer = (currentPlayer + 1) % 2;
		}
		/*public void markCells(MNKCell[] c, int max_tier) {
			int i;
			for(i = 0; i < c.length; i++) markCell(c[i].i, c[i].j, c[i].state);
			// UPDATE ALIGNMENTS
			// remove alignments for both players involving this cell
			for(i = 0; i < c.length; i++) {
				MovePair t = new MovePair(c[i]);
				removeAlignments(t, MNKCellState.P1);
				removeAlignments(t, MNKCellState.P2);
			}
			// add alignments for player
			if(c.length == 1) {
				for(int d = 0; d < lines_dirs.length; d++)
					addAlignments(new MovePair(c[0]), c[0].state, d, max_tier);
			} else {
				MovePair t0 = new MovePair(c[0]);
				MovePair dir = t0.getDirection(new MovePair(c[1]));
				int dir_index = dirsIndexes(dir);
				for(int d = 0; d < lines_dirs.length; d++) {
					if(d != dir_index) {
						for(i = 0; i < c.length; i++) addAlignments(new MovePair(c[i]), c[i].state, d, max_tier);
					} else {
						addAlignments_from(t0, MIN, c[0].state, d, max_tier);
						for(i = 1; i < c.length; i++) addAlignments_from(new MovePair(c[i]), new MovePair(c[i-1]), c[i].state, d, max_tier);
					}
				}
			}
			//update gameState
			if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
		}
		public void markCells(LinkedList<MNKCell> c, Combined combined, int max_tier, boolean check_win, boolean aligned) {
			!
			ListIterator<MNKCell> it = c.listIterator();
			while(it.hasNext()) markCell(it.next());
			// UPDATE ALIGNMENTS
			// remove alignments for both players involving this cell
			it = c.listIterator();
			while(it.hasNext()) {
				MovePair t = new MovePair(it.next());
				removeAlignments(t, MNKCellState.P1);
				removeAlignments(t, MNKCellState.P2);
			}
			// add alignments for player
			it = c.listIterator();
			while(it.hasNext()) {
				MNKCell t = it.next();
				for(int d = 0; d < lines_dirs.length; d++)
					addAlignments_fromCombined(new MovePair(t), combined, t.state, d, max_tier);
			}
			//update gameState
			if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
		}*/
		/**
		 * Undoes last move
		 @PRECONDITION: MC.length > 0
		 */
		/*public void unmarkCell() {
			MNKCell oldc = MC[MC_n - 1];
			MovePair c = new MovePair(oldc);
			MNKCellState player = cellState(oldc);
			// UPDATE STRUCTURES
			B[oldc.i][oldc.j] = MNKCellState.FREE;
			removeMC();
			addFC(oldc.i, oldc.j);
			currentPlayer = (currentPlayer + 1) % 2;
			gameState = MNKGameState.OPEN;
			// UPDATE ALIGNMENTS
			// remove alignments for player involving this cell
			removeAlignments(c, player);
			// add alignments for both players
			for(int d = 0; d < lines_dirs.length; d++) addAlignments(c, player, d);
			for(int d = 0; d < lines_dirs.length; d++) addAlignments(c, Auxiliary.opponent(player), d);
		}
		public void unmarkCells(int n) {
			for(int i = 0; i < n; i++) unmarkCell();
		}*/
		public MNKCellState cellState(int y, int x) {return B[y][x];}
		public MNKCellState cellState(MNKCell c) {return B[c.i][c.j];}
		public MNKCellState cellState(MovePair c) {return B[c.i()][c.j()];}
		public MNKGameState gameState() {return gameState;}
		public void setGameState(MNKGameState state) {gameState = state; }
		public int currentPlayer() {return currentPlayer;}
		public void setPlayer(MNKCellState player) {currentPlayer = (player == this.Player[0]) ? 0 : 1;}


		public MNKCell getMarkedCell(int i) {return MC[i];}
		//public MNKCell getFreeCell(int i) {return FC[i];}
		public LinkedList<AppliedThreat> getMarkedThreats() {return markedThreats;}
		//public int MarkedCells_length() {return MC_n;}
		//public int FreeCells_length() {reurn FC_n;}

		private void checkAlignments(MovePair cell, int max_tier) {
			if(isWinningCell(cell.i(), cell.j())) gameState = Auxiliary.cellState2winState(B[cell.i()][cell.j()]);
			else {
				for(int d = 0; d < lines_dirs.length; d++) addAlignments(cell, cellState(cell), d, max_tier);
				if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
			}
		}
		private void checkAlignments(MovePair[] cells, int max_tier) {
			if(cells.length == 1) {
				checkAlignments(cells[0], max_tier);
			} else {
				int i = 0;
				while(i < cells.length && gameState == MNKGameState.OPEN) {
					if(isWinningCell(cells[i].i(), cells[i].j())) gameState = Auxiliary.cellState2winState(B[cells[i].i()][cells[i].j()]);
					else i++;
				}
				if(gameState == MNKGameState.OPEN) {
					MovePair dir = cells[0].getDirection(new MovePair(cells[1]));
					int dir_index = dirsIndexes(dir);
					for(int d = 0; d < lines_dirs.length; d++) {
						if(d != dir_index) {
							for(MovePair c : cells) addAlignments(c, cellState(c), d, max_tier);
						} else {
							addAlignments_from(cells[0], MIN, cellState(cells[0]), d, max_tier);
							for(i = 1; i < cells.length; i++) addAlignments_from(cells[i], cells[i-1], cellState(cells[i]), d, max_tier);
						}
					}
					//update gameState
					if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
				}
			}
		}
		/*private void checkAllCombinedAlignments(int max_tier) {
			if(isWinningCell(cell.i(), cell.j())) gameState = Auxiliary.cellState2winState(B[cell.i()][cell.j()]);
			else {
				for(int d = 0; d < lines_dirs.length; d++) addAlignments(cell, cellState(cell), d, max_tier);
				if(FC_n == 0 && gameState == MNKGameState.OPEN) gameState = MNKGameState.DRAW;
			}
		}*/

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

	//#endregion BOARD
		
		
		
	
	//#region DB_SEARCH

		/**
		 * 
		 * @param threat : as defined in Operators
		 * @param atk : attacker's move index in threat
		 * @param use : as def in Operators
		 * @param threats : wether to update alignments and threats for this board
		 * @return :	a new board resulting after developing this with such threat (dependency stage);
		 * 				the new board only has alignment involving the newly marked cells
		 */
		public DbBoard getDependant(Threat threat, int atk, USE use, int max_tier, boolean check_threats) {
			DbBoard res = new DbBoard(this, false);
			switch(use) {
				//used for...
				case ATK:
					MovePair cell = threat.related[threat.nextAtk(atk)];
					res.markCell(cell.i(), cell.j(), Player[currentPlayer]);
					if(check_threats) res.checkAlignments(cell, max_tier);
					break;
				//used for init defensive visit (marks defensive cells as own)
				case DEF:
					res.addThreat(threat, atk, Auxiliary.opponent(Player[currentPlayer]));
					//if there exist any defensive moves
					if(threat.related.length > 1) {
						res.markCells(threat.getDefensive(atk), Player[currentPlayer]);
						if(check_threats) res.checkAlignments(threat.getDefensive(atk), max_tier);
					}
					break;
				//used for dependency stage
				case BTH:
					res.markCells(threat.related, atk);
					res.addThreat(threat, atk, Player[currentPlayer]);
					if(check_threats) res.checkAlignments(threat.related, max_tier);
			}
			return res;
		}
		//only checks for alignments not included in the union of A's and B's alignments, i.e. those which involve at  least one cell only present in A and one only in B
		public DbBoard getCombined(DbBoard board, MNKCellState attacker, int max_tier) {
			DbBoard res = new DbBoard(this, false);
			for(AppliedThreat athreat : board.markedThreats) {
				if(this.isUsefulThreat(athreat, attacker)) {
					//mark other board's threat on res
					for(int i = 0; i < athreat.threat.related.length; i++) {
						MovePair c = athreat.threat.related[i];
						if(B[c.i()][c.j()] == MNKCellState.FREE) {
							if(i == athreat.atk) res.markCell(c, athreat.attacker);
							else res.markCell(c, Auxiliary.opponent(athreat.attacker));
						}
					}
					//add threats
					res.addThreat(athreat);
				}
			}
			//re-calculate alignments
			for(int d = 0; d < lines_per_dir.length; d++)
				res.addAllCombinedAlignments(this, board, attacker, d, max_tier);
			return res;
		}
		public void addAllAlignments(MNKCellState player, int max_threat) {
			for(int i = 0; i < lines_dirs.length; i++)
				addAllAlignments_t(player, i, max_threat);
		}
		//public boolean isOperatorInCell(int i, int j, short dir, Operator f, MNKCellState player);
		////marks all involved cells (-delete +add) for current player, without changing current player
		//public void applyOperator(int i, int j, short dir, Operator f, MNKCellState attacker);
		//public void undoOperator(int i, int j, short dir, Operator f, MNKCellState attacker);
		
	//#endregion DB_SEARCH



	//#region AUXILIARIY
		//#region FC_MC_ARRAYS
			private void removeFC(int y, int x) {
				Auxiliary.swap(FC, FC_indexes[y][x], --FC_n);		//swap this and last
				MNKCell last = FC[FC_indexes[y][x]];				//last, in position where was this
				FC_indexes[last.i][last.j] = FC_indexes[y][x];		//FC_indexes[last] = this position
			}
			// ASSUMES IT'S EITHER P1 OR P2, NOT FREE
			private int cellState2int(MNKCellState state) {
				return (state == Player[0]) ? 0 : 1;
			}
			/*private void addFC(int y, int x) {
				Auxiliary.swap(FC, FC_indexes[y][x], FC_n);
				MNKCell last = FC[FC_n];
				FC_indexes[last.i][last.j] = FC_n;
				FC_n++;
			}
			private void removeMC() {MC_n--;}*/
			private void addMC(MNKCell cell) {MC[MC_n++] = cell;}
			private void addMC(int y, int x, MNKCellState player) {MC[MC_n++] = new MNKCell(y, x, player);}
			public void addThreat(Threat threat, int atk, MNKCellState attacker) {
				// from when appliedThreat was different:
				//MovePair[] def = new MovePair[threat.related.length - 1];
				//for(int i = 0; i < atk; i++) def[i] = threat.related[i];
				//for(int i = atk + 1; i < threat.related.length; i++) def[i - 1] = threat.related[i];
				AppliedThreat at = new AppliedThreat(threat, atk, attacker);
				markedThreats.addLast(at);
			}
			public void addThreat(AppliedThreat a_threat) {
				markedThreats.addLast(a_threat);
			}
		//#endregion FC_MC_ARRAYS

		//#region ALIGNMENTS
			private void removeAlignments(final MovePair center, MNKCellState player) {
				System.out.println("\nremoveAlignments START:");

				int MAX_ALIGNMENT = K - Operators.MAX_LINE + Operators.MAX_FREE_EXTRA_TOT;
				//foreach alignment that was stored in (y,x)
				BiNode<BiNode<OperatorPosition>> node = cells_lines[center.i()][center.j()].getFirst(player);
				if(node != null) {
					do {
						
						MovePair start = node.item.item.start, end = node.item.item.end;
						MovePair dir = start.getDirection(end);
						// DEBUG
						System.out.println("\t\trm: " + node.item.item);
						System.out.println("\t\t" + lineIndex(dir, center));
						//delete for line
						lines_per_dir[dirsIndexes(dir)].remove(player, lineIndex(dir, center), node.item);
						//delete for this cell
						BiNode<BiNode<OperatorPosition>> tmp = node;
						
						node = node.next;
						cells_lines[center.i()][center.j()].remove(player, tmp);
					} while(node != null);
					//delete for each involved cell
					MovePair first, last;
					for(int d = 0; d < lines_dirs.length; d++) {
						MovePair dir = DIRECTIONS[lines_dirs[d]];
						first = new MovePair(center); last = new MovePair(center);
						first.clamp_diag(MIN, MAX, DIRECTIONS[lines_dirs[d]].getProd(-(MAX_ALIGNMENT - 1)));
						last.clamp_diag(MIN, MAX, DIRECTIONS[lines_dirs[d]].getProd(MAX_ALIGNMENT - 1));
						boolean checked_all = false;
						while(!checked_all) {
							if(!first.equals(center)) removeAlignmentsInvolvingCell(first, center, player);
							if(first.equals(last)) checked_all = true;
							else first.sum(dir);
						}
					}
				}

				System.out.println("removeAlignments END\n");
			}
			/*private void removeAlignments_line(int y1, int x1, int y2, int x2, MNKCellState player) {
				MovePair center1 = new MovePair(y1, x1), center2 = new MovePair(y2, x2);
				MovePair center_dir = center1...
				int MAX_ALIGNMENT = K - Operator.MAX_LINE + Operator.MAX_FREE_EXTRA_TOT;
				//foreach alignment that was stored in (y,x)
				MovePair center_it = new MovePair(center1);
				while(!center_it.equals(center2)) {
					BiNode<BiNode<OperatorPosition>> node = cells_lines[center_it.i()][center_it.j()].getFirst(player);
					if(node != null) {
						do {
							System.out.println("\t\trm: " + node.item.item);
							MovePair start = node.item.item.start, end = node.item.item.end;
							MovePair dir = start.getDirection(end);
							//delete for line
							//in order not to delete multiple times same alignment, only delete from line for first center,
							//and for others only if alignment starts from there
							if(center_it.equals(center1) || ( (center_it.equals(start) || center_it.equals(end)) && !center1.inBetween_included(start, end) ) ) {
								System.out.println("\t\t" + lineIndex(dir, center_it));
								lines_per_dir[dirsIndexes(dir)].remove(player, lineIndex(dir, center_it), node.item);
							}
							//delete for this cell
							BiNode<BiNode<OperatorPosition>> tmp = node;
							node = node.next;
							cells_lines[y][x].remove(player, tmp);
						} while(node != null);
					}
					//delete for each involved cell
					MovePair first, last;
					for(int d = 0; d < lines_dirs.length; d++) {
						MovePair dir = DIRECTIONS[lines_dirs[d]];
						first = new MovePair(y, x); last = new MovePair(y, x);
						first.clamp_diag(MIN, MAX, DIRECTIONS[lines_dirs[d]].getProd(-(MAX_ALIGNMENT - 1)));
						last.clamp_diag(MIN, MAX, DIRECTIONS[lines_dirs[d]].getProd(MAX_ALIGNMENT - 1));
						boolean checked_all = false;
						while(!checked_all) {
							if(!first.equals(center)) removeAlignmentsInvolvingCell(first, center, player);
							if(first.equals(last)) checked_all = true;
							else first.sum(dir);
						}
					}
				}
			}*/
			/**
			 * 1 -	the cells whose alignments will change are those at max distance K-1 from this;
			 * 2 -	considering the algorithm is correct, we assume hey already have associated, if already
			 * 		existed, alignments from K-MIN_SYM_LINE to K-1 symbols (if existed of K, the game would be ended);
			 * 3 -	that said, we will only need to increase existing alignments by 1 symbol, and to add
			 * 		new alignments of K-MIN_SYM_LINE.
			 * HOWEVER, this will be a future enhancement: for now, the function simply deletes and recreates all
			 */
			private void addAlignments(final MovePair center, final MNKCellState player, int lines_dirs_index, int max_tier) {
				boolean debug = false;
				if(debug) System.out.println("\naddAlignments START:");

				MNKCellState opponent = Auxiliary.opponent(player);
				MovePair dir = DIRECTIONS[lines_dirs[lines_dirs_index]];
				int dir_index = lineIndex(dir, center);							//if horizontal: row index, otherwise (all other cases) col index
				MovePair negdir = dir.getNegative();
				int MAX_LINE = K - Operators.MAX_LINE;
				MovePair end_c1, end_c2, c1, c2;
				//center = starting cell, end_c* = last to check for c*, c1,c2 = iterators (first and last in line to check)
				//c1 goes from center-MAX_LEN to center, c2 from c1 to center+MAX_LEN
				end_c1 = new MovePair(center); end_c2 = new MovePair(center); c1 = new MovePair(center);
				end_c1.clamp_diag(MIN, MAX, dir.getProd(Operators.MAX_FREE_EXTRA - 1) );
				end_c2.clamp_diag(MIN, MAX, dir.getProd(MAX_LINE - 1) );
				int line = 0, mark = 0, in = 0, before = 0, after = 0;
				//count from center to c1
				int dist = 0;
				MovePair c_tmp = center.getSum(negdir);
				while(dist < MAX_LINE - 1 && c_tmp.inBounds(MIN, MAX) && cellState(c_tmp) != opponent) {
					if(cellState(c_tmp) == player) c1.reset(c_tmp);
					c_tmp.sum(negdir);
					dist++;
				}
				c2 = new MovePair(c1);

				if(debug) System.out.println("\t\t\tdir: " + dir);
				boolean checked_all = false;
				while(!checked_all) {
					if(debug) System.out.println("\t\t\t" + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);

					//while (c1 == empty): c1++; c2=c1;
					if(cellState(c1) == MNKCellState.FREE) {
						if(debug) System.out.println("\t\t\t\tc1!=player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
						while(cellState(c1) != player && !c1.equals(end_c1))
							c1.sum(dir);
						if(c1.equals(end_c1) && cellState(c1) == MNKCellState.FREE) checked_all = true;
						else c2.reset(c1);
					}
					if(!checked_all) {
						//while (c2 == empty && !(c2 reached end_c2) ) line++, in++, c2++;		//impossible at first iteration, when c2=c1, because of the lines above
						while(cellState(c2) == MNKCellState.FREE && !c2.equals(end_c2)) {
							//doesn't update line,in when c2==end_c2; however not needed, since in that case it would not check for alignments
							line++;
							in++;
							c2.sum(dir);
						}
						if(debug) System.out.println("\t\t\t\tc2 empty: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
						//if ( !(line exceeded MAX) && c2 == player): line++, mark++; check alignment;
						if(line <= MAX_LINE && cellState(c2) == player) {
							line++;
							mark++;
							if(debug) System.out.println("\t\t\t\tc2 player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							//check alignments
							if(mark >= K-Operators.MARK_DIFF_MIN) {
								int tier = K - mark;
								if(tier <= max_tier)
								{
									//foreach alignment of mark marks
									for(byte code : Operators.ALIGNMENT_CODES[tier]) {
										Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
										if(debug) System.out.println("\t\t\t\t\tal = " + al);
										//if (inner alignment conditions)
										if(line <= K - al.line && in == al.in) {
											//assuming that win is K marks aligned, without free cells, checks wether the game ended
											if(tier == 0) {
												gameState = Auxiliary.cellState2winState(player);
												return;
											}
											//check outer alignment conditions
											before = countMarks( c1.getSum(dir.getNegative()), dir.getNegative(), al.out - al.mnout, MNKCellState.FREE);
											if(before >= al.mnout)
												after = countMarks(c2.getSum(dir), dir, al.out - before, MNKCellState.FREE);
											if(debug) System.out.println("\t\t\t\t\tbefore, after = " + before + "," + after);
											//if (outer conditions)
											MovePair c_start = c1.getSum(dir.getProd(-before));
											MovePair c_end = c2.getSum(dir.getProd(after));
											while(before >= al.mnout && after >= al.mnout && before + after >= al.out) {
												if(center.inBetween_included(c_start, c_end)) {
													OperatorPosition f = new OperatorPosition(c_start, c_end, code);
													if(debug) System.out.println(c_start + "_( " + c1 + "->" + c2 + ") _" + c_end + " : " + f);
													//add to arrays
													BiNode<OperatorPosition> node = lines_per_dir[lines_dirs_index].add(player, dir_index, f);				//add to array for alignments in row/col/diag
													if(debug) System.out.println("line sizes: " + lines_dirs_index + ", " + dir_index + " : " +  lines_per_dir[lines_dirs_index].get(dir_index).isEmpty(player));
													//add reference for all in the middle
													MovePair c_it = new MovePair(c_start);
													boolean ended = false;
													while(!ended) {
														cells_lines[c_it.i()][c_it.j()].add(player, node);								//add to cell's alignments
														if(c_it.equals(c_end)) ended = true;
														else c_it.sum(dir);
													}
													if(debug) System.out.println("cell empty: " + c_it + " : " + cells_lines[c_it.i()][c_it.j()].isEmpty(player));
												}
												//update before, after
												c_end.sum(dir);
												if(!c_end.inBounds(MIN, MAX) || B[c_end.i()][c_end.j()] != MNKCellState.FREE) break;
												else {
													after++;
													before--;
													c_start.sum(dir);
												}
											}
										}
									}
								}
							}
						}	//end if (c2==player)
						//increment c1/c2
						if(c2.equals(end_c2) || line >= MAX_LINE || cellState(c2) == opponent) {
							if(c1.equals(end_c1)) checked_all = true;
							else {
								c1.sum(dir);
								c2.reset(c1);
								line = mark = in = 0;
							}
						}
						else c2.sum(dir);
					}	//end if (!checked_all)
				}	//end while

				if(debug) System.out.println("addAlignments END;\n");
			}
			// used for markCells : adds all alignments involving at least one cell between (y1,x1) and (y2,x2)
			private void addAlignments_from(final MovePair center, final MovePair from, MNKCellState player, int lines_dirs_index, int max_tier) {
				boolean debug = false;

				MNKCellState opponent = Auxiliary.opponent(player);
				MovePair dir = DIRECTIONS[lines_dirs[lines_dirs_index]];
				int dir_index = lineIndex(dir, center);							//if horizontal: row index, otherwise (all other cases) col index
				MovePair negdir = dir.getNegative();
				int MAX_LINE = K - Operators.MAX_LINE;
				MovePair end_c1, end_c2, c1, c2;
				//center = starting cell, end_c* = last to check for c*, c1,c2 = iterators (first and last in line to check)
				//c1 goes from center-MAX_LEN to center, c2 from c1 to center+MAX_LEN
				end_c1 = new MovePair(center); end_c2 = new MovePair(center); c1 = new MovePair(center);
				end_c1.clamp_diag(MIN, MAX, dir.getProd(Operators.MAX_FREE_EXTRA - 1) );
				end_c2.clamp_diag(MIN, MAX, dir.getProd(MAX_LINE - 1) );
				int line = 0, mark = 0, in = 0, before = 0, after = 0;
				//count from center to c1
				int dist = 0;
				if(!from.equals(center)) {
					MovePair c_tmp = center.getSum(negdir);
					while(dist < MAX_LINE - 1 && c_tmp.inBounds(MIN, MAX) && cellState(c_tmp) != opponent) {
						if(cellState(c_tmp) == player) c1.reset(c_tmp);
						if(c_tmp.equals(from)) break;
						c_tmp.sum(negdir);
						dist++;
					}
				} else
					c1.reset(center);
				c2 = new MovePair(c1);

				if(debug) System.out.println("\t\t\tdir: " + dir);
				boolean checked_all = false;
				while(!checked_all) {
					if(debug) System.out.println("\t\t\t" + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);

					//while (c1 == empty): c1++; c2=c1;
					if(cellState(c1) == MNKCellState.FREE) {
						if(debug) System.out.println("\t\t\t\tc1!=player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
						while(cellState(c1) != player && !c1.equals(end_c1))
							c1.sum(dir);
						if(c1.equals(end_c1) && cellState(c1) == MNKCellState.FREE) checked_all = true;
						else c2.reset(c1);
					}
					if(!checked_all) {
						//while (c2 == empty && !(c2 reached end_c2) ) line++, in++, c2++;		//impossible at first iteration, when c2=c1, because of the lines above
						while(cellState(c2) == MNKCellState.FREE && !c2.equals(end_c2)) {
							//doesn't update line,in when c2==end_c2; however not needed, since in that case it would not check for alignments
							line++;
							in++;
							c2.sum(dir);
						}
						if(debug) System.out.println("\t\t\t\tc2 empty: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
						//if ( !(line exceeded MAX) && c2 == player): line++, mark++; check alignment;
						if(line <= MAX_LINE && cellState(c2) == player) {
							line++;
							mark++;
							if(debug) System.out.println("\t\t\t\tc2 player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							//check alignments
							if(mark >= K-Operators.MARK_DIFF_MIN) {
								int tier = K - mark;
								if(tier <= max_tier)
								{
									//foreach alignment of mark marks
									for(byte code : Operators.ALIGNMENT_CODES[tier]) {
										Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
										if(debug) System.out.println("\t\t\t\t\tal = " + al);
										//if (inner alignment conditions)
										if(line <= K - al.line && in == al.in) {
											//assuming that win is K marks aligned, without free cells, checks wether the game ended
											if(tier == 0) {
												gameState = Auxiliary.cellState2winState(player);
												return;
											}
											//check outer alignment conditions
											before = countMarks( c1.getSum(dir.getNegative()), dir.getNegative(), al.out - al.mnout, MNKCellState.FREE);
											if(before >= al.mnout)
												after = countMarks(c2.getSum(dir), dir, al.out - before, MNKCellState.FREE);
											if(debug) System.out.println("\t\t\t\t\tbefore, after = " + before + "," + after);
											//if (outer conditions)
											MovePair c_start = c1.getSum(dir.getProd(-before));
											MovePair c_end = c2.getSum(dir.getProd(after));
											while(before >= al.mnout && after >= al.mnout && before + after >= al.out) {
												if(center.inBetween_included(c_start, c_end)) {
													OperatorPosition f = new OperatorPosition(c_start, c_end, code);
													if(debug) System.out.println(c_start + "_( " + c1 + "->" + c2 + ") _" + c_end + " : " + f);
													//add to arrays
													BiNode<OperatorPosition> node = lines_per_dir[lines_dirs_index].add(player, dir_index, f);				//add to array for alignments in row/col/diag
													if(debug) System.out.println("line sizes: " + lines_dirs_index + ", " + dir_index + " : " +  lines_per_dir[lines_dirs_index].get(dir_index).isEmpty(player));
													//add reference for all in the middle
													MovePair c_it = new MovePair(c_start);
													boolean ended = false;
													while(!ended) {
														cells_lines[c_it.i()][c_it.j()].add(player, node);								//add to cell's alignments
														if(c_it.equals(c_end)) ended = true;
														else c_it.sum(dir);
													}
													if(debug) System.out.println("cell empty: " + c_it + " : " + cells_lines[c_it.i()][c_it.j()].isEmpty(player));
												}
												//update before, after
												c_end.sum(dir);
												if(!c_end.inBounds(MIN, MAX) || B[c_end.i()][c_end.j()] != MNKCellState.FREE) break;
												else {
													after++;
													before--;
													c_start.sum(dir);
												}
											}
										}
									}
								}
							}
						}	//end if (c2==player)
						//increment c1/c2
						if(c2.equals(end_c2) || line >= MAX_LINE || cellState(c2) == opponent) {
							if(c1.equals(end_c1)) checked_all = true;
							else {
								c1.sum(dir);
								c2.reset(c1);
								line = mark = in = 0;
							}
						}
						else c2.sum(dir);
					}	//end if (!checked_all)
				}	//end while
			}
			private void addAlignments_fromCombined(final MovePair center, final Combined combined, MNKCellState player, int lines_dirs_index, int max_tier) {
				MNKCellState opponent = Auxiliary.opponent(player);
				MovePair dir = DIRECTIONS[lines_dirs[lines_dirs_index]];
				int dir_index = lineIndex(dir, center);							//if horizontal: row index, otherwise (all other cases) col index
				MovePair negdir = dir.getNegative();
				int MAX_LINE = K - Operators.MAX_LINE;
				MovePair end_c1, end_c2, c1, c2;
				//center = starting cell, end_c* = last to check for c*, c1,c2 = iterators (first and last in line to check)
				//c1 goes from center-MAX_LEN to center, c2 from c1 to center+MAX_LEN
				end_c1 = new MovePair(center); end_c2 = new MovePair(center); c1 = new MovePair(center);
				end_c1.clamp_diag(MIN, MAX, dir.getProd(Operators.MAX_FREE_EXTRA - 1) );
				end_c2.clamp_diag(MIN, MAX, dir.getProd(MAX_LINE - 1) );
				int line = 0, mark = 0, in = 0, before = 0, after = 0;
				//count from center to c1
				int dist = 0;
				MovePair c_tmp = new MovePair(center);
				do {
					if(cellState(c_tmp) == player) c1.reset(c_tmp);
					c_tmp.sum(negdir);
					dist++;
				} while(dist < MAX_LINE - 1 && c_tmp.inBounds(MIN, MAX) && combined.board[c_tmp.i()][c_tmp.j()] != combined.n && cellState(c_tmp) != opponent);
				c2 = new MovePair(c1);

				System.out.println("\t\t\tdir: " + dir);
				boolean checked_all = false;
				while(!checked_all) {
					System.out.println("\t\t\t" + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);

					//while (c1 == empty): c1++; c2=c1;
					if(cellState(c1) == MNKCellState.FREE) {
						System.out.println("\t\t\t\tc1!=player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
						while(cellState(c1) != player && !c1.equals(end_c1))
							c1.sum(dir);
						if(c1.equals(end_c1) && cellState(c1) == MNKCellState.FREE) checked_all = true;
						else c2.reset(c1);
					}
					if(!checked_all) {
						//while (c2 == empty && !(c2 reached end_c2) ) line++, in++, c2++;		//impossible at first iteration, when c2=c1, because of the lines above
						while(cellState(c2) == MNKCellState.FREE && !c2.equals(end_c2)) {
							//doesn't update line,in when c2==end_c2; however not needed, since in that case it would not check for alignments
							line++;
							in++;
							c2.sum(dir);
						}
						System.out.println("\t\t\t\tc2 empty: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
						//if ( !(line exceeded MAX) && c2 == player): line++, mark++; check alignment;
						if(line <= MAX_LINE && cellState(c2) == player) {
							line++;
							mark++;
							System.out.println("\t\t\t\tc2 player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							//check alignments
							if(mark >= K-Operators.MARK_DIFF_MIN) {
								int tier = K - mark;
								if(tier <= max_tier)
								{
									//foreach alignment of mark marks
									for(byte code : Operators.ALIGNMENT_CODES[tier]) {
										Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
										System.out.println("\t\t\t\t\tal = " + al);
										//if (inner alignment conditions)
										if(line <= K - al.line && in == al.in) {
											//assuming that win is K marks aligned, without free cells, checks wether the game ended
											if(tier == 0) {
												gameState = Auxiliary.cellState2winState(player);
												return;
											}
											//check outer alignment conditions
											before = countMarks( c1.getSum(dir.getNegative()), dir.getNegative(), al.out - al.mnout, MNKCellState.FREE);
											if(before >= al.mnout)
												after = countMarks(c2.getSum(dir), dir, al.out - before, MNKCellState.FREE);
											System.out.println("\t\t\t\t\tbefore, after = " + before + "," + after);
											//if (outer conditions)
											MovePair c_start = c1.getSum(dir.getProd(-before));
											MovePair c_end = c2.getSum(dir.getProd(after));
											while(before >= al.mnout && after >= al.mnout && before + after >= al.out) {
												if(center.inBetween_included(c_start, c_end)) {
													OperatorPosition f = new OperatorPosition(c_start, c_end, code);
													System.out.println(c_start + "_( " + c1 + "->" + c2 + ") _" + c_end + " : " + f);
													//add to arrays
													BiNode<OperatorPosition> node = lines_per_dir[lines_dirs_index].add(player, dir_index, f);				//add to array for alignments in row/col/diag
													System.out.println("line sizes: " + lines_dirs_index + ", " + dir_index + " : " +  lines_per_dir[lines_dirs_index].get(dir_index).isEmpty(player));
													//add reference for all in the middle
													MovePair c_it = new MovePair(c_start);
													boolean ended = false;
													while(!ended) {
														cells_lines[c_it.i()][c_it.j()].add(player, node);								//add to cell's alignments
														if(c_it.equals(c_end)) ended = true;
														else c_it.sum(dir);
													}
													System.out.println("cell empty: " + c_it + " : " + cells_lines[c_it.i()][c_it.j()].isEmpty(player));
												}
												//update before, after
												c_end.sum(dir);
												if(!c_end.inBounds(MIN, MAX) || B[c_end.i()][c_end.j()] != MNKCellState.FREE) break;
												else {
													after++;
													before--;
													c_start.sum(dir);
												}
											}
										}
									}
								}
							}
						}	//end if (c2==player)
						//increment c1/c2
						if(c2.equals(end_c2) || line >= MAX_LINE || cellState(c2) == opponent) {
							if(c1.equals(end_c1)) checked_all = true;
							else {
								c1.sum(dir);
								c2.reset(c1);
								line = mark = in = 0;
							}
						}
						else c2.sum(dir);
					}	//end if (!checked_all)
				}	//end while
			}
			//for combination: adds all alignments which involve at least one cell only present in first and one only in second (and of course this board)
			private void addAllCombinedAlignments(DbBoard first, DbBoard second, MNKCellState player, int lines_dirs_index, int max_tier) {
				boolean debug = false;
				
				MNKCellState opponent = Auxiliary.opponent(player);
				MovePair dir = DIRECTIONS[lines_dirs[lines_dirs_index]];
				int MAX_LINE = K - Operators.MAX_LINE;
				MovePair start, c1, c2;
				//c1,c2 = iterators (first and last in line to check)
				//c1 goes from center-MAX_LEN to center, c2 from c1 to center+MAX_LEN
				start = iterateLineDirs(null, lines_dirs_index);
				for(int i = 0; i < lines_per_dir[lines_dirs_index].size(); i++, start = iterateLineDirs(start, lines_dirs_index))
				{
					c1 = new MovePair(start);
					int dir_index = lineIndex(dir, c1);							//if horizontal: row index, otherwise (all other cases) col index
					int line = 0, mark = 0, in = 0, before = 0, after = 0;
					//count from center to c1
					int dist = 0;
					c2 = new MovePair(c1);

					if(debug) System.out.println("\t\t\tdir: " + dir);
					MovePair next;
					boolean checked_all = false, found_first = false, found_second = false;
					while(!checked_all) {
						if(debug) System.out.println("\t\t\t" + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);

						//while (c1 == empty): c1++; c2=c1;
						if(cellState(c1) == MNKCellState.FREE) {
							if(debug) System.out.println("\t\t\t\tc1!=player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							while(c1.inBounds(MIN, MAX) && cellState(c1) != player && c1.inBounds(MIN, MAX))
								c1.sum(dir);
							if(!c1.inBounds(MIN, MAX)) checked_all = true;
							else c2.reset(c1);
						}
						if(!checked_all) {
							//while (c2 == empty && !(c2 reached end_c2) ) line++, in++, c2++;		//impossible at first iteration, when c2=c1, because of the lines above
							while(cellState(c2) == MNKCellState.FREE && (next = c2.getSum(dir)).inBounds(MIN, MAX)) {
								//doesn't update line,in when c2==end_c2; however not needed, since in that case it would not check for alignments
								line++;
								in++;
								c2.reset(next);;
							}
							if(debug) System.out.println("\t\t\t\tc2 empty: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							//if ( !(line exceeded MAX) && c2 == player): line++, mark++; check alignment;
							if(line <= MAX_LINE && cellState(c2) == player) {
								line++;
								mark++;
								if(first.cellState(c2) != MNKCellState.FREE && second.cellState(c2) == MNKCellState.FREE) found_first = true;
								else if(first.cellState(c2) == MNKCellState.FREE && second.cellState(c2) != MNKCellState.FREE) found_second = true;
								if(debug) System.out.println("\t\t\t\tc2 player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
								//check alignments
								if(found_first && found_second && mark >= K-Operators.MARK_DIFF_MIN) {
									int tier = K - mark;
									if(tier <= max_tier)
									{
										//foreach alignment of mark marks
										for(byte code : Operators.ALIGNMENT_CODES[tier]) {
											Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
											if(debug) System.out.println("\t\t\t\t\tal = " + al);
											//if (inner alignment conditions)
											if(line <= K - al.line && in == al.in) {
												//assuming that win is K marks aligned, without free cells, checks wether the game ended
												if(tier == 0) {
													gameState = Auxiliary.cellState2winState(player);
													return;
												}
												//check outer alignment conditions
												before = countMarks( c1.getSum(dir.getNegative()), dir.getNegative(), al.out - al.mnout, MNKCellState.FREE);
												if(before >= al.mnout)
													after = countMarks(c2.getSum(dir), dir, al.out - before, MNKCellState.FREE);
												if(debug) System.out.println("\t\t\t\t\tbefore, after = " + before + "," + after);
												//if (outer conditions)
												MovePair c_start = c1.getSum(dir.getProd(-before));
												MovePair c_end = c2.getSum(dir.getProd(after));
												while(before >= al.mnout && after >= al.mnout && before + after >= al.out) {
													OperatorPosition f = new OperatorPosition(c_start, c_end, code);
													if(debug) System.out.println(c_start + "_( " + c1 + "->" + c2 + ") _" + c_end + " : " + f);
													//add to arrays
													BiNode<OperatorPosition> node = lines_per_dir[lines_dirs_index].add(player, dir_index, f);				//add to array for alignments in row/col/diag
													if(debug) System.out.println("line sizes: " + lines_dirs_index + ", " + dir_index + " : " +  lines_per_dir[lines_dirs_index].get(dir_index).isEmpty(player));
													//add reference for all in the middle
													MovePair c_it = new MovePair(c_start);
													boolean ended = false;
													while(!ended) {
														cells_lines[c_it.i()][c_it.j()].add(player, node);								//add to cell's alignments
														if(c_it.equals(c_end)) ended = true;
														else c_it.sum(dir);
													}
													if(debug) System.out.println("cell empty: " + c_it + " : " + cells_lines[c_it.i()][c_it.j()].isEmpty(player));
													//update before, after
													c_end.sum(dir);
													if(!c_end.inBounds(MIN, MAX) || B[c_end.i()][c_end.j()] != MNKCellState.FREE) break;
													else {
														after++;
														before--;
														c_start.sum(dir);
													}
												}
											}
										}
									}
								}
							}	//end if (c2==player)
							//increment c1/c2
							next = c2.getSum(dir);
							if(!next.inBounds(MIN, MAX) || line >= MAX_LINE || cellState(c2) == opponent) {
								if(!next.inBounds(MIN, MAX)) checked_all = true;
								else {
									c1.sum(dir);
									c2.reset(c1);
									line = mark = in = 0;
									found_first = found_second = false;
								}
							}
							else c2.reset(next);
						}	//end if (!checked_all)
					}	//end while
				}
			}
			public void addAllAlignments_t(MNKCellState player, int lines_dirs_index, int max_tier) {
				boolean debug = false;
				
				MNKCellState opponent = Auxiliary.opponent(player);
				MovePair dir = DIRECTIONS[lines_dirs[lines_dirs_index]];
				int MAX_LINE = K - Operators.MAX_LINE;
				MovePair start, c1, c2;
				//c1,c2 = iterators (first and last in line to check)
				//c1 goes from center-MAX_LEN to center, c2 from c1 to center+MAX_LEN
				start = iterateLineDirs(null, lines_dirs_index);
				for(int i = 0; i < lines_per_dir[lines_dirs_index].size(); i++, start = iterateLineDirs(start, lines_dirs_index))
				{
					c1 = new MovePair(start);
					int dir_index = lineIndex(dir, c1);							//if horizontal: row index, otherwise (all other cases) col index
					int line = 0, mark = 0, in = 0, before = 0, after = 0;
					//count from center to c1
					c2 = new MovePair(c1);

					if(debug) System.out.println("\t\t\tdir: " + dir);
					MovePair next;
					boolean checked_all = false;
					while(!checked_all) {
						if(debug) System.out.println("\t\t\t" + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);

						//while (c1 == empty): c1++; c2=c1;
						if(cellState(c1) == MNKCellState.FREE) {
							if(debug) System.out.println("\t\t\t\tc1!=player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							while(c1.inBounds(MIN, MAX) && cellState(c1) != player)
								c1.sum(dir);
							if(!c1.inBounds(MIN, MAX)) checked_all = true;
							else c2.reset(c1);
						}
						if(!checked_all) {
							//while (c2 == empty && !(c2 reached end_c2) ) line++, in++, c2++;		//impossible at first iteration, when c2=c1, because of the lines above
							while(cellState(c2) == MNKCellState.FREE && (next = c2.getSum(dir)).inBounds(MIN, MAX)) {
								//doesn't update line,in when c2==end_c2; however not needed, since in that case it would not check for alignments
								line++;
								in++;
								c2.reset(next);;
							}
							if(debug) System.out.println("\t\t\t\tc2 empty: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
							//if ( !(line exceeded MAX) && c2 == player): line++, mark++; check alignment;
							if(line <= MAX_LINE && cellState(c2) == player) {
								line++;
								mark++;
								if(debug) System.out.println("\t\t\t\tc2 player: " + c1 + "->" + c2 + " : " + line + ", " + mark + ", " + in);
								//check alignments
								if(mark >= K-Operators.MARK_DIFF_MIN) {
									int tier = K - mark;
									if(tier <= max_tier)
									{
										//foreach alignment of mark marks
										for(byte code : Operators.ALIGNMENT_CODES[tier]) {
											Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
											if(debug) System.out.println("\t\t\t\t\tal = " + al);
											//if (inner alignment conditions)
											if(line <= K - al.line && in == al.in) {
												//assuming that win is K marks aligned, without free cells, checks wether the game ended
												if(tier == 0) {
													gameState = Auxiliary.cellState2winState(player);
													return;
												}
												//check outer alignment conditions
												before = countMarks( c1.getSum(dir.getNegative()), dir.getNegative(), al.out - al.mnout, MNKCellState.FREE);
												if(before >= al.mnout)
													after = countMarks(c2.getSum(dir), dir, al.out - before, MNKCellState.FREE);
												if(debug) System.out.println("\t\t\t\t\tbefore, after = " + before + "," + after);
												//if (outer conditions)
												MovePair c_start = c1.getSum(dir.getProd(-before));
												MovePair c_end = c2.getSum(dir.getProd(after));
												while(before >= al.mnout && after >= al.mnout && before + after >= al.out) {
													OperatorPosition f = new OperatorPosition(c_start, c_end, code);
													if(debug) System.out.println(c_start + "_( " + c1 + "->" + c2 + ") _" + c_end + " : " + f);
													//add to arrays
													BiNode<OperatorPosition> node = lines_per_dir[lines_dirs_index].add(player, dir_index, f);				//add to array for alignments in row/col/diag
													if(debug) System.out.println("line sizes: " + lines_dirs_index + ", " + dir_index + " : " +  lines_per_dir[lines_dirs_index].get(dir_index).isEmpty(player));
													//add reference for all in the middle
													MovePair c_it = new MovePair(c_start);
													boolean ended = false;
													while(!ended) {
														cells_lines[c_it.i()][c_it.j()].add(player, node);								//add to cell's alignments
														if(c_it.equals(c_end)) ended = true;
														else c_it.sum(dir);
													}
													if(debug) System.out.println("cell empty: " + c_it + " : " + cells_lines[c_it.i()][c_it.j()].isEmpty(player));
													//update before, after
													c_end.sum(dir);
													if(!c_end.inBounds(MIN, MAX) || B[c_end.i()][c_end.j()] != MNKCellState.FREE) break;
													else {
														after++;
														before--;
														c_start.sum(dir);
													}
												}
											}
										}
									}
								}
							}	//end if (c2==player)
							//increment c1/c2
							next = c2.getSum(dir);
							if(!next.inBounds(MIN, MAX) || line >= MAX_LINE || cellState(c2) == opponent) {
								if(!next.inBounds(MIN, MAX)) checked_all = true;
								else {
									c1.sum(dir);
									c2.reset(c1);
									line = mark = in = 0;
								}
							}
							else c2.reset(next);
						}	//end if (!checked_all)
					}	//end while
				}
			}
			private int countMarks(MovePair start, MovePair incr, int max, MNKCellState mark) {
				int count = 0;
				while(count < max && start.inBounds(MIN, MAX) && B[start.i()][start.j()] == mark) {
					count++;
					start.sum(incr);
				}
				return count;
			}

			private void removeAlignmentsInvolvingCell(MovePair from, MovePair involved, MNKCellState player) {
				MovePair involved_direction = from.getDirection(involved);
				BiNode<BiNode<OperatorPosition>> node, tmp;
				BiList_NodeOpPos list = cells_lines[from.i()][from.j()];
				node = list.getFirst(player);
				while(node != null) {
					//check if involved
					MovePair start = node.item.item.start, end = node.item.item.end;
					MovePair line_direction = start.getDirection(end);
					//remove
					tmp = node;
					node = node.next;
					if(dirsIndexes(line_direction) == dirsIndexes(involved_direction) && involved.inBounds_included(start, end))
						list.remove(player, tmp);
				}
			}

			private int lineIndex(MovePair dir, MovePair position) {
				if(dir.i() == 0) return position.i();							//horizontal
				else if(dir.j() == 0) return position.j();						//vertical
				else if(dir.i() == dir.j()) return dir.j() - dir.i() + M - 1;	//dright
				else return dir.i() + dir.j();									//dleft
			}
			private MovePair iterateLineDirs(MovePair start, int lines_dirs_index) {
				if(start == null) {
					if(lines_dirs_index == 2) start = new MovePair(M - 1, 0);	//dright
					else start = new MovePair(0, 0);
				} else {
					if(lines_dirs_index == 0) start.reset(start.i() + 1, start.j());
					else if(lines_dirs_index == 1) start.reset(start.i(), start.j() + 1);
					else if(lines_dirs_index == 2) {
						if(start.i() == 0) start.reset(start.i(), start.j() + 1);
						else start.reset(start.i() - 1, start.j());
					}
					else {
						if(start.j() == N - 1) start.reset(start.i() + 1, start.j());
						else start.reset(start.i(), start.j() + 1);
					}
				}
				return start;
			}
			/**
			 * 
			 * @param player
			 * @return true if there are valid alignments (calculated before, with proper max_tier)
			 */
			public boolean hasAlignments(MNKCellState player) {
				for(int i = 0; i < lines_per_dir.length; i++) {
					for(int j = 0; j < lines_per_dir[i].size(); j++) {
						BiList_OpPos t = lines_per_dir[i].get(j);
						if(t != null && !t.isEmpty(player)) return true;
					}
				}
				return false;
			}
			/**
			 * 
			 * @param athreat
			 * @param attacker
			 * @return true if the AppliedThreat is compatible with this board, and useful, i.e. adds at least one attacker's mark
			 */
			private boolean isUsefulThreat(AppliedThreat athreat, MNKCellState attacker) {
				MNKCellState state, state_athreat;
				boolean useful = false;
				for(int i = 0; i < athreat.threat.related.length; i++) {
					state = cellState(athreat.threat.related[i]);
					state_athreat = (i == athreat.atk) ? athreat.attacker : Auxiliary.opponent(athreat.attacker);
					if(state == MNKCellState.FREE) {					//compatible
						if(state_athreat == attacker) useful = true;	//useful
					} else if(state != state_athreat) return false;		//not compatible
				}
				return useful;
			}
		//#endregion ALIGNMENTS

		//returns index in lines_per_dir to the line related to this direction
		protected int dirsIndexes(MovePair dir) {
			if(dir.equals(DIRECTIONS[0]))		return 1;
			else if(dir.equals(DIRECTIONS[1]))	return 3;
			else if(dir.equals(DIRECTIONS[2]))	return 0;
			else if(dir.equals(DIRECTIONS[3]))	return 2;
			else if(dir.equals(DIRECTIONS[4]))	return 1;
			else if(dir.equals(DIRECTIONS[5]))	return 3;
			else if(dir.equals(DIRECTIONS[6]))	return 0;
			else /*if(dir==DIRECTIONS[7]) */	return 2;
		}

	//#endregion AUXILIARY

	//#region INIT
		private void initStructures() {
			B  = new MNKCellState[M][N];
			FC = new MNKCell[M*N]; 
			MC = new MNKCell[M*N];
			FC_indexes = new int[M][N];
		}
		public void reset() {
			currentPlayer = 0;
			initBoard();
			initFreeCells();
			initMarkedCells();
			markedThreats = new LinkedList<AppliedThreat>();
		}
		// Sets to free all board cells
		private void initBoard() {
			for(int i = 0; i < M; i++)
				for(int x = 0; x < N; x++) B[i][x] = MNKCellState.FREE;
		}
		// Rebuilds the free cells set 
		private void initFreeCells() {
			FC_n = 0;
			for(int y = 0; y < M; y++) {
				for(int x = 0; x < N; x++) {
					FC[FC_n] = new MNKCell(y, x);
					FC_indexes[y][x] = FC_n++;
				}
			}
		}
		// Resets the marked cells list
		private void initMarkedCells() {MC_n = 0;}
		
		private void initLinesStructures() {
			lines_rows = new AlignmentsList(M);
			lines_cols = new AlignmentsList(N);
			lines_dright = new AlignmentsList(M + N - 1);
			lines_dleft = new AlignmentsList(M + N - 1);
			lines_per_dir = new AlignmentsList[]{lines_rows, lines_cols, lines_dright, lines_dleft};
			cells_lines = new BiList_NodeOpPos[M][N];
			for(int i = 0; i < M; i++) {
				for(int j = 0; j < N; j++)
					cells_lines[i][j] = new BiList_NodeOpPos();
			}
		}
		//#region COPY
			public void copyArrays(DbBoard AB) {
				copyBoard(AB);
				copyFreeCells(AB);
				copyMarkedCells(AB);
				copyFCindexes(AB);
				markedThreats = new LinkedList<AppliedThreat>(AB.markedThreats);	//copy marked threats
			}
			private void copyBoard(DbBoard AB) {
				for(int i = 0; i < M; i++)
					for(int x = 0; x < N; x++) B[i][x] = AB.B[i][x];
			}
			private void copyFreeCells(DbBoard AB) {
				FC_n = AB.FC_n;
				for(int i = 0; i < FC_n; i++) FC[i] = copyCell(AB.FC[i]);
			}
			private void copyMarkedCells(DbBoard AB) {
				MC_n = AB.MC_n;
				for(int i = 0; i < MC_n; i++) MC[i] = copyCell(AB.MC[i]);
			}
			private void copyFCindexes(DbBoard AB) {
				for(int y = 0; y < M; y++) 
					for(int x = 0; x < N; x++) FC_indexes[y][x] = AB.FC_indexes[y][x];
			}
			// copies an MNKCell
			private MNKCell copyCell(MNKCell c) {
				return new MNKCell(c.i, c.j, c.state);
			}
			private void copyLinesStructures(DbBoard DB) {
				lines_rows = new AlignmentsList(DB.lines_rows);
				lines_cols = new AlignmentsList(DB.lines_cols);
				lines_dright = new AlignmentsList(DB.lines_dright);
				lines_dleft = new AlignmentsList(DB.lines_dleft);
				lines_per_dir = new AlignmentsList[]{lines_rows, lines_cols, lines_dright, lines_dleft};
				cells_lines = new BiList_NodeOpPos[M][N];
				for(int i = 0; i < M; i++) {
					for(int j = 0; j < N; j++)
						cells_lines[i][j] = new BiList_NodeOpPos();
				}
				for(int d = 0; d < lines_per_dir.length; d++) {
					AlignmentsList line = lines_per_dir[d];
					MovePair dir = DIRECTIONS[lines_dirs[d]];
					for(int i = 0; i < line.size(); i++) {
						if(line.get(i) != null) {
							copyLineInCells(line.getFirst(MNKCellState.P1, i), MNKCellState.P1, dir);
							copyLineInCells(line.getFirst(MNKCellState.P2, i), MNKCellState.P2, dir);
						}
					}
				}
			}
			private void copyLineInCells(BiNode<OperatorPosition> line_node, MNKCellState player, MovePair dir) {
				if(line_node != null) {
					copyLineInCells(line_node.next, player, dir);
					MovePair it = new MovePair(line_node.item.start), end = line_node.item.end;
					while(true) {
						cells_lines[it.i()][it.j()].add(player, line_node);
						if(!it.equals(end)) it.sum(dir);
						else break;
					}
				}
			}
		//#endregion COPY
	//#endregion INIT
	
}
