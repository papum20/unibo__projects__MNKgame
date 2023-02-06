package player.dbsearch_old.structures;



public interface INodeDB<N extends INodeDB<N>> {

	public void combine(N node);
	
	public boolean equals(N node);
	public boolean inConflict(N node);
	
	public void addChild(N node);
	
}
