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

import com.decisiontree.data.Tuple;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * SplitSearch (Interface) - finding the best split point for a set of data.
 *
 * @author Smith Tsang
 * @since 0.8
 */
public interface SplitSearch {

	static final Logger log = Logger.getLogger(SplitSearch.class);

	public static final String UDT = "UDT";
	public static final String UDTBP = "UDTBP";
	public static final String UDTGP = "UDTGP";
	public static final String UDTLP = "UDTLP";
	public static final String UDTES = "UDTES";

	public static final String AVG = "AVG";

	public static final String UDTUD = "UDTUD";
	public static final String AVGUD = "AVGUD";

	public static final String POINT = "POINT";

	/**
	 * Finding best attribute,split point pair with the given data tuples
	 *
	 * @param data   the given data tuples
	 * @param noCls  the number of classes
	 * @param noAttr the number of attribute
	 * @return the SplitData object storing the best attribute,split point pair
	 */
	public SplitData findBestAttr(List<Tuple> data, int noCls, int noAttr);

	public double findDispersion(double[] dist, double distSize);

//	public Dispersion getDispersion();
//
//	public void setDispersion(Dispersion dispersion);

//	Split getSplit();
//	void setBinarySplit(BinarySplit binarySplit);


}
