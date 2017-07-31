package test.uk.ac.kent.dover.fastGraph;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.uk.ac.kent.dover.TestRunner;

import uk.ac.kent.dover.fastGraph.ExactIsomorphism;
import uk.ac.kent.dover.fastGraph.FastGraph;
import uk.ac.kent.dover.fastGraph.FastGraphException;
import uk.ac.kent.dover.fastGraph.isomorphism.Isomorphism;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IsomorphismTest {
	@Test
	public void test001() throws Exception {
		FastGraph g1,g2;

		g1 = FastGraph.randomGraphFactory(0, 0, 1, false);
		g2 = FastGraph.randomGraphFactory(0, 0, 1, false);
		assertTrue(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.randomGraphFactory(1, 0, 1, false);
		g2 = FastGraph.randomGraphFactory(1, 0, 1, false);
		assertTrue(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test002() throws FastGraphException {
		FastGraph g2;
		FastGraph g1;
		
		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeB(), false);
		assertTrue(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeB(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		assertTrue(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeC(), false);
		assertFalse(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeC(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		assertFalse(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeB(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeC(), false);
		assertFalse(Isomorphism.isomorphic(g1, g2));
		
		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeC(), false);
		assertFalse(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test003() throws Exception {
		FastGraph g2;
		FastGraph g1;

		g1 = FastGraph.randomGraphFactory(12, 30, 111, true);
		g2 = FastGraph.randomGraphFactory(12, 30, 111, true);
		assertTrue(Isomorphism.isomorphic(g1, g2));
		
		g1 = FastGraph.randomGraphFactory(12, 30, 111, false);
		g2 = FastGraph.randomGraphFactory(12, 30, 112, false);
		assertFalse(Isomorphism.isomorphic(g1, g2));
		
		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeB(), false);
		assertTrue(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeB(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		assertTrue(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeC(), false);
		assertFalse(Isomorphism.isomorphic(g1, g2));

		g1 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeC(), false);
		g2 = FastGraph.jsonStringGraphFactory(TestRunner.get5Node7EdgeA(), false);
		assertFalse(Isomorphism.isomorphic(g1, g2));
	}

	
	@Rule
	public ExpectedException thrown1 = ExpectedException.none();
	@Test
	public void test004() throws Exception {
		FastGraph g1;
		g1 = FastGraph.randomGraphFactory(10, 20, 1, false); // disconnected graph
		thrown1.expect(FastGraphException.class);
		Isomorphism.isomorphic(g1, g1);
	}

	@Rule
	public ExpectedException thrown2 = ExpectedException.none();
	@Test
	public void test005() throws Exception {
		FastGraph g1,g2;

		g1 = FastGraph.randomGraphFactory(10, 20, 6, false); // connected graph
		g2 = FastGraph.randomGraphFactory(10, 20, 1, false); // disconnected graph
		thrown2.expect(FastGraphException.class);
		Isomorphism.isomorphic(g1, g2);
	}

	@Test
	public void test006() throws Exception {
		FastGraph g1,g2;

		g1 = FastGraph.randomGraphFactory(10, 20, 6, false);

		g2 = FastGraph.randomGraphFactory(10, 25, 6, false);
		assertFalse(Isomorphism.isomorphic(g1, g2));

		g2 = FastGraph.randomGraphFactory(12, 20, 6, false);
		assertFalse(Isomorphism.isomorphic(g1, g2));

		g2 = FastGraph.randomGraphFactory(10, 20, 2, false);
		assertFalse(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test010() throws FastGraphException {
		FastGraph g1 = null;
		try {
			g1 = FastGraph.randomGraphFactory(50, 150, 999, true, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		FastGraph g2 = ExactIsomorphism.generateRandomIsomorphicGraph(g1, 444, false);
		assertTrue(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test011() throws FastGraphException {
		FastGraph g1 = null, g2 = null;
		try {
			g1 = FastGraph.randomGraphFactory(50, 150, 999, true, false);
			g2 = FastGraph.randomGraphFactory(50, 150, 6, true, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		assertFalse(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test012() throws FastGraphException {
		FastGraph g1 = null;
		try {
			g1 = FastGraph.randomGraphFactory(100, 300, 1, true, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		FastGraph g2 = ExactIsomorphism.generateRandomIsomorphicGraph(g1, 2, false);
		assertTrue(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test013() throws FastGraphException {
		FastGraph g1 = null, g2 = null;
		try {
			g1 = FastGraph.randomGraphFactory(100, 300, 1, true, false);
			g2= FastGraph.randomGraphFactory(100, 300, 2, true, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		assertFalse(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test014() throws FastGraphException {
		FastGraph g1 = null;
		try {
			g1 = FastGraph.randomGraphFactory(500, 2000, 1, true, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		FastGraph g2 = ExactIsomorphism.generateRandomIsomorphicGraph(g1, 2, false);
		assertTrue(Isomorphism.isomorphic(g1, g2));
	}

	@Test
	public void test015() throws FastGraphException {
		FastGraph g1 = null, g2 = null;
		try {
			g1 = FastGraph.randomGraphFactory(500, 2000, 1, true, false);
			g2= FastGraph.randomGraphFactory(500, 2000, 99, true, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		assertFalse(Isomorphism.isomorphic(g1, g2));
	}
}
