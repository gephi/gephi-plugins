/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.data.attributes.type;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.Estimator;

/**
 * A special type which provides methods of getting/setting values of any time
 * interval. It is internally implemented using Interval Tree for efficiency.
 *
 * @author Cezary Bartosiak
 * 
 * @param <T> type of data
 */
public abstract class DynamicType<T> {
	protected IntervalTree<T> intervalTree;

	/**
	 * Constructs a new {@code DynamicType} instance with no intervals.
	 */
	public DynamicType() {
		intervalTree = new IntervalTree<T>();
	}

	/**
	 * Constructs a new {@code DynamicType} instance that contains a given
	 * {@code Interval<T>} in.
	 * 
	 * @param in interval to add (could be null)
	 */
	public DynamicType(Interval<T> in) {
		this();
		if (in != null)
			intervalTree.insert(in);
	}

	/**
	 * Constructs a new {@code DynamicType} instance with intervals given by
	 * {@code List<Interval<T>>} in.
	 * 
	 * @param in intervals to add (could be null)
	 */
	public DynamicType(List<Interval<T>> in) {
		this();
		if (in != null)
			for (Interval<T> interval : in)
				intervalTree.insert(interval);
	}

	/**
	 * Constructs a deep copy of {@code source}.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 */
	public DynamicType(DynamicType<T> source) {
		if (source == null)
			intervalTree = new IntervalTree<T>();
		else intervalTree = new IntervalTree<T>(source.intervalTree);
	}

	/**
	 * Constructs a deep copy of {@code source} that contains a given
	 * {@code Interval<T>} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     interval to add (could be null)
	 */
	public DynamicType(DynamicType<T> source, Interval<T> in) {
		this(source);
		if (in != null)
			intervalTree.insert(in);
	}

	/**
	 * Constructs a deep copy of {@code source} that contains a given
	 * {@code Interval<T>} in. Before add it removes from the newly created
	 * object all intervals that overlap with a given {@code Interval<T>} out.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     interval to add (could be null)
	 * @param out    interval to remove (could be null)
	 */
	public DynamicType(DynamicType<T> source, Interval<T> in, Interval<T> out) {
		this(source);
		if (out != null)
			intervalTree.delete(out);
		if (in != null)
			intervalTree.insert(in);
	}

	/**
	 * Constructs a deep copy of {@code source} with additional intervals
	 * given by {@code List<Interval<T>>} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 */
	public DynamicType(DynamicType<T> source, List<Interval<T>> in) {
		this(source);
		if (in != null)
			for (Interval<T> interval : in)
				intervalTree.insert(interval);
	}

	/**
	 * Constructs a deep copy of {@code source} with additional intervals
	 * given by {@code List<Interval<T>>} in. Before add it removes from the
	 * newly created object all intervals that overlap with intervals given by
	 * {@code List<Interval<T>>} out.
	 * 
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 * @param out    intervals to remove (could be null)
	 */
	public DynamicType(DynamicType<T> source, List<Interval<T>> in, List<Interval<T>> out) {
		this(source);
		if (out != null)
			for (Interval<T> interval : out)
				intervalTree.delete(interval);
		if (in != null)
			for (Interval<T> interval : in)
				intervalTree.insert(interval);
	}

	/**
	 * Returns the leftmost point or {@code Double.NEGATIVE_INFINITY} in case
	 * of no intervals.
	 *
	 * @return the leftmost point.
	 */
	public double getLow() {
		return intervalTree.getLow();
	}

	/**
	 * Returns the rightmost point or {@code Double.POSITIVE_INFINITY} in case
	 * of no intervals.
	 *
	 * @return the rightmost point.
	 */
	public double getHigh() {
		return intervalTree.getHigh();
	}

	/**
	 * Indicates if the leftmost point is excluded.
	 *
	 * @return {@code true} if the leftmost point is excluded,
	 *         {@code false} otherwise.
	 */
	public boolean isLowExcluded() {
		return intervalTree.isLowExcluded();
	}

	/**
	 * Indicates if the rightmost point is excluded.
	 *
	 * @return {@code true} if the rightmost point is excluded,
	 *         {@code false} otherwise.
	 */
	public boolean isHighExcluded() {
		return intervalTree.isHighExcluded();
	}

	/**
	 * Indicates if a given time interval overlaps with any interval of this instance.
	 *
	 * @param interval a given time interval
	 *
	 * @return {@code true} a given time interval overlaps with any interval of this
	 *         instance, otherwise {@code false}.
	 */
	public boolean isInRange(Interval interval) {
		return intervalTree.overlapsWith(interval);
	}

