package player.pnsearch.structures;

import java.util.LinkedList;



public class NodesCE extends INodesC {
	
	// INSTANCE FOR PnSearch
	public static class NodeL extends Node_l<MovePair,Value,NodeL> {
		protected short i, j;
		protected boolean expanded;
		
		public NodeL()								{super();}
		public NodeL(MovePair move, NodeL parent)	{super(move, parent);}
		//public NodeL(MovePair move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}

		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children.addLast(new NodeL(move, this));}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void generateChildren() {this.children = new LinkedList<NodeL>();}

		// EXPANDED:
		@Override protected void init(MovePair move, short proof, short disproof, NodeL parent) {
			super.init(move, proof, disproof, parent);
			this.expanded = false;
		}
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// SET
		@Override public void expand() {expanded = true;}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override public void reset(MovePair move) {
			super.reset(move);
			expanded = false;
		}
	}
	
	// INSTANCE FOR PnSearchDelete
	public static class NodeLD extends Node_ld<MovePair,Value,NodeLD> {
		protected short i, j;
		protected boolean expanded;
		
		public NodeLD()										{super();}
		public NodeLD(MovePair move, NodeLD parent)			{super(move, parent);}
		//public NodeLD(MovePair move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}
		
		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children.addLast(new NodeLD(move, this));}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void generateChildren() {this.children = new LinkedList<NodeLD>();}

		// EXPANDED:
		@Override protected void init(MovePair move, short proof, short disproof, NodeLD parent) {
			super.init(move, proof, disproof, parent);
			this.expanded = false;
		}
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// SET
		@Override public void expand() {expanded = true;}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override public void reset(MovePair move) {
			super.reset(move);
			expanded = false;
		}
	}
	
	public static class NodeLDS extends Node_lds<MovePair,Value,NodeLDS> {
		protected short i, j;
		protected boolean expanded;

		public NodeLDS()								{super();}
		public NodeLDS(MovePair move, NodeLDS parent)	{super(move, parent);}
		//public NodeLDS(MovePair move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}

		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children.addLast(new NodeLDS(move, this));}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void generateChildren() {this.children = new LinkedList<NodeLDS>();}

		// EXPANDED:
		@Override protected void init(MovePair move, short proof, short disproof, NodeLDS parent) {
			super.init(move, proof, disproof, parent);
			this.expanded = false;
		}
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// SET
		@Override public void expand() {expanded = true;}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override public void reset(MovePair move) {
			super.reset(move);
			expanded = false;
		}
	}
	
}
