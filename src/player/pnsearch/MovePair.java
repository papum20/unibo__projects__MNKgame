package player.pnsearch;

import mnkgame.MNKCell;
import player.dbsearch.Auxiliary;



public class MovePair {
	protected short i, j;
	
	//#region CONSTRUCTORS
		public MovePair() {}
		/*
		public MovePair(short i, short j) {
			this.i = i;
			this.j = j;
		}
		*/
		public MovePair(int i, int j) {
			this.i = (short)i;
			this.j = (short)j;
		}
		public MovePair(MovePair move) {
			this.i = move.i;
			this.j = move.j;
		}
		public MovePair(MNKCell move) {
			this.i = (short)move.i;
			this.j = (short)move.j;
		}

		public void reset(MovePair move) {
			this.i = move.i;
			this.j = move.j;
		}
		public void reset(int i, int j) {
			this.i = (short)i;
			this.j = (short)j;
		}
	//#endregion CONSTRUCTORS

	public MovePair getPair() {return this;}
	public short i() {return i;}
	public short j() {return j;}
	public String toString() {return "[" + i + "," + j + "]";}

	//#region MATH_OPERATIONS
		public boolean equals(MovePair move) {return i == move.i() && j == move.j();}
		public void negate() {i = (short)(-i); j = (short)(-j);}
		public MovePair getNegative() {return new MovePair(-i, -j);}
		public void sum(MovePair B) {
			this.i += B.i;
			this.j += B.j;
		}
		public MovePair getSum(MovePair B) {
			return new MovePair(i + B.i, j + B.j);
		}
		public MovePair getSum(int i, int j) {
			return new MovePair(this.i + i, this.j + j);
		}
		public MovePair getDiff(MovePair B) {
			return new MovePair(i - B.i, j - B.j);
		}
		public MovePair getProd(int t) {
			return new MovePair(i * t, j * t);
		}
	//#endregion MATH_OPERATIONS

	//#region BOUNDS
		public boolean inBounds(MovePair min, MovePair max) {
			return i >= min.i && i < max.i && j >= min.j && j < max.j;
		}
		public boolean inBounds_included(MovePair min, MovePair max) {
			return i >= min.i && i <= max.i && j >= min.j && j <= max.j;
		}
		public boolean inBetween_included(MovePair first, MovePair second) {
			short imin, imax, jmin, jmax;
			if(first.i < second.i) {
				imin = first.i;		imax = second.i;
			} else {
				imin = second.i;	imax = first.i;
			}
			if(first.j < second.j) {
				jmin = first.j;		jmax = second.j;
			} else {
				jmin = second.j;	jmax = first.j;
			}
			return i >= imin && i <= imax && j >= jmin && j <= jmax;
		}
		public void clampMin(MovePair min) {
			if(min.i > i) i = min.i;
			if(min.j > j) j = min.j;
		}
		public void clampMax(MovePair max) {
			if(i >= max.i) i = (short)(max.i - 1);
			if(j >= max.j) j = (short)(max.j - 1);
		}
		public void clamp(MovePair min, MovePair max) {
			clampMin(min);
			clampMax(max);
		}
		public void clamp_diag(MovePair min, MovePair max, MovePair dir) {
			short old_i = i, old_j = j;
			i = (short)Auxiliary.clamp(i + dir.i, min.i, max.i);
			j = (short)Auxiliary.clamp(j + dir.j, min.j, max.j);
			if(dir.i != 0 && dir.j != 0) {
				int diff_i = Math.abs(i - old_i), diff_j = Math.abs(j - old_j);
				if(diff_i < diff_j) {
					if(j < old_j) j = (short)(old_j - diff_i);
					else j = (short)(old_j + diff_i);
				}
				else if(diff_j < diff_i) {
					if(i < old_i) i = (short)(old_i - diff_j);
					else i = (short)(old_i + diff_j);
				}
			}
		}
		
		public MovePair getDirection(MovePair target) {
			int dir_i, dir_j;
			if(target.i == i)		dir_i = 0;
			else if(target.i > i)	dir_i = 1;
			else					dir_i = -1;
			if(target.j == j)		dir_j = 0;
			else if(target.j > j)	dir_j = 1;
			else					dir_j = -1;
			return new MovePair(dir_i, dir_j);
		}
		//abs(difference), excluded
		public int getDistance(MovePair target) {
			return Math.max(Math.abs(i - target.i), Math.abs(j - target.j));
		}

	//#endregion BOUDS

}