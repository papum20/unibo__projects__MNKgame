package player.dbsearch2;

import java.io.FileWriter;
import java.util.Scanner;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import player.dbsearch2.BiList.BiNode;
import player.dbsearch2.Operators.AlignmentsMap;
import player.pnsearch.structures.INodes.MovePair;



public class DbTest {


	public static void printBoard(DbBoard board) {
		for(int i = 0; i < board.M; i++) {
			String row = "";
			for(int j = 0; j < board.N; j++) 
				row += mnk2char(board.cellState(i, j));
			System.out.println(row);
		}
	}
	public static void printBoard(DbBoard board, FileWriter file) {
		try {
			for(int i = 0; i < board.M; i++) {
				String row = "";
				for(int j = 0; j < board.N; j++) 
					row += mnk2char(board.cellState(i, j));
					file.write(row + "\n");
			}
			file.write((int)(board.hash) + " " + (int)(board.hash >> 16) + "\n");
		} catch (Exception e) { }
	}
	public static void printBoard(DbBoard board, FileWriter file, int level) {
		try {
			for(int i = 0; i < board.M; i++) {
				String row = "";
				for(int j = 0; j < level; j++) row += "\t";
				for(int j = 0; j < board.N; j++) 
					row += mnk2char(board.cellState(i, j));
				file.write(row + "\n");
			}
			int dec = (int)(Math.pow(10, 8));
			file.write("hash:" + board.hash + "\n");
			file.write("key, rest of key: " + (int)(board.hash) + " " + (int)(board.hash >> 16) + "\n");
		} catch (Exception e) { }
	}
	
	public static char mnk2char(MNKCellState c) {
		if(c == MNKCellState.P1) return 'X';
		else if(c == MNKCellState.P2) return 'O';
		else return '.';
	}

	public static void debugBoard(DbBoard board, boolean print_mc, boolean print_fc, boolean print_cells) {
		System.out.println("\ndebugBoard() START:");
		
		// PRINT BOARD
		String sep_line  = "";
		for(int i = 0; i < 20; i++) sep_line += "-";
		System.out.println(sep_line);
		System.out.println("Turn " + board.MC_n);
		printBoard(board);

		// PRINT MC, FC
		System.out.println("\n");
		String mc_string = "", fc_string = "";
		if(print_mc) for(int i = 0; i < board.MC_n; i++) {
			mc_string += board.MC[i];
			System.out.println("MC: " + mc_string);
		}
		if(print_fc) for(int i = 0; i < board.FC_n; i++) {
			fc_string += board.FC[i];
			System.out.println("FC: " + fc_string);
		}

		// PRINT ALIGNMENTS
		if(print_cells) {
			System.out.println("Alignments per cell - P1:");
			for(int i = 0; i < board.M; i++) {
				for(int j = 0; j < board.N; j++) {
					BiNode<BiNode<OperatorPosition>> p = board.cells_lines[i][j].getFirst(MNKCellState.P1);
					if(p != null) {
						System.out.println("\tcell: " + new MovePair(i, j));
						do {
							System.out.println("\t\t" + p.item.item);
						} while((p = p.next) != null);
					}
				}
			}
			System.out.println("Alignments per cell - P2:");
			for(int i = 0; i < board.M; i++) {
				for(int j = 0; j < board.N; j++) {
					BiNode<BiNode<OperatorPosition>> p = board.cells_lines[i][j].getFirst(MNKCellState.P2);
					if(p != null) {
						System.out.println("\tcell: " + new MovePair(i, j));
						do {
							System.out.println("\t\t" + p.item.item);
						} while((p = p.next) != null);
					}
				}
			}
		}
		System.out.println("Alignments per line - P1:");
		for(int d = 0; d < DbBoard.lines_dirs.length; d++) {
			System.out.println("\tDIRECTION: " + DbBoard.DIRECTIONS[DbBoard.lines_dirs[d]]);
			for(int k = 0; k < board.lines_per_dir[d].size(); k++) {
				BiNode<OperatorPosition> p;
				try {
					p = board.lines_per_dir[d].getFirst(MNKCellState.P1, k);
				} catch (Exception e) {
					continue;
				}
				if(p != null) {
					System.out.println("\tline: " + k);
					do {
						System.out.println("\t\t" + p.item);
					} while((p = p.next) != null);
				}
			}
		}
		System.out.println("Alignments per line - P2:");
		for(int d = 0; d < DbBoard.lines_dirs.length; d++) {
			System.out.println("\tDIRECTION: " + DbBoard.DIRECTIONS[DbBoard.lines_dirs[d]]);
			for(int k = 0; k < board.lines_per_dir[d].size(); k++) {
				BiNode<OperatorPosition> p;
				try {
					p = board.lines_per_dir[d].getFirst(MNKCellState.P2, k);
				} catch (Exception e) {
					continue;
				}
				if(p != null) {
					System.out.println("\tline: " + k);
					do {
						System.out.println("\t\t" + p.item);
					} while((p = p.next) != null);
				}
			}
		}

		System.out.println("\n\n");
		
		System.out.println("END debugBoard();\n");
	}

