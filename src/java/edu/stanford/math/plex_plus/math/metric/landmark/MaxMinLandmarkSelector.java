package edu.stanford.math.plex_plus.math.metric.landmark;

import edu.stanford.math.plex_plus.math.metric.interfaces.SearchableFiniteMetricSpace;
import edu.stanford.math.plex_plus.utility.Infinity;
import edu.stanford.math.plex_plus.utility.RandomUtility;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

/**
 * This class implements sequential max-min landmark selection of points from a 
 * metric space. This method of landmark selection chooses points that are as
 * far spread out as possible, using the following inductive procedure:
 * Suppose that {l_0, ..., l_{i-1}} have been chosen as landmark points.
 * Define the function f(z) = min{d(z, l_0), ...., d(z, l_{i-1}} and define 
 * l_i to be l_i = arg max f(z). We start with l_0 being chosen randomly.
 * 
 * @author Andrew Tausz
 *
 * @param <T> the type of the underlying metric space
 */
public class MaxMinLandmarkSelector<T> extends LandmarkSelector<T> {

	/**
	 * This constructor initializes the landmark selector with a finite metric space,
	 * and a size parameter.
	 * 
	 * @param metricSpace the metric space to build the landmarks set in
	 * @param landmarkSetSize the size of the landmark set
	 */
	public MaxMinLandmarkSelector(SearchableFiniteMetricSpace<T> metricSpace, int landmarkSetSize) {
		super(metricSpace, landmarkSetSize);
	}

	@Override
	protected int[] computeLandmarkSet() {
		int[] landmarkIndices = new int[this.landmarkSetSize];

		int metricSpaceSize = this.metricSpace.size();

		TIntHashSet landmarkSet = new TIntHashSet();

		// select first point randomly
		landmarkSet.add(RandomUtility.nextUniformInt(0, metricSpaceSize - 1));
		landmarkIndices[0] = RandomUtility.nextUniformInt(0, metricSpaceSize - 1);

		/*
		 * Construct the landmark set inductively. Suppose that
		 * {l_0, ..., l_{i-1}} have been chosen as landmark points.
		 * Define the function 
		 * f(z) = min{d(z, l_0), ...., d(z, l_{i-1}}
		 * and define l_i to be l_i = arg max f(z)
		 * 
		 */
		for (int i = 1; i < this.landmarkSetSize; i++) {
			double max_f_value = Infinity.Double.getNegativeInfinity();
			int arg_max_f = 0;
			for (int z_index = 0; z_index < metricSpaceSize; z_index++) {
				if (!landmarkSet.contains(z_index)) {
					double f_value = this.computeMinDistance(z_index, landmarkSet);
					if (f_value > max_f_value) {
						max_f_value = f_value;
						arg_max_f = z_index;
					}
				}
			}

			landmarkSet.add(arg_max_f);
			landmarkIndices[i] = arg_max_f;
		}

		return landmarkIndices;
	}

	/**
	 * This function computes the function f(z) = min{d(z, l_0), ...., d(z, l_{i-1}}.
	 * 
	 * @param queryPointIndex the index of the argument z in the above expression
	 * @param existingLandmarkPoints the set {l_0, ..., l_{i-1}}
	 * @return the value min{d(z, l_0), ...., d(z, l_{i-1}}
	 */
	private double computeMinDistance(int queryPointIndex, TIntHashSet existingLandmarkPoints) {
		double minDistance = Infinity.Double.getPositiveInfinity();

		for (TIntIterator iterator = existingLandmarkPoints.iterator(); iterator.hasNext(); ) {
			minDistance = Math.min(minDistance, this.metricSpace.distance(queryPointIndex, iterator.next()));
		}

		return minDistance;
	}

}