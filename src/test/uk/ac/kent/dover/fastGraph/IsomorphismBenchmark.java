package test.uk.ac.kent.dover.fastGraph;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import uk.ac.kent.dover.fastGraph.ExactIsomorphism;
import uk.ac.kent.dover.fastGraph.FastGraph;
import uk.ac.kent.dover.fastGraph.isomorphism.FastGraphIsomorphism;
import uk.ac.kent.dover.fastGraph.isomorphism.Isomorphism;
import java.util.concurrent.TimeUnit;

public class IsomorphismBenchmark {
	@Test public void
	launchBenchmark() throws RunnerException {
		Options options = new OptionsBuilder()
				.include(this.getClass().getSimpleName())
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.MICROSECONDS)
				.warmupTime(TimeValue.seconds(1))
				.warmupIterations(10)
				.measurementTime(TimeValue.seconds(1))
				.measurementIterations(10)
				.timeout(TimeValue.seconds(240))
				.threads(1)
				.forks(10)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				.jvmArgs("-server")
				.build();
		new Runner(options).run();
	}

	@State(Scope.Benchmark)
	public static class GraphContainer {
		private static final int NODES = 0;
		private static final int EDGES = 1;
		private static final int SEED = 2;

		int testGraphs[][] = {
			/**
			 * Small graphs
			 */
			{  5,  20,   0},
			{ 10,  40,   1},
			{ 15,  60,   2},
			{ 20,  80,   3},
			{ 25, 100,   4},
			{ 30, 120,   5},
			{ 35, 140,   6},
			{ 40, 160,   7},
			{ 45, 180,   8},
			{ 50, 200,   9},
			{ 55, 220,  10},
			{ 60, 240,  11},
			{ 65, 260,  12},
			{ 70, 280,  13},
			{ 75, 300,  14},
			{ 80, 320,  15},
			{ 85, 340,  16},
			{ 90, 360,  17},
			{ 95, 380,  18},
			{100, 400,  19},

			/**
			 * Small graphs 2
			*/
			{  5,  20,   20},
			{ 10,  40,   21},
			{ 15,  60,   22},
			{ 20,  80,   23},
			{ 25, 100,   24},
			{ 30, 120,   25},
			{ 35, 140,   26},
			{ 40, 160,   27},
			{ 45, 180,   28},
			{ 50, 200,   29},
			{ 55, 220,  210},
			{ 60, 240,  211},
			{ 65, 260,  212},
			{ 70, 280,  213},
			{ 75, 300,  214},
			{ 80, 320,  215},
			{ 85, 340,  216},
			{ 90, 360,  217},
			{ 95, 380,  218},
			{100, 400,  219},

			/**
			 * Small graphs 3
			*/
			{  5,  20,   30},
			{ 10,  40,   31},
			{ 15,  60,   32},
			{ 20,  80,   33},
			{ 25, 100,   34},
			{ 30, 120,   35},
			{ 35, 140,   36},
			{ 40, 160,   37},
			{ 45, 180,   38},
			{ 50, 200,   39},
			{ 55, 220,  310},
			{ 60, 240,  311},
			{ 65, 260,  312},
			{ 70, 280,  313},
			{ 75, 300,  314},
			{ 80, 320,  315},
			{ 85, 340,  316},
			{ 90, 360,  317},
			{ 95, 380,  318},
			{100, 400,  319},

			/**
			 * Small graphs x5
			 * nbr of Edges = nbr of Nodes x 5
			*/
			{  5,  25,   0},
			{ 10,  50,   1},
			{ 15,  75,   2},
			{ 20, 100,   3},
			{ 25, 125,   4},
			{ 30, 150,   5},
			{ 35, 175,   6},
			{ 40, 200,   7},
			{ 45, 125,   8},
			{ 50, 250,   9},
			{ 55, 275,  10},
			{ 60, 300,  11},
			{ 65, 225,  12},
			{ 70, 250,  13},
			{ 75, 375,  14},
			{ 80, 400,  15},
			{ 85, 325,  16},
			{ 90, 350,  17},
			{ 95, 375,  18},
			{100, 500,  19},

			/**
			 * Small graphs x6
			 * nbr of Edges = nbr of Nodes x 6
			*/
			{  5,  30,   0},
			{ 10,  60,   1},
			{ 15,  90,   2},
			{ 20, 120,   3},
			{ 25, 150,   4},
			{ 30, 180,   5},
			{ 35, 210,   6},
			{ 40, 240,   7},
			{ 45, 270,   8},
			{ 50, 300,   9},
			{ 55, 330,  10},
			{ 60, 360,  11},
			{ 65, 390,  12},
			{ 70, 420,  13},
			{ 75, 450,  14},
			{ 80, 480,  15},
			{ 85, 510,  16},
			{ 90, 540,  17},
			{ 95, 570,  18},
			{100, 600,  19},

			/**
			 * Medium graphs
			*/
			{ 25,  100,  10},
			{ 50,  200,  10},
			{ 75,  300,  11},
			{100,  400,  11},
			{125,  500,  12},
			{150,  600,  12},
			{175,  700,  13},
			{200,  800, 131},
			{225,  900,  14},
			{250, 1000,  14},
			{275, 1100,  15},
			{300, 1200, 152},
			{325, 1300,  16},
			{350, 1400, 161},
			{375, 1500,  17},
			{400, 1600, 171},
			{425, 1700,  18},
			{450, 1800,  18},
			{475, 1900,  19},
			{500, 2000, 197},

			/**
			 * Large graphs
			*/
			{ 250,  1000, 201},
			{ 500,  2000,  20},
			{ 750,  3000,  21},
			{1000,  4000,  21},
			{1250,  5000,  22},
			{1500,  6000,  22},
			{1750,  7000,  23},
			{2000,  8000, 232},
			{2250,  9000, 232},
			{2500, 10000, 243},
			{2750, 11000, 251},
			{3000, 12000, 255},
			{3250, 13000, 268},
			{3500, 14000, 264},
			{3750, 15000, 274},
			{4000, 16000, 274},
			{4250, 17000, 282},
			{4500, 18000, 289},
			{4750, 19000,  29},
			{5000, 20000, 298},
		};

		@Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19"})
		int graphID;

		// Small 1
		private FastGraph smallGraph1, smallGraph2;
		private ExactIsomorphism smallEi;

		private FastGraphIsomorphism smallGraphIso1, smallGraphIso2;
		private Isomorphism smallIso;

		// Small 2
		private FastGraph smallGraph21, smallGraph22;
		private ExactIsomorphism smallEi2;

		private FastGraphIsomorphism smallGraphIso21, smallGraphIso22;
		private Isomorphism smallIso2;

		// Small 3
		private FastGraph smallGraph31, smallGraph32;
		private ExactIsomorphism smallEi3;

		private FastGraphIsomorphism smallGraphIso31, smallGraphIso32;
		private Isomorphism smallIso3;

		// Small x5
		private FastGraph smallGraphX51, smallGraphX52;
		private ExactIsomorphism smallEiX5;

		private FastGraphIsomorphism smallGraphIsoX51, smallGraphIsoX52;
		private Isomorphism smallIsoX5;

		// Small x6
		private FastGraph smallGraphX61, smallGraphX62;
		private ExactIsomorphism smallEiX6;

		private FastGraphIsomorphism smallGraphIsoX61, smallGraphIsoX62;
		private Isomorphism smallIsoX6;

		// Medium
		private FastGraphIsomorphism mediumGraphIso1, mediumGraphIso2;
		private Isomorphism mediumIso;

		// Large
		private FastGraphIsomorphism largeGraphIso1, largeGraphIso2;
		private Isomorphism largeIso;

		@Setup(Level.Trial)
		public void init() {
			FastGraph mediumGraph1, mediumGraph2, largeGraph1, largeGraph2;

			try {
				int id = graphID;
				System.out.print("Generate small graphs " + id + " (1)…");
				smallGraph1 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				smallGraph2 = ExactIsomorphism.generateRandomIsomorphicGraph(smallGraph1, testGraphs[id][SEED] * 2, false);
				smallGraphIso1 = new FastGraphIsomorphism(smallGraph1);
				smallGraphIso2 = new FastGraphIsomorphism(smallGraph2);

				smallEi = new ExactIsomorphism(smallGraph1);
				smallIso = new Isomorphism(smallGraphIso1);
				System.out.println(" Generated!");

				id += 20;
				System.out.print("Generate small graphs " + id + " (2)…");
				smallGraph21 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				smallGraph22 = ExactIsomorphism.generateRandomIsomorphicGraph(smallGraph21, testGraphs[id][SEED] * 2, false);
				smallGraphIso21 = new FastGraphIsomorphism(smallGraph21);
				smallGraphIso22 = new FastGraphIsomorphism(smallGraph22);

				smallEi2 = new ExactIsomorphism(smallGraph21);
				smallIso2 = new Isomorphism(smallGraphIso21);
				System.out.println(" Generated!");

				id += 20;
				System.out.print("Generate small graphs " + id + " (3)…");
				smallGraph31 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				smallGraph32 = ExactIsomorphism.generateRandomIsomorphicGraph(smallGraph31, testGraphs[id][SEED] * 2, false);
				smallGraphIso31 = new FastGraphIsomorphism(smallGraph31);
				smallGraphIso32 = new FastGraphIsomorphism(smallGraph32);

				smallEi3 = new ExactIsomorphism(smallGraph31);
				smallIso3 = new Isomorphism(smallGraphIso31);
				System.out.println(" Generated!");

				id += 20;
				System.out.print("Generate graphs x 5 " + id + "…");
				smallGraphX51 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				smallGraphX52 = ExactIsomorphism.generateRandomIsomorphicGraph(smallGraphX51, testGraphs[id][SEED] * 2, false);
				smallGraphIsoX51 = new FastGraphIsomorphism(smallGraphX51);
				smallGraphIsoX52 = new FastGraphIsomorphism(smallGraphX52);

				smallEiX5 = new ExactIsomorphism(smallGraphX51);
				smallIsoX5 = new Isomorphism(smallGraphIsoX51);
				System.out.println(" Generated!");

				id += 20;
				System.out.print("Generate graphs x 6 " + id + "…");
				smallGraphX61 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				smallGraphX62 = ExactIsomorphism.generateRandomIsomorphicGraph(smallGraphX61, testGraphs[id][SEED] * 2, false);
				smallGraphIsoX61 = new FastGraphIsomorphism(smallGraphX61);
				smallGraphIsoX62 = new FastGraphIsomorphism(smallGraphX62);

				smallEiX6 = new ExactIsomorphism(smallGraphX61);
				smallIsoX6 = new Isomorphism(smallGraphIsoX61);
				System.out.println(" Generated!");

				id += 20;
				System.out.print("Generate medium graphs " + id + "…");
				mediumGraph1 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				mediumGraph2 = ExactIsomorphism.generateRandomIsomorphicGraph(mediumGraph1, testGraphs[id][SEED] * 2, false);
				mediumGraphIso1 = new FastGraphIsomorphism(mediumGraph1);
				mediumGraphIso2 = new FastGraphIsomorphism(mediumGraph2);

				mediumIso = new Isomorphism(mediumGraphIso1);
				System.out.println(" Generated!");

				id += 20;
				System.out.print("Generate large graphs " + id + "…");
				largeGraph1 = FastGraph.randomGraphFactory(testGraphs[id][NODES],
						testGraphs[id][EDGES], testGraphs[id][SEED], false, false);
				largeGraph2 = ExactIsomorphism.generateRandomIsomorphicGraph(largeGraph1, testGraphs[id][SEED] * 2, false);
				largeGraphIso1 = new FastGraphIsomorphism(largeGraph1);
				largeGraphIso2 = new FastGraphIsomorphism(largeGraph2);

				largeIso = new Isomorphism(largeGraphIso1);
				System.out.println(" Generated!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		FastGraph getFastGraph1() {
			return smallGraph1;
		}

		FastGraph getFastGraph2() {
			return smallGraph2;
		}

		FastGraph getFastGraph21() {
			return smallGraph21;
		}

		FastGraph getFastGraph22() {
			return smallGraph22;
		}

		FastGraph getFastGraph31() {
			return smallGraph31;
		}

		FastGraph getFastGraph32() {
			return smallGraph32;
		}

		FastGraph getFastGraphX51() {
			return smallGraphX51;
		}

		FastGraph getFastGraphX52() {
			return smallGraphX52;
		}

		FastGraph getFastGraphX61() {
			return smallGraphX61;
		}

		FastGraph getFastGraphX62() {
			return smallGraphX62;
		}

		ExactIsomorphism getExactIsomorphism() {
			return smallEi;
		}

		ExactIsomorphism getExactIsomorphism2() {
			return smallEi2;
		}

		ExactIsomorphism getExactIsomorphism3() {
			return smallEi3;
		}

		ExactIsomorphism getExactIsomorphismX5() {
			return smallEiX5;
		}

		ExactIsomorphism getExactIsomorphismX6() {
			return smallEiX6;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphism1() {
			return smallGraphIso1;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphism2() {
			return smallGraphIso2;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphism21() {
			return smallGraphIso21;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphism22() {
			return smallGraphIso22;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphism31() {
			return smallGraphIso31;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphism32() {
			return smallGraphIso32;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphismX51() {
			return smallGraphIsoX51;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphismX52() {
			return smallGraphIsoX52;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphismX61() {
			return smallGraphIsoX61;
		}

		FastGraphIsomorphism getSmallFastGraphIsomorphismX62() {
			return smallGraphIsoX62;
		}

		FastGraphIsomorphism getMediumFastGraphIsomorphism1() {
			return mediumGraphIso1;
		}

		FastGraphIsomorphism getMediumFastGraphIsomorphism2() {
			return mediumGraphIso2;
		}

		FastGraphIsomorphism getLargeFastGraphIsomorphism1() {
			return largeGraphIso1;
		}

		FastGraphIsomorphism getLargeFastGraphIsomorphism2() {
			return largeGraphIso2;
		}

		Isomorphism getSmallIsomorphism() {
			return smallIso;
		}

		Isomorphism getSmallIsomorphism2() {
			return smallIso2;
		}

		Isomorphism getSmallIsomorphism3() {
			return smallIso3;
		}

		Isomorphism getSmallIsomorphismX5() {
			return smallIsoX5;
		}

		Isomorphism getSmallIsomorphismX6() {
			return smallIsoX6;
		}

		Isomorphism getMediumIsomorphism() {
			return mediumIso;
		}

		Isomorphism getLargeIsomorphism() {
			return largeIso;
		}
	}

	/**
	 * ExactIsomorphism benchmarks
	 */
	@Benchmark
	public boolean exactIsomorphismWithSmallGraph(GraphContainer g) throws Exception {
		FastGraph g2 = g.getFastGraph2();
		ExactIsomorphism ei = g.getExactIsomorphism();
		return ei.isomorphic(g2);
	}

	@Benchmark
	public boolean exactIsomorphismWithSmallGraph2(GraphContainer g) throws Exception {
		FastGraph g2 = g.getFastGraph22();
		ExactIsomorphism ei = g.getExactIsomorphism2();
		return ei.isomorphic(g2);
	}

	@Benchmark
	public boolean exactIsomorphismWithSmallGraph3(GraphContainer g) throws Exception {
		FastGraph g2 = g.getFastGraph32();
		ExactIsomorphism ei = g.getExactIsomorphism3();
		return ei.isomorphic(g2);
	}

	@Benchmark
	public boolean exactIsomorphismWithSmallGraphX5(GraphContainer g) throws Exception {
		FastGraph g2 = g.getFastGraphX52();
		ExactIsomorphism ei = g.getExactIsomorphismX5();
		return ei.isomorphic(g2);
	}

	@Benchmark
	public boolean exactIsomorphismWithSmallGraphX6(GraphContainer g) throws Exception {
		FastGraph g2 = g.getFastGraphX62();
		ExactIsomorphism ei = g.getExactIsomorphismX6();
		return ei.isomorphic(g2);
	}

	/**
	 * Isomorphism benchmarks
	 */
	@Benchmark
	public boolean isomorphismWithSmallGraph(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getSmallFastGraphIsomorphism2();
		Isomorphism iso = g.getSmallIsomorphism();
		return iso.isomorphic(gi2);
	}

	@Benchmark
	public boolean isomorphismWithSmallGraph2(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getSmallFastGraphIsomorphism22();
		Isomorphism iso = g.getSmallIsomorphism2();
		return iso.isomorphic(gi2);
	}

	@Benchmark
	public boolean isomorphismWithSmallGraph3(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getSmallFastGraphIsomorphism32();
		Isomorphism iso = g.getSmallIsomorphism3();
		return iso.isomorphic(gi2);
	}

	@Benchmark
	public boolean isomorphismWithSmallGraphX5(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getSmallFastGraphIsomorphismX52();
		Isomorphism iso = g.getSmallIsomorphismX5();
		return iso.isomorphic(gi2);
	}

	@Benchmark
	public boolean isomorphismWithSmallGraphX6(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getSmallFastGraphIsomorphismX62();
		Isomorphism iso = g.getSmallIsomorphismX6();
		return iso.isomorphic(gi2);
	}

	@Benchmark
	public boolean isomorphismWithMediumGraphs(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getMediumFastGraphIsomorphism2();
		Isomorphism iso = g.getMediumIsomorphism();
		return iso.isomorphic(gi2);
	}

	@Benchmark
	public boolean isomorphismWithLargeGraphs(GraphContainer g) {
		FastGraphIsomorphism gi2 = g.getLargeFastGraphIsomorphism2();
		Isomorphism iso = g.getLargeIsomorphism();
		return iso.isomorphic(gi2);
	}
}
