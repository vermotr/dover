package uk.ac.kent.dover.fastGraph.isomorphism;

import java.util.Arrays;

/**
 * Graph isomorphism
 * Copyright (c) 2017 Romain Vermot
 *
 * This class is largely copied from code due Gilleain Torrance:
 * https://github.com/gilleain/group
 *
 * A permutation with some associated methods to multiply, invert, and convert
 * to cycle strings. Much of the code in this was implemented from the 
 * C.A.G.E.S. book.
 * 
 * @author maclean
 * @author Romain Vermot <rfmv2@kent.ac.uk>
 */
public class Permutation {

	private int[] values;

	/**
	 * Constructs an identity permutation with <code>size</code> elements.
	 *
	 * @param size the number of elements in the permutation
	 */
	public Permutation(int size) {
		this.values = new int[size];
		for (int i = 0; i < size; i++) {
			this.values[i] = i;
		}
	}

	public Permutation(int... values) {
		this.values = values;
	}

	public Permutation(Permutation other) {
		this.values = other.values.clone();
	}

	public boolean equals(Object other) {
		if (other instanceof Permutation) {
			return Arrays.equals(values, ((Permutation)other).values);
		} else {
			return false;
		}
	}

	public boolean isIdentity() {
		for (int i = 0; i < this.values.length; i++) {
			if (this.values[i] != i) {
				return false;
			}
		}
		return true;
	}

	public int size() {
		return this.values.length;
	}

	public int get(int i) {
		return this.values[i];
	}

	/**
	 * Find an r such that this[r] != other[r].
	 * @param other the other permutation to compare with
	 * @return the first point at which the two permutations differ
	 */
	public int firstIndexOfDifference(Permutation other) {
		int r = 0;
		while ((r < this.values.length) && this.values[r] == other.get(r)) {
			r++;
		}
		return r;
	}

	public void set(int index, int value) {
		this.values[index] = value;
	}

	public void setTo(Permutation other) {
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = other.values[i];
		}
	}

	public Permutation multiply(Permutation other) {
		Permutation newPermutation = new Permutation(this.values.length);
		for (int i = 0; i < this.values.length; i++) {
			newPermutation.values[i] = this.values[other.values[i]];
		}
		return newPermutation;
	}

	public Permutation invert() {
		Permutation inversion = new Permutation(this.values.length);
		for (int i = 0; i < this.values.length; i++) {
			inversion.values[this.values[i]] = i;
		}
		return inversion;
	}

	public String toString() {
		return Arrays.toString(this.values);
	}
}
