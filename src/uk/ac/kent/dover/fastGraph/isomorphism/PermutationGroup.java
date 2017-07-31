package uk.ac.kent.dover.fastGraph.isomorphism;

/**
 * Graph isomorphism
 * Copyright (c) 2017 Romain Vermot
 *
 * This class is largely copied from code due Gilleain Torrance:
 * https://github.com/gilleain/group
 *
 * This is port of the code from the C.A.G.E.S. book by Kreher and Stinson. The
 * mathematics in the description above is also from that book (pp. 203).
 * 
 * @author maclean
 * @author Romain Vermot <rfmv2@kent.ac.uk>
 */
public class PermutationGroup {
	/**
	 * The compact list of permutations that make up this group
	 */
	private Permutation[][] permutations;

	/**
	 * The size of the group - strictly, the size of the permutation
	 */
	private int n;

	/**
	 * The base of the group
	 */
	private Permutation base;

	public PermutationGroup(int n) {
		this(new Permutation(n));
	}

	/**
	 * Creates the initial group, with the base <code>base</code>.
	 *
	 * @param base the permutation that the group is based on
	 */
	public PermutationGroup(Permutation base) {
		this.n = base.size();
		this.base = new Permutation(base);
		this.permutations = new Permutation[n][n];
		for (int i = 0; i < n; i++) {
			this.permutations[i][this.base.get(i)] = new Permutation(n);
		}
	}

	/**
	 * Get one of the permutations that make up the compact representation.
	 *
	 * @param i the index of the set U.
	 * @param j the index of the permutation within Ui.
	 * @return a permutation
	 */
	public Permutation get(int i, int j) {
		return this.permutations[i][j];
	}

	/**
	 * Change the base of the group to the new base <code>newBase</code>.
	 *
	 * @param newBase the new base for the group
	 */
	public void changeBase(Permutation newBase) {
		PermutationGroup H =
			new PermutationGroup(newBase);

		int r = this.base.firstIndexOfDifference(newBase);

		for (int j = r; j < n; j++) {
			for (int a = 0; a < n; a++) {
				Permutation g = this.permutations[j][a];
				if (g != null) {
					H.enter(g);
				}
			}
		}

		for (int j = 0; j < r; j++) {
			for (int a = 0; a < n; a++) {
				Permutation g = this.permutations[j][a];
				if (g != null) {
					int h = H.base.get(j);
					int x = g.get(h);
					H.permutations[j][x] = new Permutation(g);
				}
			}
		}
		this.base = new Permutation(H.base);
		this.permutations = H.permutations.clone();
	}

	/**
	 * Enter the permutation g into this group.
	 *
	 * @param g a permutation
	 */
	public void enter(Permutation g) {
		int deg = this.n;
		int i = test(g);

		if (i == deg) {
			return;
		} else {
			this.permutations[i][g.get(this.base.get(i))] = new Permutation(g);
		}

		for (int j = 0; j <= i; j++) {
			for (int a = 0; a < deg; a++) {
				Permutation h = this.permutations[j][a];
				if (h != null) {
					Permutation f = g.multiply(h);
					enter(f);
				}
			}
		}
	}

	/**
	 * Test a permutation to see if it is in the group. Note that this also
	 * alters the permutation passed in.
	 *
	 * @param permutation the one to test
	 * @return the position it should be in the group, if any
	 */
	public int test(Permutation permutation) {
		for (int i = 0; i < n; i++) {
			int x = permutation.get(this.base.get(i));
			Permutation h = permutations[i][x];
			if (h == null) {
				return i;
			} else {
				permutation.setTo(h.invert().multiply(permutation));
			}
		}
		return n;
	}

	/**
	 * @inheritDoc
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Base = ").append(this.base).append("\n");
		for (int i = 0; i < this.n; i++) {
			sb.append("U").append(i).append(" = ");
			for (int j = 0; j < this.n; j++) {
				sb.append(this.permutations[i][j]).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
