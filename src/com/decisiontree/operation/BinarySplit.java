/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 * <p>
 * This file is part of UDT.
 * <p>
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.operation;

import com.decisiontree.eval.DispersionMeasure;
import com.decisiontree.eval.DispersionMeasureFactory;

/**
 * BinarySplit - Finds the binary split point of an attribute using the basic technique.
 *
 * @author Smith Tsang
 * @since 0.8
 */
public class BinarySplit implements Split {

	protected double noTuples;
	protected int noCls;
	protected double localOptimal;
	protected double threshold;
	protected DispersionMeasure dispersionMeasure;


	public BinarySplit(String dispersionStr) {
		this.dispersionMeasure = DispersionMeasureFactory.createDispersionMeasure(dispersionStr);
	}

	public BinarySplit(DispersionMeasure dispersionMeasure) {
		this.dispersionMeasure = dispersionMeasure;
	}

	public void init(double noTuples, int noCls) {
		this.noTuples = noTuples;
		this.noCls = noCls;
		this.dispersionMeasure.init(noTuples, noCls);
		this.threshold = Double.POSITIVE_INFINITY;
		this.localOptimal = Double.POSITIVE_INFINITY;


	}

	public void run(Histogram[] segments) {

		int noSegments = segments.length;
		double left[] = new double[noCls];
		double right[] = new double[noCls];
		// Starts with all tuples going to the right child
		for (int i = 0; i < noCls; i++) {
			left[i] = 0.0;
			for (int j = 0; j < noSegments; j++) {
				right[i] += segments[j].getCls(i);
			}
		}
		int min = -1; //sample index where the split (<= sample value) is the best
		double minEnt = Double.POSITIVE_INFINITY;

		// Here it experiments with each sample point as the split point
		// Slowly moving the sample bars (segments) from the right child to the left child
		// S0 --split with j=0-- S1 --split with j=1-- S2 ... --split with j=noSegments-2-- S[-1]
		// (j < noSegments - 1) instead of (j < noSegments) because the split (<= last sample) would
		// 		be everything, which would be discarded (same entropy as the current dataset without the split)
		for (int j = 0; j < noSegments - 1; j++) {
			for (int i = 0; i < noCls; i++) {
				left[i] += segments[j].getCls(i);
				right[i] -= segments[j].getCls(i);
			}

			double avgEnt = dispersionMeasure.averageDispersion(left, right);
			if (minEnt - avgEnt > 1E-12) {
				min = j;
				minEnt = avgEnt;
			}
		}

		threshold = minEnt;
		if (min != -1)
			localOptimal = segments[min].getValue(); //valor de separacao, valor do split, valor do sample em q foi feito o split

	}

	public double getEnt() {
		return threshold;
	}

	public double getSplit() {
		return localOptimal;
	}

	@Override
	public DispersionMeasure getDispersionMeasure() {
		// TODO Auto-generated method stub
		return dispersionMeasure;
	}


}