	public static void debugBoard(DbBoard board, FileWriter file, boolean print_mc, boolean print_fc, boolean print_cells) {
		try {
			String sep_line  = "";
			for(int i = 0; i < 20; i++) sep_line += "-";
			file.write(sep_line + "\n");
			file.write("Turn " + board.MC_n + "\n");
			printBoard(board, file, 0);

			// PRINT MC, FC
			file.write("\n");
			String mc_string = "", fc_string = "";
			if(print_mc) for(int i = 0; i < board.MC_n; i++){
				mc_string += board.MC[i];
				file.write("MC: " + mc_string);
			}
			if(print_fc) for(int i = 0; i < board.FC_n; i++) {
				fc_string += board.FC[i];
				file.write("FC: " + fc_string);
			}
			
			// PRINT ALIGNMENTS
			if(print_cells) {
				file.write("Alignments per cell - P1:\n");
				for(int i = 0; i < board.M; i++) {
					for(int j = 0; j < board.N; j++) {
						BiNode<BiNode<OperatorPosition>> p = board.cells_lines[i][j].getFirst(MNKCellState.P1);
						if(p != null) {
							file.write("\tcell: " + new MovePair(i, j) + "\n");
							do {
								file.write("\t\t" + p.item.item + "\n");
							} while((p = p.next) != null);
						}
					}
				}
				file.write("Alignments per cell - P2:\n");
				for(int i = 0; i < board.M; i++) {
					for(int j = 0; j < board.N; j++) {
						BiNode<BiNode<OperatorPosition>> p = board.cells_lines[i][j].getFirst(MNKCellState.P2);
						if(p != null) {
							file.write("\tcell: " + new MovePair(i, j) + "\n");
							do {
								file.write("\t\t" + p.item.item + "\n");
							} while((p = p.next) != null);
						}
					}
				}
			}
			file.write("Alignments per line - P1:\n");
			for(int d = 0; d < DbBoard.lines_dirs.length; d++) {
				file.write("\tDIRECTION: " + DbBoard.DIRECTIONS[DbBoard.lines_dirs[d]] + "\n");
				for(int k = 0; k < board.lines_per_dir[d].size(); k++) {
					BiNode<OperatorPosition> p;
					try {
						p = board.lines_per_dir[d].getFirst(MNKCellState.P1, k);
					} catch (Exception e) {
						continue;
					}
					if(p != null) {
						file.write("\tline: " + k + "\n");
						do {
							file.write("\t\t" + p.item + "\n");
						} while((p = p.next) != null);
					}
				}
			}
			file.write("Alignments per line - P2:\n");
			for(int d = 0; d < DbBoard.lines_dirs.length; d++) {
				file.write("\tDIRECTION: " + DbBoard.DIRECTIONS[DbBoard.lines_dirs[d]] + "\n");
				for(int k = 0; k < board.lines_per_dir[d].size(); k++) {
					BiNode<OperatorPosition> p;
					try {
						p = board.lines_per_dir[d].getFirst(MNKCellState.P2, k);
					} catch (Exception e) {
						continue;
					}
					if(p != null) {
						file.write("\tline: " + k + "\n");
						do {
							file.write("\t\t" + p.item + "\n");
						} while((p = p.next) != null);
					}
				}
			}

			file.write("\n\n\n");

		} catch(Exception e) {}
	}
	
	
	
	public static void main(String[] args) {
		int M = Integer.parseInt(args[0]), N = Integer.parseInt(args[1]), K = Integer.parseInt(args[2]);
		DbSearch db = new DbSearch();
		db.initPlayer(M, N, K, true, 10);
		DbBoard board = new DbBoard(M, N, K);
		MNKCellState player = MNKCellState.P1;


		System.out.println("Alignments sizes:");
		for(AlignmentsMap al : Operators.ALIGNMENTS) System.out.println("\t" + al.size());

		debugBoard(board, false, false, false);
		
		Scanner scanner = new Scanner(System.in);
		while(true) {
			debugBoard(board, false, false, false);
			int i, j;
			System.out.println("i,j: ");
			i = Integer.parseInt(scanner.next());
			j = Integer.parseInt(scanner.next());
			MovePair move = new MovePair(i, j);
			board.markCell(move, player);
			board.updateAlignments(move, player);

			debugBoard(board, false, false, false);

			MNKCell[] FC = resizeArray(board.FC, board.FC_n), MC = resizeArray(board.MC, board.MC_n);
			MNKCell cell = db.selectCell(FC, MC);
			System.out.println(cell);

			if(board.gameState != MNKGameState.OPEN) break;
			else player = Auxiliary.opponent(player);
		}

		scanner.close();

	}

	private static MNKCell[] resizeArray(MNKCell[] vec, int size) {
		MNKCell[] res = new MNKCell[size];
		for(int i = 0; i < size; i++) res[i] = vec[i];
		return res;
	}

}
