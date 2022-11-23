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

		public Node_c()							{super();}
		public Node_c(M move, S parent)			{super(move, parent);}
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
				if(move.equals(child.getMove())) {
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
		// BOOL
		//return true if node value is "unknown" and has children
		@Override public boolean isExpanded() {return children != null && children.size() > 0;}
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
	public abstract static class Node_l<M extends IMove, V, S extends Node_l<M,V,S>> extends Node_c<M,V,S,LinkedList<S>> {
		
		public Node_l()								{super();}
		public Node_l(M move, S parent)	{super(move, parent);}
		//public Node_e(M move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}

		// SET
		@Override public void expand() {}
		// GET
		@Override public S getFirstChild() {return children.getFirst();}
		// INIT
		@Override protected void initChildren() {this.children = new LinkedList<S>();}
	}

	public abstract static class Node_ld<M extends IMove, V, S extends Node_ld<M,V,S>> extends Node_l<M,V,S> {
		public Node_ld()					{super();}
		public Node_ld(M move, S parent)	{super(move, parent);}
		//public Node_d(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		
		// FUNCTIONS
		//functions about children should be redefined to check whether children==null;
		//however some are only called if node is expanded
		@Override public S findChild(M move) {
			if(children == null) return null;
			else return super.findChild(move);
		}
		// SET
		@Override public void expand() {children = new LinkedList<S>();}
		// INIT
		@Override protected void initChildren() {this.children = null;}
	}

	// STORES THE MOST PROVING NODE
	public abstract static class Node_lds<M extends IMove, V, S extends Node_lds<M,V,S>> extends Node_ld<M,V,S> {
		public S most_proving;
		
		public Node_lds()					{super();}
		public Node_lds(M move, S parent)	{super(move, parent);}
		//public Node_eds(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		@Override protected void init(M move, short proof, short disproof, S parent) {
			super.init(move, proof, disproof, parent);
			most_proving = null;
		}

		// FUNCTIONS
		@Override public void reduce() {
			evalValue();
			children.clear();
			children.add(most_proving);
		}
		// SET
		@Override public void reset(M move) {
			super.reset(move);
			most_proving = null;
		}
	}


}