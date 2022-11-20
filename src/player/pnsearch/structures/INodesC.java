package player.pnsearch.structures;

import java.util.Collection;
import java.util.LinkedList;



public class INodesC extends INodes {


	/**
	 * A extends Collection
	 * @param <M> move type
	 * @param <S> self (same type)
	 * @param <A> collection (children)
	 */
	public abstract static class Node_c<M extends IMove, V, S extends Node_c<M,V,S,A>, A extends Collection<S>> extends Node_t<M,V,S,A> {

		public Node_c()													{super();}
		public Node_c(M move, S parent)						{super(move, parent);}
		//public Node_c(M move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}
		//protected void init(M move, Value value, short proof, short disproof, S parent);

		// FUNCTIONS
		@Override public short getChildren_sumProof() {
			short sum = 0;
			for(S child : children) {
				if(child.proof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.proof;
			}
			return sum;
		}
		@Override public short getChildren_sumDisproof() {
			short sum = 0;
			for(S child : children) {
				if(child.disproof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.disproof;
			}
			return sum;
		}
		@Override public S getChildren_minProof() {
			S min = getFirstChild();
			for(S child : children)
			if (child.proof < min.proof) min = child;
			return min;
		}
		@Override public S getChildren_minDisproof() {
			S min = getFirstChild();
			for(S child : children) 
			if(child.disproof < min.disproof) min = child;
			return min;
		}
		@Override public S findChild(M move) {
			S res = null;
			for(S child : children) {
				if(equalMoves(move, child.getMove())) {
					res = child;
					break;
				}
			}
			return res;
		}
		@Override public S findChildProof(short proof) {
			S res = null;
			for(S child : children) {
				if(child.proof == proof) {
					res = child;
					break;
				}
			}
			return res;
		}
		@Override public S findChildDisproof(short disproof) {
			S res = null;
			for(S child : children) {
				if(child.disproof == disproof) {
					res = child;
					break;
				}
			}
			return res;
		}
		// GET
		@Override public int getChildrenLength() {return children.size();}
		// SET
		@Override public void reduce() {
			S next = null;
			for(S child : children) {
				if(child.proof == proof && child.disproof == disproof) {
					next = child;
					break;
				}
			}
			evalValue();
			children.clear();
			children.add(next);
		}
	}

	/**
	 * expand, linkedlist
	 * @param <S> self
	 */
	public abstract static class Node_e<M extends IMove, V, S extends Node_e<M,V,S>> extends Node_c<M,V,S,LinkedList<S>> {
		protected boolean expanded;
		
		public Node_e()								{super();}
		public Node_e(M move, S parent)	{super(move, parent);}
		//public Node_e(M move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		@Override protected void init(M move, short proof, short disproof, S parent) {
			super.init(move, proof, disproof, parent);
			this.expanded = false;
		}
		
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// GET
		@Override public S getFirstChild() {return children.getFirst();}
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
		@Override public void reset(M move) {
			super.reset(move);
			expanded = false;
		}
		// INIT
		@Override protected void initChildren() {this.children = new LinkedList<S>();}
	}

	public abstract static class Node_ed<M extends IMove, V, S extends Node_ed<M,V,S>> extends Node_e<M,V,S> {
		public Node_ed()							{super();}
		public Node_ed(M move, S parent)	{super(move, parent);}
		//public Node_d(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		
		// FUNCTIONS
		//functions about children should be redefined to check whether children==null;
		//however some are only called if node is expanded
		@Override public S findChild(M move) {
			if(children == null) return null;
			else return super.findChild(move);
		}
		// SET
		@Override public void expand() {
			expanded = true;
			children = new LinkedList<S>();
		}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		// INIT
		@Override protected void initChildren() {this.children = null;}
	}

	// STORES THE MOST PROVING NODE
	public abstract static class Node_eds<M extends IMove, V, S extends Node_eds<M,V,S>> extends Node_ed<M,V,S> {
		public S most_proving;
		
		public Node_eds()							{super();}
		public Node_eds(M move, S parent)	{super(move, parent);}
		//public Node_eds(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		@Override protected void init(M move, short proof, short disproof, S parent) {
			super.init(move, proof, disproof, parent);
			most_proving = null;
		}

		// FUNCTIONS
		@Override public void reduce() {
			evalValue();
			children.clear();;
			children.add(most_proving);
		}
		// SET
		@Override public void reset(M move) {
			super.reset(move);
			most_proving = null;
		}
	}


}
