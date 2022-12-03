package player.dbsearch2;

import player.pnsearch.structures.INodes.MovePair;



public class OperatorPosition {
	public MovePair start;
	public MovePair end;
	public byte type;

	public OperatorPosition() {
		start = null;
		end = null;
		type = '\0';
	}
	public OperatorPosition(MovePair start, MovePair end, byte type) {
		this.start = new MovePair(start);
		this.end = new MovePair(end);
		this.type = type;
	}

	public int length() {
		return Math.max(Math.abs(start.i() -end.i()) , Math.abs(start.j() - end.j()));
	}
	public void set(MovePair start, MovePair end, byte type) {
		this.start = new MovePair(start);
		this.end = new MovePair(end);
		this.type = type;
	}

	@Override public String toString() {
		return start + "->" + end + " : " + type;
	}
}
