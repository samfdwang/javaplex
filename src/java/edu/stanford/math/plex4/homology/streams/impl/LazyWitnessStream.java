package edu.stanford.math.plex4.homology.streams.impl;

import edu.stanford.math.plex4.array_utility.DoubleArrayQuery;
import edu.stanford.math.plex4.graph.UndirectedWeightedListGraph;
import edu.stanford.math.plex4.math.metric.interfaces.SearchableFiniteMetricSpace;
import edu.stanford.math.plex4.math.metric.landmark.LandmarkSelector;
import edu.stanford.math.plex4.utility.ExceptionUtility;
import edu.stanford.math.plex4.utility.Infinity;

/**
 * This class implements the lazy witness complex described in the paper
 * "Topological estimation using witness complexes", by Vin de Silva and
 * Gunnar Carlsson. The details of the construction are described in this
 * paper. Note that a lazy witness complex is fully described by its 
 * 1-skeleton, therefore we simply derive from the MaximalStream class.
 * 
 * @author Andrew Tausz
 *
 * @param <T> the type of the underlying metric space
 */
public class LazyWitnessStream<T> extends MaximalStream {

	/**
	 * This is the metric space upon which the stream is built from.
	 */
	protected final SearchableFiniteMetricSpace<T> metricSpace;

	/**
	 * This is the selection of landmark points
	 */
	protected final LandmarkSelector<T> landmarkSelector;

	/**
	 * This is the nu value described in the paper. Note that we use the
	 * default value of 2.
	 */
	protected final int nu;

	/**
	 * This is the R value described. It has a default value of 0.
	 */
	protected final double R;

	/**
	 * Constructor which initializes the complex with a metric space.
	 * 
	 * @param metricSpace the metric space to use in the construction of the complex
	 * @param maxDistance the maximum allowable distance
	 * @param maxDimension the maximum dimension of the complex
	 */
	public LazyWitnessStream(SearchableFiniteMetricSpace<T> metricSpace, LandmarkSelector<T> landmarkSelector, int maxDimension, double maxDistance, int nu, double R, int numDivisions) {
		super(maxDimension, maxDistance, numDivisions);
		ExceptionUtility.verifyNonNull(metricSpace);
		ExceptionUtility.verifyNonNegative(nu);
		ExceptionUtility.verifyLessThan(nu, landmarkSelector.size());
		this.metricSpace = metricSpace;
		this.landmarkSelector = landmarkSelector;
		this.nu = nu;
		this.R = R;
	}

	public LazyWitnessStream(SearchableFiniteMetricSpace<T> metricSpace, LandmarkSelector<T> landmarkSelector, int maxDimension, double maxDistance, int numDivisions) {
		this(metricSpace, landmarkSelector, maxDimension, maxDistance, 2, 0, numDivisions);
	}

	@Override
	protected UndirectedWeightedListGraph constructEdges() {
		int N = this.metricSpace.size();
		int n = this.landmarkSelector.size();

		UndirectedWeightedListGraph graph = new UndirectedWeightedListGraph(n);

		/*
		 * Let N be the number of points in the metric space, and n the number of 
		 * landmark points. Let D be the n x N matrix of distances between the set
		 * of landmark points, and the set of all points in the metric space.
		 * 
		 * The definition of the 1-skeleton of the lazy witness complex is as follows:
		 * 
		 * - If nu = 0, then define m_i = 0, otherwise define m_i to be the nu-th smallest entry
		 * in the i-th column of D.
		 * - The edge [ab] belongs to W(D, R, nu) iff there exists as witness i in {1, ..., N} such 
		 * that max(D(a, i), D(b, i)) <= R + m_i
		 * 
		 */

		/**
		 * This is will hold the i-th column in the n x N distance matrix.
		 */
		double[] distanceMatrixColumn = new double[n];
		double[] m_i = new double[N];

		for (int i = 0; i < N; i++) {
			// form the i-th column of the distance matrix;
			for (int k = 0; k < n; k++) {
				distanceMatrixColumn[k] = this.metricSpace.distance(i, this.landmarkSelector.getLandmarkIndex(k));
			}	

			// get the minimum indices within the set of landmark points
			int[] minimumIndices = DoubleArrayQuery.getMinimumIndices(distanceMatrixColumn, Math.max(2, nu));


			// If nu = 0, then define m_i = 0, otherwise define m_i to be the nu-th smallest entry
			// in the i-th column of D.
			if (this.nu > 0) {
				m_i[i] = distanceMatrixColumn[minimumIndices[this.nu - 1]];
			}
		}
		
		for (int i_index = 0; i_index < this.landmarkSelector.size(); i_index++) {
			int i = this.landmarkSelector.getLandmarkIndex(i_index);
			for (int j_index = i_index + 1; j_index < this.landmarkSelector.size(); j_index++) {
				int j = this.landmarkSelector.getLandmarkIndex(j_index);
				double E_ij = Infinity.Double.getPositiveInfinity();
				double R_ij = 0;
				for (int k = 0; k < N; k++) {
					E_ij = Math.min(E_ij, Math.max(this.metricSpace.distance(i, k), this.metricSpace.distance(j, k)));
					R_ij = E_ij - m_i[k];
				}
				
				if (R_ij < 0) {
					R_ij = 0;
				}
				
				if (R_ij <= this.maxDistance) {
					graph.addEdge(i_index, j_index, R_ij);
				}
			}
		}
		
		/*
		// iterate through the columns
		for (int i = 0; i < N; i++) {
			// form the i-th column of the distance matrix;
			for (int k = 0; k < n; k++) {
				distanceMatrixColumn[k] = this.metricSpace.distance(i, this.landmarkSelector.getLandmarkIndex(k));
			}	

			// get the minimum indices within the set of landmark points
			int[] minimumIndices = DoubleArrayQuery.getMinimumIndices(distanceMatrixColumn, Math.max(2, nu));


			// If nu = 0, then define m_i = 0, otherwise define m_i to be the nu-th smallest entry
			// in the i-th column of D.
			if (this.nu > 0) {
				m_i = distanceMatrixColumn[minimumIndices[this.nu - 1]];
			}

			for (int b_index = 0; b_index < this.landmarkSelector.size(); b_index++) {
				int b = this.landmarkSelector.getLandmarkIndex(b_index);
				for (int a_index = 0; a_index < b_index; a_index++) {
					int a = this.landmarkSelector.getLandmarkIndex(a_index);
					double edge_weight = Math.max(distanceMatrixColumn[a_index], distanceMatrixColumn[b_index]) - m_i;
					if (edge_weight < 0) {
						edge_weight = 0;
					}
					if (edge_weight <= this.maxDistance) {
						//double distance = this.metricSpace.distance(a, b);
						if (graph.containsEdge(a_index, b_index) && (graph.getWeight(a_index, b_index) > edge_weight)) {
							graph.addEdge(a_index, b_index, edge_weight);
						} else if (!graph.containsEdge(a_index, b_index)) {
							graph.addEdge(a_index, b_index, edge_weight);
						}
					}
				}
			}

		}
		 */

		return graph;
	}

}