	/**
	 * Indicates if [{@code low}, {@code high}] interval overlaps with any interval of this instance.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @return {@code true} a given time interval overlaps with any interval of this
	 *         instance, otherwise {@code false}.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public boolean isInRange(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		return intervalTree.overlapsWith(new Interval(low, high));
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code -inf}, {@code inf}] time interval.
	 * {@code Estimator.FIRST} is used.
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code -inf}, {@code inf}] time interval or
	 *         {@code null} if there are no intervals.
	 * 
	 * @see Estimator
	 */
	public T getValue() {
		return getValue(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a given time interval.
	 * {@code Estimator.FIRST} is used.
	 *
	 * @param interval a given time interval
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a given time interval or
	 *         {@code null} if there are no intervals.
	 * 
	 * @see Estimator
	 */
	public T getValue(Interval interval) {
		return getValue(interval, Estimator.FIRST);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code low}, {@code high}] time interval.
	 * {@code Estimator.FIRST} is used.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code low}, {@code high}] time interval or
	 *         {@code null} if there are no intervals.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 *
	 * @see Estimator
	 */
	public T getValue(double low, double high) {
		return getValue(low, high, Estimator.FIRST);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code -inf}, {@code inf}] time interval.
	 *
	 * @param estimator used to estimate the result
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code -inf}, {@code inf}] time interval or
	 *         {@code null} if there are no intervals.
	 *
	 * @throws UnsupportedOperationException if type {@code T} doesn't support
	 *                                       the given {@code estimator}.
	 *
	 * @see Estimator
	 */
	public T getValue(Estimator estimator) {
		return getValue(Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY,
						estimator);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a given time interval.
	 *
	 * @param interval  a given time interval
	 * @param estimator used to estimate the result
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a given time interval or
	 *         {@code null} if there are no intervals.
	 *
	 * @throws UnsupportedOperationException if type {@code T} doesn't support
	 *                                       the given {@code estimator}.
	 *
	 * @see Estimator
	 */
	public abstract T getValue(Interval interval, Estimator estimator);

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code low}, {@code high}] time interval.
	 *
	 * @param low       the left endpoint
	 * @param high      the right endpoint
	 * @param estimator used to estimate the result
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code low}, {@code high}] time interval or
	 *         {@code null} if there are no intervals.
	 *
	 * @throws IllegalArgumentException      if {@code low} > {@code high}.
	 * @throws UnsupportedOperationException if type {@code T} doesn't support
	 *                                       the given {@code estimator}.
	 *
	 * @see Estimator
	 */
	public T getValue(double low, double high, Estimator estimator) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		return getValue(new Interval(low, high, false, false), estimator);
	}

	/**
	 * Returns a list of all values stored in this instance.
	 *
	 * @return a list of all values stored in this instance.
	 */
	public List<T> getValues() {
		return getValues(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Returns a list of values whose time intervals overlap with a
	 * [{@code low}, {@code high}] time interval.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @return a list of values whose time intervals overlap with a
	 *         [{@code low}, {@code high}] time interval.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public List<T> getValues(double low, double high) {
		return getValues(new Interval(low, high));
	}

	/**
	 * Returns a list of values whose time intervals overlap with a
	 * given time interval.
	 *
	 * @param interval a given time interval
	 *
	 * @return a list of values whose time intervals overlap with a
	 *         given time interval.
	 */
	public List<T> getValues(Interval interval) {
		List<T> result = new ArrayList<T>();
		for (Interval<T> i : intervalTree.search(interval))
			result.add(i.getValue());
		return result;
	}
        
        /**
	 * Returns a list of all intervals.
	 *
	 * @return a list of intervals which overlap with a given time interval.
	 */
	public List<Interval<T>> getIntervals() {
		return intervalTree.getIntervals();
	}

	/**
	 * Returns a list of intervals which overlap with a given time interval.
	 *
	 * @param interval a given time interval
	 *
	 * @return a list of intervals which overlap with a given time interval.
	 */
	public List<Interval<T>> getIntervals(Interval interval) {
		return intervalTree.search(interval);
	}

	/**
	 * Returns a list of intervals which overlap with a
	 * [{@code low}, {@code high}] time interval.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @return a list of intervals which overlap with a
	 *         [{@code low}, {@code high}] time interval.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public List<Interval<T>> getIntervals(double low, double high) {
		return intervalTree.search(low, high);
	}

	/**
	 * Returns the underlying type {@code T}.
	 *
	 * @return the underlying type {@code T}.
	 */
	public abstract Class getUnderlyingType();

	/**
	 * Compares this instance with the specified object for equality.
	 *
	 * <p>Note that two {@code DynamicType} instances are equal if they have got
	 * the same type {@code T} and their interval trees are equal.
	 *
	 * @param obj object to which this instance is to be compared
	 *
	 * @return {@code true} if and only if the specified {@code Object} is a
	 *         {@code DynamicType} which has the same type {@code T} and an
	 *         equal interval tree.
	 * 
	 * @see #hashCode
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass()) &&
				((DynamicType<T>)obj).intervalTree.equals(intervalTree))
			return true;
		return false;
	}

	/**
	 * Returns a hashcode of this instance.
	 *
	 * @return a hashcode of this instance.
	 */
	@Override
	public int hashCode() {
		return intervalTree.hashCode();
	}

	/**
	 * Creates a string representation of all the intervals with their values.
	 *
	 * @param timesAsDoubles indicates if times should be shown as doubles or dates
	 *
	 * @return a string representation with times as doubles or dates.
	 */
	public String toString(boolean timesAsDoubles) {
		return intervalTree.toString(timesAsDoubles);
	}

	/**
	 * Returns a string representation of this instance in a format
	 * {@code <[low, high, value], ..., [low, high, value]>}. Intervals are
	 * ordered by its left endpoint.
	 *
	 * @return a string representation of this instance.
	 */
	@Override
	public String toString() {
		return intervalTree.toString();
	}
}
