package eval;

import java.io.*;
import java.util.*;

import javatools.administrative.D;
import javatools.filehandlers.TSVFile;

/**
 * This class is only for the evaluation of the Multilingual YAGO stuff
 *
 * @author Fabian
 *
 */
public class Evaluator {

	public abstract static class Measure {
		public abstract boolean measure(int total, int correct, int wrong);

		public double ratio;

		@Override
		public String toString() {
			return this.getClass().getSimpleName()+": "+ratio;
		}

		public String measureName() {
  		  return this.getClass().getSimpleName();
		}

	}

	public static class Support extends Measure {

		public Support(int cutoff) {
			this.ratio = cutoff;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			return correct >= ratio;
		}
	}

	public static class All extends Support {
		public All() {
			super(0);
		}
	}

	public static class Pca extends Measure {

		public Pca(double r) {
			ratio = r;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			return correct / (double) (correct + wrong) >= ratio;
		}
	}

	public static class Confidence extends Measure {

		public Confidence(double r) {
			ratio = r;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			return correct / (double) total >= ratio;
		}
	}

	public static class Wilson extends Measure {

		public Wilson(double r) {
			ratio = r;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			double wilson[] = wilson(total, correct);
			return (wilson[0] - wilson[1] > ratio);
		}
	}

	/**
	 * Computes the Wilson Interval (see
	 * http://en.wikipedia.org/wiki/Binomial_proportion_confidence_interval
	 * #Wilson_score_interval) Given the total number of events and the number
	 * of "correct" events, returns in a double-array in the first component the
	 * center of the Wilson interval and in the second component the width of
	 * the interval. alpha=95%.
	 */
	public static double[] wilson(int total, int correct) {
		double z = 1.96;
		double p = (double) correct / total;
		double center = (p + 1 / 2.0 / total * z * z)
				/ (1 + 1.0 / total * z * z);
		double d = z
				* Math.sqrt((p * (1 - p) + 1 / 4.0 / total * z * z) / total)
				/ (1 + 1.0 / total * z * z);
		return (new double[] { center, d });
	}

	public static void main(String[] args) throws Exception {
		args=new String[]{"/Users/suchanek/Dropbox/Shared/multiYAGO/AttributeMatches/"};
		List<Measure> measures = new ArrayList<Measure>();
		final int numSteps=11;
		for(double i=0;i<=1.0;i+=1.0/(numSteps-1)) {
		  measures.add(new Support((int)(i*30)));
 		  measures.add(new Confidence(i));
 		  measures.add(new Pca(i));
   		  measures.add(new Wilson(i));
		}
		for (String lan : new String[] { "fa","ar","de","fr","es","it","ro" }) {
			D.p("\n",lan);
			Map<String, Map<String, Boolean>> gold = new HashMap<>();
			for (List<String> line : new TSVFile(new File(args[0],
					"goldAttributeMatches_" + lan + ".txt"))) {
				if (line.size() > 2 && !line.get(2).isEmpty()) {
					Map<String, Boolean> map = gold.get(line.get(0));
					if (map == null)
						gold.put(line.get(0), map = new HashMap<>());
					map.put(line.get(1), !line.get(2).equals("0"));
				}
			}

			int[] yells = new int[measures.size()];
			int[] correctYells = new int[measures.size()];
			double goldYells = 0;
			int[] weightedyells = new int[measures.size()];
			int[] weightedcorrectYells = new int[measures.size()];
			double weightedgoldYells = 0;
			String lastAttr="";
			String lastTarget="";
			int lastCorrect=0;
			int lastWrong=0;
			int lastTotal=0;
			for (List<String> line : new TSVFile(new File(args[0],
					"_attributeMatches_" + lan + ".tsv"))) {
				String attr = line.get(0);
				String target = line.get(1);
				int total = Integer.parseInt(line.get(2));
				int correct = Integer.parseInt(line.get(3));
				int wrong = Integer.parseInt(line.get(4));

				if(attr.equals(lastAttr)) {
					if(correct>lastCorrect) {
						lastCorrect=correct;
						lastTarget=target;
						lastWrong=wrong;
						lastTotal=total;
					}
					continue;
				}

				do {
				Map<String, Boolean> map = gold.get(lastAttr);
				if (map == null)
					break;
				Boolean val = map.get(lastTarget);
				if (val == null)
					break;
				if (val) {
					goldYells++;
					weightedgoldYells+=lastTotal;
				}
				for (int i = 0; i < measures.size(); i++) {
					boolean m = measures.get(i).measure(lastTotal, lastCorrect, lastWrong);
					if (m && val) {
						correctYells[i]++;
						weightedcorrectYells[i]+=lastTotal;
					}
					if (m) {
					    //if(!val) D.p("Incorrect:",measures[i],lastAttr,lastTarget);
						yells[i]++;
						weightedyells[i]+=lastTotal;
					}
				}
				}while(false);
						lastCorrect=correct;
						lastTarget=target;
						lastWrong=wrong;
						lastTotal=total;
						lastAttr=attr;
			}
			int numMeasures=measures.size()/numSteps;
			for (int m = 0; m < numMeasures; m++) {
				Writer w=new FileWriter(new File(args[0],"plot_"+lan+"_"+measures.get(m).measureName()+".dat"));
				for(int i=0; i<numSteps;i++) {
				  double prec=weightedcorrectYells[m+i*numMeasures] / (double) weightedyells[m+i*numMeasures];
				  double rec=weightedcorrectYells[m+i*numMeasures] / weightedgoldYells;
				  w.write(prec+"\t"+rec+"\n");
				}
				w.close();
//				double prec=correctYells[i] / (double) yells[i];
//				double rec=correctYells[i] / goldYells;
//				D.p(" Precision:", correctYells[i] ,"/", yells[i], "=", prec);
//				D.p(" Recall:", correctYells[i] ,"/" ,goldYells, "=", rec);
//				D.p(" F1:", 2*prec*rec/(prec+rec));
//				if(prec>.95)
//				D.p(measures[i],"Prec:",prec,"Rec:",rec);
//				D.p(" Weighted Precision:", correctYells[i] ,"/", yells[i], "=", prec);
//				D.p(" Weighted Recall:", correctYells[i] ,"/" ,goldYells, "=", rec);
//				D.p(" Weighted F1:", 2*prec*rec/(prec+rec));
			}

		}
	}
}
