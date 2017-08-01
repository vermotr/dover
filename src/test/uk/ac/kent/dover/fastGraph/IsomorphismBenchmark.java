package test.uk.ac.kent.dover.fastGraph;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import uk.ac.kent.dover.fastGraph.BacktrackIsomorphism;
import uk.ac.kent.dover.fastGraph.ExactIsomorphism;
import uk.ac.kent.dover.fastGraph.FastGraph;
import uk.ac.kent.dover.fastGraph.isomorphism.FastGraphIsomorphism;
import uk.ac.kent.dover.fastGraph.isomorphism.Isomorphism;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class IsomorphismBenchmark {
	private FastGraph g1[], g2[];
	private ExactIsomorphism ei[];

	private BacktrackIsomorphism bi[];
	private FastGraphIsomorphism gi1[], gi2[];
	private Isomorphism iso[];

	private int testValues[] = {10, 30, 50, 70, 90, 110, 130};

	@Test public void
	launchBenchmark() throws RunnerException {
		Options options = new OptionsBuilder()
			.include(this.getClass().getSimpleName())
			.mode(Mode.AverageTime)
			.timeUnit(TimeUnit.MILLISECONDS)
			.warmupTime(TimeValue.seconds(1))
			.warmupIterations(2)
			.measurementTime(TimeValue.seconds(1))
			.measurementIterations(2)
			.timeout(TimeValue.seconds(3))
			.threads(1)
			.forks(1)
			.shouldFailOnError(true)
			.shouldDoGC(true)
			.jvmArgs("-server")
			.build();
		new Runner(options).run();
	}

	@Setup(Level.Trial)
	public void init() {
		int len = testValues.length;
		g1 = new FastGraph[len];
		g2 = new FastGraph[len];
		gi1 = new FastGraphIsomorphism[len];
		gi2 = new FastGraphIsomorphism[len];

		ei = new ExactIsomorphism[len];
		bi = new BacktrackIsomorphism[len];
		iso = new Isomorphism[len];

		try {
			int i = 0;
			System.out.println(i);
			for (i = 0; i < len; i++) {
				System.out.println("Generate graphs " + i);
				g1[i] = FastGraph.randomGraphFactory(testValues[i], testValues[i] * 3, 2, true, false);
				g2[i] = ExactIsomorphism.generateRandomIsomorphicGraph(g1[i], 42, false);
				gi1[i] = new FastGraphIsomorphism(g1[i]);
				gi2[i] = new FastGraphIsomorphism(g2[i]);

				ei[i] = new ExactIsomorphism(g1[i]);
				bi[i] = new BacktrackIsomorphism(g1[i]);
				iso[i] = new Isomorphism(gi1[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ExactIsomorphism benchmarks
	 */

	@Benchmark
	public void exactIsomorphism0() throws Exception {
		ei[0].isomorphic(g2[0]);
	}

	@Benchmark
	public void exactIsomorphism1() throws Exception {
		ei[1].isomorphic(g2[1]);
	}

	@Benchmark
	public void exactIsomorphism2() throws Exception {
		ei[2].isomorphic(g2[2]);
	}

	@Benchmark
	public void exactIsomorphism3() throws Exception {
		ei[3].isomorphic(g2[3]);
	}

	/**
	 * BacktrackIsomorphism benchmarks
	 */

	@Benchmark
	public void backtrackIsomorphism0() throws Exception {
		bi[0].isomorphic(g2[0]);
	}

	@Benchmark
	public void backtrackIsomorphism1() throws Exception {
		bi[1].isomorphic(g2[1]);
	}

	/**
	 * Isomorphism benchmarks
	 */

	@Benchmark
	public void isomorphism0() {
		iso[0].isomorphic(gi2[0]);
	}

	@Benchmark
	public void isomorphism1() {
		iso[1].isomorphic(gi2[1]);
	}

	@Benchmark
	public void isomorphism2() {
		iso[2].isomorphic(gi2[2]);
	}

	@Benchmark
	public void isomorphism3() {
		iso[3].isomorphic(gi2[3]);
	}

	@Benchmark
	public void isomorphism4() {
		iso[4].isomorphic(gi2[4]);
	}

	@Benchmark
	public void isomorphism5() {
		iso[5].isomorphic(gi2[5]);
	}

	@Benchmark
	public void isomorphism6() {
		iso[6].isomorphic(gi2[6]);
	}
}
