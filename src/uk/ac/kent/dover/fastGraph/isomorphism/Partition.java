package uk.ac.kent.dover.fastGraph.isomorphism;

import java.util.*;

/**
 * Graph isomorphism
 * Copyright (c) 2017 Romain Vermot
 *
 * This class is largely copied from code due Gilleain Torrance:
 * https://github.com/gilleain/group
 *
 * A partition of a set of integers, such as the discrete partition {{1}, {2},
 * {3}, {4}} or the unit partition {{1, 2, 3, 4}} or an intermediate like {{1,
 * 2}, {3, 4}}.
 *
 * @author maclean
 * @author Romain Vermot <rfmv2@kent.ac.uk>
 */
public class Partition {

	/**
	 * The cells of the partition.
	 */
	private List<SortedSet<Integer>> cells;

	/**
	 * Creates a new, empty partition with no cells.
	 */
	public Partition() {
		this.cells = new ArrayList<>();
	}

	/**
	 * Copy constructor to make one partition from another.
	 *
	 * @param other the partition to copy
	 */
	public Partition(Partition other) {
		this();
		for (SortedSet<Integer> block : other.cells) {
			this.cells.add(new TreeSet<>(block));
		}
	}

	/**
	 * Create a unit partition - in other words, the coarsest possible partition
	 * where all the elements are in one cell.
	 *
	 * @param size the number of elements
	 * @return a new Partition with one cell containing all the elements
	 */
	public static Partition unit(int size) {
		Partition unit = new Partition();
		unit.cells.add(new TreeSet<Integer>());
		for (int i = 0; i < size; i++) {
			unit.cells.get(0).add(i);
		}
		return unit;
	}

	/**
	 * Gets the size of the partition (the number of cells).
	 *
	 * @return the number of cells contained in the partition
	 */
	public int size() {
		return cells.size();
	}

	/**
	 * Gets the first element in the specified cell.
	 *
	 * @param cellIndex the cell to use
	 * @return the first element in this cell
	 */
	public int getFirstInCell(int cellIndex) {
		return this.cells.get(cellIndex).first();
	}

	/**
	 * Gets the cell at this index.
	 *
	 * @param cellIndex the index of the cell to return
	 * @return the cell at this index
	 */
	public SortedSet<Integer> getCell(int cellIndex) {
		return this.cells.get(cellIndex);
	}

	/**
	 * Fill the elements of a permutation from the first element of each
	 * cell, up to the point <code>upTo</code>.
	 *
	 * @param upTo take values from cells up to this one
	 * @return the permutation representing the first element of each cell
	 */
	public Permutation setAsPermutation(int upTo) {
		int[] p = new int[upTo];
		for (int i = 0; i < upTo; i++) {
			p[i] = this.cells.get(i).first();
		}
		return new Permutation(p);
	}

		/**
		 * Checks that all the cells are singletons - that is, they only have one
		 * element. A discrete partition is equivalent to a permutation.
		 *
		 * @return true if all the cells are discrete
		 */
	public boolean isDiscrete() {
		for (SortedSet<Integer> cell : cells) {
			if (cell.size() != 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Converts the whole partition into a permutation.
	 *
	 * @return the partition as a permutation
	 */
	public Permutation toPermutation() {
		Permutation p = new Permutation(this.size());
		for (int i = 0; i < this.size(); i++) {
			p.set(i, this.cells.get(i).first());
		}
		return p;
	}

	/**
	 * Check to see if the cell at <code>cellIndex</code> is discrete - that is,
	 * it only has one element.
	 *
	 * @param cellIndex the index of the cell to check
	 * @return true of the cell at this index is discrete
	 */
	public boolean isDiscreteCell(int cellIndex) {
		return this.cells.get(cellIndex).size() == 1;
	}

	/**
	 * Gets the index of the first cell in the partition that is discrete.
	 *
	 * @return the index of the first discrete cell
	 */
	public int getIndexOfFirstNonDiscreteCell() {
		for (int i = 0; i < this.cells.size(); i++) {
			if (!isDiscreteCell(i))
				return i;
		}
		return -1;
	}

	/**
	 * Splits this partition by taking the cell at cellIndex and making two
	 * new cells - the first with the the rest of the elements from that cell
	 * and the second with the singleton splitElement.
	 *
	 * @param cellIndex the index of the cell to split on
	 * @param splitElements the element to put in its own cell
	 * @return a new (finer) Partition
	 */
	public Partition splitBefore(int cellIndex, int... splitElements) {
		Partition r = new Partition();
		// copy the blocks up to blockIndex
		for (int j = 0; j < cellIndex; j++) {
			r.addCell(this.copyBlock(j));
		}

		// split the block at block index
		r.addCell(splitElements);
		SortedSet<Integer> splitBlock = this.copyBlock(cellIndex);
		for (int splitElement : splitElements) {
			splitBlock.remove(splitElement);
		}
		r.addCell(splitBlock);

		// copy the blocks after blockIndex, shuffled up by one
		for (int j = cellIndex + 1; j < this.size(); j++) {
			r.addCell(this.copyBlock(j));
		}
		return r;
	}

	/**
	 * Splits this partition by taking the cell at cellIndex and making two
	 * new cells - the first with the the rest of the elements from that cell
	 * and the second with the singleton splitElement.
	 *
	 * @param cellIndex the index of the cell to split on
	 * @param splitElements the elements to put in its own cell
	 * @return a new (finer) Partition
	 */
	public Partition splitAfter(int cellIndex, int... splitElements) {
		Partition r = new Partition();
		// copy the blocks up to blockIndex
		for (int j = 0; j < cellIndex; j++) {
			r.addCell(this.copyBlock(j));
		}

		// split the block at block index
		SortedSet<Integer> splitBlock = this.copyBlock(cellIndex);
		for (int splitElement : splitElements) {
			splitBlock.remove(splitElement);
		}
		r.addCell(splitBlock);
		r.addCell(splitElements);

		// copy the blocks after blockIndex, shuffled up by one
		for (int j = cellIndex + 1; j < this.size(); j++) {
			r.addCell(this.copyBlock(j));
		}
		return r;
	}

	/**
	 * Adds a new cell to the end of the partition containing these elements.
	 *
	 * @param elements the elements to add in a new cell
	 */
	public void addCell(int... elements) {
		SortedSet<Integer> cell = new TreeSet<>();
		for (int element : elements) {
			cell.add(element);
		}
		this.cells.add(cell);
	}

	/**
	 * Adds a new cell to the end of the partition.
	 *
	 * @param elements the collection of elements to put in the cell
	 */
	public void addCell(Collection<Integer> elements) {
		cells.add(new TreeSet<>(elements));
	}

	/**
	 * Removes the cell at the specified index.
	 *
	 * @param index the index of the cell to remove
	 */
	public void removeCell(int index) {
		this.cells.remove(index);
	}

	/**
	 * Insert a cell into the partition at the specified index.
	 *
	 * @param index the index of the cell to add
	 * @param cell the cell to add
	 */
	public void insertCell(int index, SortedSet<Integer> cell) {
		this.cells.add(index, cell);
	}

	/**
	 * Creates and returns a copy of the cell at cell index.
	 *
	 * @param cellIndex the cell to copy
	 * @return the copy of the cell
	 */
	public SortedSet<Integer> copyBlock(int cellIndex) {
		return new TreeSet<>(this.cells.get(cellIndex));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
			SortedSet<Integer> cell = cells.get(cellIndex);
			for (int element : cell) {
				sb.append(element);
			}
			if (cells.size() > 1 && cellIndex < cells.size() - 1) {
				sb.append("|");
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
