package eval;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	public static class CutOffMeasure extends Measure {
		public final int cutoff;

		@Override
		public String toString() {
			return "Cutoff: "+cutoff;
		}

		public CutOffMeasure(int cutoff) {
			this.cutoff = cutoff;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			return correct >= cutoff;
		}
	}

	public static class All extends CutOffMeasure {
		public All() {
			super(0);
		}
	}

	public static class Ratio extends Measure {
		public final double ratio;

		@Override
		public String toString() {
			return "Ratio: "+ratio;
		}
		public Ratio(double r) {
			ratio = r;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			return correct / (correct + wrong) >= ratio;
		}
	}

	public static class Wilson extends Measure {
		public final double threshold;

		@Override
		public String toString() {
			return "Wilson: "+threshold;
		}
		public Wilson(double r) {
			threshold = r;
		}

		@Override
		public boolean measure(int total, int correct, int wrong) {
			double wilson[] = wilson(total, correct);
			return (wilson[0] - wilson[1] > threshold);
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
		args=new String[]{"c:/fabian/dropbox/shared/multiyago/attributematches"};
		Measure[] measures = new Measure[] { new All(), new CutOffMeasure(1),
				new CutOffMeasure(2), new CutOffMeasure(3),
				new CutOffMeasure(4), new CutOffMeasure(5), new Wilson(0.1) };
		for (String lan : new String[] { "ar", "de", "fa" }) {
			D.p("\n",lan);
			Map<String, Map<String, Boolean>> gold = new HashMap<>();
			for (List<String> line : new TSVFile(new File(args[0],
					"goldAttributeMatches_" + lan + ".txt"))) {
				if (line.size() > 2 && !line.get(2).isEmpty()) {
					Map<String, Boolean> map = gold.get(line.get(0));
					if (map == null)
						gold.put(line.get(0), map = new HashMap<>());
					map.put(line.get(1), line.get(2).equals("1"));
				}
			}
			int[] yells = new int[measures.length];
			int[] correctYells = new int[measures.length];
			double goldYells = 0;
			String lastAttr="";
			String lastRel="";
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
						lastRel=target;
						lastWrong=wrong;
						lastTotal=total;
					}
					continue;
				}

				do {
				Map<String, Boolean> map = gold.get(lastAttr);
				if (map == null)
					break;
				Boolean val = map.get(target);
				if (val == null)
					break;
				if (val)
					goldYells++;
				for (int i = 0; i < measures.length; i++) {
					boolean m = measures[i].measure(total, correct, wrong);
					if (m && val)
						correctYells[i]++;
					if (m)
						yells[i]++;
				}
			}
			for (int i = 0; i < measures.length; i++) {
				D.p(measures[i]);
				double prec=correctYells[i] / (double) yells[i];
				double rec=correctYells[i] / goldYells;
				D.p(" Precision:", correctYells[i] ,"/", yells[i], "=", prec);
				D.p(" Recall:", correctYells[i] ,"/" ,goldYells, "=", rec);
				D.p(" F1:", 2*prec*rec/(prec+rec));
			}

		}
	}
}
