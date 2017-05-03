/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.build;

/**
 *
 * TreeUtil - Calculate tree related computations
 *
 * @author Smith Tsang
 * @since 0.9
 */
public class TreeUtil {

	public static boolean isSingleCls(double [] clsDist){
		int count = 0;
		for(int i = 0; i < clsDist.length; i++){
			if(clsDist[i] > 0) count++;
			if(count > 1) return false;
		}
		return true;
	}

	public static int findMajorityCls(double [] clsDist){
		int max = 0;
		for(int i = 1 ; i < clsDist.length ; i++){
			if(clsDist[max] < clsDist[i])
				max = i;
		}
		return max;
	}

	public static double findError(double [] clsDist, int majorityCls){

		double count =0.0;
		for(int i = 0 ; i < clsDist.length ; i++)
			if(i != majorityCls)
				count += clsDist[i];

		return count;

	}



}
