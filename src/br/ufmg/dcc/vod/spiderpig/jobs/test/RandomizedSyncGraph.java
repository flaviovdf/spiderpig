package br.ufmg.dcc.vod.spiderpig.jobs.test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

public class RandomizedSyncGraph implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_UID;
	
	private final int numVertex;
	private int[][] graph;
	
	public RandomizedSyncGraph(int numVertex) {
		this.numVertex = numVertex;
		this.graph = new int[numVertex][];
		
		Random r = new Random();
		for (int i = 0; i < numVertex; i++) {
			int numNeighbours = r.nextInt(numVertex) + 1;

			int[] neighbours = new int[numNeighbours];
			int next = (i + 1) % numVertex;
			neighbours[0] = next; //garante componente
			this.graph[i] = neighbours;
			
			for (int j = 1; j < numNeighbours; j++) {
				int neighbor = r.nextInt(numVertex);
				neighbours[j] = neighbor;
			}
		}
	}
	
	public synchronized int[] getNeighbours(int vertex) {
		return Arrays.copyOf(graph[vertex], graph[vertex].length);
	}
	
	public int getNumVertex() {
		return numVertex;
	}
}