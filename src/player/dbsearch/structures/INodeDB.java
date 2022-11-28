package player.dbsearch.structures;

import player.pnsearch.structures.INodes.MovePair;

public interface INodeDB<N extends INodeDB<N>> {

	public boolean equals(N node);
	
}
