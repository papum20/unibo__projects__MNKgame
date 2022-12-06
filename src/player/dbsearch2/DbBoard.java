package player.dbsearch2;

import java.util.LinkedList;
import java.util.ListIterator;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import player.dbsearch2.BiList.BiNode;
import player.dbsearch2.DbSearch.Combined;
import player.pnsearch.structures.INodes.MovePair;



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
	protected final MovePair MIN;
	protected final MovePair MAX;

	protected final MNKCellState[][] B;	// board
	protected MNKCell[] MC; 			// Marked Cells
	protected MNKCell[] FC; 			// Free Cells
	protected int MC_n;					// marked cells number
	protected int FC_n;					// free cells number
	protected int[][] FC_indexes;		// cell y,x=index to element y,x in FC

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
	protected AlignmentsList[] lines_per_dir;	//for each direction 0-3, contains the reference to the proper lines array(list)
	protected int[] lines_dirs;					//indexes in DIRECTIONS, with same order as lines_per_dir
	protected BiList_NodeOpPos[][] cells_lines;

	protected final MNKCellState[] Player = {MNKCellState.P1, MNKCellState.P2};
	protected int currentPlayer;		// currentPlayer plays next move (= 0 or 1)
	protected MNKGameState gameState;
  

	

	public DbBoard(int M, int N, int K) {
		this.M  = M;
		this.N  = N;
		this.K  = K;
	 	this.gameState = MNKGameState.OPEN;
		MIN = new MovePair(0, 0);
		MAX = new MovePair(M, N);
		
		B  = new MNKCellState[M][N];
		FC = new MNKCell[M*N]; 
		MC = new MNKCell[M*N];
	  	FC_indexes = new int[M][N];

		initLinesStructures();
		reset();
	}
	public DbBoard(DbBoard board) {
		this.M  = board.M;
		this.N  = board.N;
		this.K  = board.K;
		this.gameState = board.gameState;
		MIN = new MovePair(0, 0);
		MAX = new MovePair(M, N);
		
		B  = new MNKCellState[M][N];
		FC = new MNKCell[M*N]; 
		MC = new MNKCell[M*N];
		FC_indexes = new int[M][N];
		
		initLinesStructures();
		currentPlayer = board.currentPlayer;
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
		public void markCell(int y, int x) {
			MovePair c = new MovePair(y, x);
			removeFC(y, x);
			B[y][x] = Player[currentPlayer];
			addMC(y, x, B[y][x]);
			currentPlayer = (currentPlayer + 1) % 2;
			//update gameState
			// UPDATE ALIGNMENTS
			// remove alignments for both players involving this cell
			removeAlignments(c, Auxiliary.opponent(B[y][x]));
			removeAlignments(c, B[y][x]);
			// add alignments for player
			for(int d = 0; d < lines_dirs.length; d++) addAlignments(c, B[y][x], d);
		}
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
		public void markCells(MNKCell[] c) {
			int i;
			for(i = 0; i < c.length; i++) {
				removeFC(c[i].i, c[i].j);
				addMC(c[i].i, c[i].j, c[i].state);
				B[c[i].i][c[i].j] = c[i].state;
			}
			//update gameState
			// UPDATE ALIGNMENTS
			// remove alignments for both players involving this cell
			for(i = 0; i < c.length; i++) {
				MovePair t = new MovePair(c[i]);
				removeAlignments(t, MNKCellState.P1);
				removeAlignments(t, MNKCellState.P2);
			}
			// add alignments for player
			MovePair t0 = new MovePair(c[0]);
			MovePair dir = t0.getDirection(new MovePair(c[1]));
			int dir_index = dirsIndexes(dir);
			for(int d = 0; d < lines_dirs.length; d++) {
				if(d != dir_index) {
					for(i = 0; i < c.length; i++) addAlignments(new MovePair(c[i]), c[i].state, d);
				} else {
					addAlignments_from(t0, MIN, c[0].state, d);
					for(i = 1; i < c.length; i++) addAlignments_from(new MovePair(c[i]), new MovePair(c[i-1]), c[i].state, d);
				}
			}
		}
		public void markCells(LinkedList<MNKCell> c, Combined combined) {
			ListIterator<MNKCell> it = c.listIterator();
			while(it.hasNext()) {
				MNKCell t = it.next();
				removeFC(t.i, t.j);
				addMC(t);
				B[t.i][t.j] = t.state;
			}
			//update gameState
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
					addAlignments_fromCombined(new MovePair(t), combined, t.state, d);
			}
		}
		/**
		 * Undoes last move
		 @PRECONDITION: MC.length > 0
		 */
		public void unmarkCell() {
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
		}
		public MNKCellState cellState(int y, int x) {return B[y][x];}
		public MNKCellState cellState(MNKCell c) {return B[c.i][c.j];}
		public MNKCellState cellState(MovePair c) {return B[c.i()][c.j()];}
		public MNKGameState gameState() {return gameState;}
		public int currentPlayer() {return currentPlayer;}


		public MNKCell getMarkedCell(int i) {return MC[i];}
		//public MNKCell getFreeCell(int i) {return FC[i];}
		//public int MarkedCells_length() {return MC_n;}
		//public int FreeCells_length() {reurn FC_n;}
		
	//#endregion BOARD




	//#region DB_SEARCH

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
			private void addFC(int y, int x) {
				Auxiliary.swap(FC, FC_indexes[y][x], FC_n);
				MNKCell last = FC[FC_n];
				FC_indexes[last.i][last.j] = FC_n;
				FC_n++;
			}
			private void removeMC() {MC_n--;}
			private void addMC(MNKCell cell) {MC[MC_n++] = cell;}
			private void addMC(int y, int x, MNKCellState player) {MC[MC_n++] = new MNKCell(y, x, player);}
		//#endregion FC_MC_ARRAYS

		//#region ALIGNMENTS
			private void removeAlignments(final MovePair center, MNKCellState player) {
				int MAX_ALIGNMENT = K - Operators.MAX_LINE + Operators.MAX_FREE_EXTRA_TOT;
				//foreach alignment that was stored in (y,x)
				BiNode<BiNode<OperatorPosition>> node = cells_lines[center.i()][center.j()].getFirst(player);
				if(node != null) {
					do {
						System.out.println("\t\trm: " + node.item.item);
						MovePair start = node.item.item.start, end = node.item.item.end;
						MovePair dir = start.getDirection(end);
						//delete for line
						System.out.println("\t\t" + lineIndex(dir, center));
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
			private void addAlignments(final MovePair center, MNKCellState player, int lines_dirs_index) {
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
								//foreach alignment of mark marks
								for(byte code : Operators.ALIGNMENTS_CODES[tier]) {
									Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
									System.out.println("\t\t\t\t\tal = " + al);
									//if (inner alignment conditions)
									if(line <= K - al.line && in == al.in) {
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
			// used for markCells : adds all alignments involving at least one cell between (y1,x1) and (y2,x2)
			private void addAlignments_from(final MovePair center, final MovePair from, MNKCellState player, int lines_dirs_index) {
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
								//foreach alignment of mark marks
								for(byte code : Operators.ALIGNMENTS_CODES[tier]) {
									Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
									System.out.println("\t\t\t\t\tal = " + al);
									//if (inner alignment conditions)
									if(line <= K - al.line && in == al.in) {
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
			private void addAlignments_fromCombined(final MovePair center, final Combined combined, MNKCellState player, int lines_dirs_index) {
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
				} while(combined.board[c_tmp.i()][c_tmp.j()] != combined.n && dist < MAX_LINE - 1 && c_tmp.inBounds(MIN, MAX) && cellState(c_tmp) != opponent);
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
								//foreach alignment of mark marks
								for(byte code : Operators.ALIGNMENTS_CODES[tier]) {
									Operators.Alignment al = Operators.ALIGNMENTS[tier].get((int)code);
									System.out.println("\t\t\t\t\tal = " + al);
									//if (inner alignment conditions)
									if(line <= K - al.line && in == al.in) {
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
			private int countMarks(MovePair start, MovePair incr, int max, MNKCellState mark) {
				int count = 0;
				while(count < max && start.inBounds(MIN, MAX) && B[start.i()][start.j()] == mark) {
					count++;
					start.sum(incr);
				}
				return count;
			}

			private void removeAlignmentsInvolvingCell(MovePair from, MovePair involved, MNKCellState player) {
				BiNode<BiNode<OperatorPosition>> node, tmp;
				BiList_NodeOpPos list = cells_lines[from.i()][from.j()];
				node = list.getFirst(player);
				while(node != null) {
					//check if involved
					MovePair start = node.item.item.start, end = node.item.item.end;
					MovePair line_direction = start.getDirection(end);
					MovePair involved_direction = from.getDirection(involved);
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
		public void reset() {
			currentPlayer = 0;
			initBoard();
			initFreeCells();
			initMarkedCells();
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
			lines_dirs = new int[]{2, 4, 3, 5};
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
		//#endregion COPY
	//#endregion INIT
	
}
