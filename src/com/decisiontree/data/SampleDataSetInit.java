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
package com.decisiontree.data;

import com.decisiontree.measure.MemoryMonitor;
import com.decisiontree.param.GlobalParam;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.decisiontree.param.GlobalParam.POINT_FILE;

/**
 * SampleDataSetInit - Initializes a SampleDataSet object.
 *
 * @author Smith Tsang
 * @since 0.8
 */
public class SampleDataSetInit extends DataSetInit {

	private static final Logger log = Logger.getLogger(SampleDataSetInit.class);

	public SampleDataSetInit(String input, int noSamples) {
		this(input, noSamples, false);
	}

	public SampleDataSetInit(String input, int noSamples, boolean averaging) {
		dataSet = new SampleDataSet(input, findNoCls(input), findNoAttr(input), noSamples);
		dataSet.setClsNameList(findClsName(input));
		preProcess(input);
		dataSet.setNoTuples(countNoTuples(input));
		if (averaging) {
			storeData(input, averaging);
		} else storeData(input);
	}

	public SampleDataSetInit(String input, String name, int noSamples) {
		this(input, name, noSamples, false);
	}

	public SampleDataSetInit(String input, String name, int noSamples, boolean averaging) {
		dataSet = new SampleDataSet(input, findNoCls(name), findNoAttr(name), noSamples);
		dataSet.setClsNameList(findClsName(name));
		preProcess(name); // set attributes names and if they are continuous or not
		dataSet.setNoTuples(countNoTuples(input));
		if (averaging) {
			storeData(input, averaging);
		} else storeData(input);
	}


	@Override
	public void storeData(String input) {
		storeData(input, false);
	}

	public void storeData(String input, boolean averaging) {
		SampleDataSet dataSet = getDataSet();

		//Generate dataset tuples
		try (BufferedReader reader = new BufferedReader(new FileReader(input + POINT_FILE))){

			int noTuples = dataSet.getNoTuples();
			int noAttr = dataSet.getNoAttr();
			int noCls = dataSet.getNoCls();

			int countCls[] = new int[noCls];
			MemoryMonitor memMonitor = new MemoryMonitor();

			String data;
			List<Tuple> t = new ArrayList<>(noTuples);
			for (int i = 0; (data = reader.readLine()) != null && i < noTuples; i++) {
				System.out.printf("Generating tuple %d/%d...\n", i+1, noTuples);
				memMonitor.printMemoryUsage(i);
				int index = data.lastIndexOf(GlobalParam.SEPERATOR);
				int cls = dataSet.getClsNum(data.substring(index + 1));
				countCls[cls]++;
				t.add(new SampleTuple(data, noAttr, cls));
			}

			for (int i = 0; i < noCls; i++) {
				dataSet.setClsDistribution(i, countCls[i]);
			}

			dataSet.setData(t);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("No dataset file or file cannot access. Please try again!");
			System.exit(1);
		}
	}

//	public void storeData(String input, boolean averaging) {
//
//		SampleDataSet dataSet = getDataSet();
//
//		BufferedReader reader = null;
//		try {
//
//			int noTuples = dataSet.getNoTuples();
//
//			reader = new BufferedReader(new FileReader(input + SAMPLE_FILE));
//
//			String data = "";
//			List<Tuple> t = new ArrayList<Tuple>(noTuples);
//			for (int i = 0; (data = reader.readLine()) != null && i < noTuples; i++) {
//				int index = data.lastIndexOf(GlobalParam.SEPERATOR);
//				int cls = dataSet.getClsNum(data.substring(index + 1));
//				dataSet.setClsDistribution(cls);
//				t.add(new SampleTuple(data, dataSet.getNoAttr(), cls, i, dataSet, averaging));
//			}
//
//			dataSet.setData(t);
//		} catch (IOException e) {
//			e.printStackTrace();
//			log.error("No dataset file or file cannot access. Please try again!");
//			System.exit(1);
//		} finally {
//			try {
//				if (reader != null) reader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}


	@Override
	public int countNoTuples(String input) {
		return countNoTuples(input, SAMPLE_FILE);
	}

	@Override
	public void preProcess(String input) {
		preProcess(input, SAMPLE_FILE);
	}

	@Override
	public SampleDataSet getDataSet() {
		return (SampleDataSet) dataSet;
	}

}
