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

import com.decisiontree.data.PointAttrClass;
import com.decisiontree.data.Sample;
import com.decisiontree.data.SampleAttribute;
import com.decisiontree.data.Tuple;
import com.decisiontree.param.GlobalParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * SplitSearchUnp - finding the best split point for a set of data without any
 * pruning technique.
 *
 * @author Smith Tsang
 * @since 0.8
 */
public class SplitSearchUnp extends AbstractSplitSearch {


	public SplitSearchUnp(String dispersionStr) {
		this(new BinarySplit(dispersionStr));
	}

	protected SplitSearchUnp(Split split) {
		super(split);
	}
//	public SplitSearchUnp(Dispersion dispersion){
//		this(new BinarySplit(dispersion));
//	}
//	
//	protected SplitSearchUnp(Dispersion dispersion, Split split){
//		super(dispersion);
//		setSplit(split);
//	}

	protected PointAttrClass[] generatePointAttrClass(List<Tuple> data, int attr) {
		// gera o peso de cada sample
		// o retorno eh um array do tipo [
		//   {
		//     sample (possui valor (no nosso caso, 0 ou 1) e cdist),
		//     classe,
		//     peso da sample = peso da tupla * (area da sample / peso de todas as samples)
		//   },
		//   ...
		// ]
		int noTuples = data.size();
		double curFrac = 0;
		ArrayList<PointAttrClass> attrClassList = new ArrayList<PointAttrClass>(
		  noTuples);
		for (int j = 0; j < noTuples; j++) {
			SampleAttribute p = (SampleAttribute) (data.get(j)
			  .getAttribute(attr));
			Sample samples[] = p.getSamples();
			curFrac = p.getCurFrac();
			// log.info(curFrac + " " + p.getStartPos() + " " + p.getEndPos());
			for (int a = p.getStartPos() + 1; a <= p.getEndPos(); a++) {
				// frac is the area of the bar of this sample in the histogram
				double frac = samples[a].getCDist();
				if (a != 0)
					frac -= samples[a - 1].getCDist();
				// as per the paper, w = w * sampleBarArea / totalLocalPDFArea
				// dividing by the totalLocalPDFArea, it normalizes the PDF
				// thus, w = w * normalizedSampleBarArea
				// In the end, it will have added noTuples * noSamples pointAttrClass objects
				attrClassList.add(new PointAttrClass(samples[a], data.get(j)
				  .getCls(), data.get(j).getWeight() * frac / curFrac));
			}
		}

		// log.info("nlist size: " + nlist.size());
		PointAttrClass attrClassSet[] = new PointAttrClass[attrClassList.size()];

		Iterator<PointAttrClass> iter = attrClassList.iterator();
		for (int k = 0; iter.hasNext(); k++)
			attrClassSet[k] = iter.next();
		// nlist.toArray(n);
//		log.info("n size: " + attrClassSet.length);

		return attrClassSet;
	}

	protected Histogram[] SegGen(List<Tuple> data, int noCls, int attr) {
		//SegGen calcula a distribuicao de pesos por classe, slide 8 (1.2, 1.5, 0.8, .0.5)
		//O problema aqui eh que o start do histograma esta sempre em 0
		//O end de um fica em 0 e do outro fica em 1


		// sabemos que ateh o attrClassSet calculou os pesos direito pro height = 0, attr = 0
		// int noCls = db.getNoCls();
		PointAttrClass[] attrClassSet = generatePointAttrClass(data, attr);
		if (attrClassSet.length == 0) {
			log.info("Bug");
			return null;
		}
		Arrays.sort(attrClassSet);
		// List n = new ArrayList();
		// Set n2 = new TreeSet();
		// n2.

		Histogram tempSegmentSet[] = new Histogram[attrClassSet.length];

		int count = 0;
		tempSegmentSet[0] = new Histogram(noCls);
		tempSegmentSet[0].setHist(attrClassSet[0].getValue(), attrClassSet[0]
		  .getCls(), attrClassSet[0].getWeight());
		for (int i = 1; i < attrClassSet.length; i++) {
			// occurs when there is more than a sample in the same point
			if (attrClassSet[i].getValue() == tempSegmentSet[count].getValue())
				tempSegmentSet[count].setHist(attrClassSet[i].getValue(),
				  attrClassSet[i].getCls(), attrClassSet[i].getWeight());
			else {
				tempSegmentSet[++count] = new Histogram(noCls);
				tempSegmentSet[count].setHist(attrClassSet[i].getValue(),
				  attrClassSet[i].getCls(), attrClassSet[i].getWeight());
			}
		}
		int noSegments = count + 1;
		Histogram segmentSet[] = new Histogram[noSegments];
		for (int i = 0; i < noSegments; i++) {
			segmentSet[i] = tempSegmentSet[i];
		}

		return segmentSet;
	}

	@Override
	public SplitData findBestAttr(List<Tuple> data, int noCls, int noAttr) {

		SplitData splitData = new SplitData();
		splitData.setDispersion(Double.POSITIVE_INFINITY);

		double totalTuples = Tuple.countWeightedTuples(data);

		getSplit().init(totalTuples, noCls);
//		BinarySplit binarySplit = new BinarySplit(dispersion, noCls);
		for (int i = 0; i < noAttr; i++) {
			Histogram segmentSet[] = SegGen(data, noCls, i);
			if (segmentSet == null)
				continue;
			int noSegments = segmentSet.length;
			// Param.noEntCal += noSegments;
			// Param.addNoEntCal(noSegments);
			GlobalParam.addNoEntOnSamples(noSegments);

			if (noSegments <= 1)
				continue;

			getSplit().run(segmentSet);
			double localEnt = getSplit().getEnt(); //entropia do split

			if (splitData.getDispersion() - localEnt > 1E-12) {
				splitData.setDispersion(localEnt);
				splitData.setSplitPt(getSplit().getSplit()); //valor dos samples (do segmento) onde eh feito o split
				splitData.setAttrNum(i);
			}
		}

		log.debug("Best Split: " + splitData.getAttrNum() + ", "
		  + splitData.getSplitPt() + ", " + splitData.getDispersion());

		return splitData;
	}

	protected BinarySplit getSplit() {
		return (BinarySplit) super.getSplit();
	}


}
