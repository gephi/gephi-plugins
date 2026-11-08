package cwts.util;

import java.util.Arrays;

/**
 * Dynamic array of integers.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class DynamicIntArray implements Cloneable
{
    /**
     * Default initial capacity of the array.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    /**
     * Relative increase in the capacity of the array when the existing
     * capacity is insufficient.
     */
    public static final double RELATIVE_CAPACITY_INCREASE = 0.5;

    private int[] values;
    private int size;

    /**
     * Constructs an empty dynamic array.
     *
     */
    public DynamicIntArray()
    {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs an empty dynamic array with a specified initial capacity.
     *
     * @param initialCapacity Initial capacity
     */
    public DynamicIntArray(int initialCapacity)
    {
        values = new int[initialCapacity];
        size = 0;
    }

    /**
     * Constructs a dynamic array containing specified values.
     *
     * @param values Values
     */
    public DynamicIntArray(int[] values)
    {
        values = values.clone();
        size = values.length;
    }

    /**
     * Clones the array.
     *
     * @return Cloned array
     */
    public DynamicIntArray clone()
    {
        DynamicIntArray clonedArray;

        try
        {
            clonedArray = (DynamicIntArray)super.clone();
            clonedArray.values = values.clone();
            return clonedArray;
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Returns the capacity of the array.
     *
     * @return Capacity
     */
    public int capacity()
    {
        return values.length;
    }

    /**
     * Ensures a specified minimum capacity of the array.
     *
     * <p>
     * The capacity is increased (if necessary) to ensure that it equals at
     * least the specified minimum capacity.
     * </p>
     *
     * @param minCapacity Minimum capacity
     */
    public void ensureCapacity(int minCapacity)
    {
        int newCapacity, oldCapacity;

        oldCapacity = values.length;
        if (minCapacity > oldCapacity)
        {
            newCapacity = (int)((1 + RELATIVE_CAPACITY_INCREASE) * oldCapacity);
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    /**
     * Returns the number of elements of the array.
     *
     * @return Size
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns the value of an element of the array.
     *
     * @param index Index
     *
     * @return Value
     */
    public int get(int index)
    {
        if (index >= size)
            throw new IndexOutOfBoundsException();

        return values[index];
    }

    /**
     * Sets an element of the array to a specified value.
     *
     * @param index Index
     * @param value Value
     */
    public void set(int index, int value)
    {
        if (index >= size)
            throw new IndexOutOfBoundsException();

        values[index] = value;
    }

    /**
     * Appends a specified value to the end of the array.
     *
     * @param value Value
     */
    public void append(int value)
    {
        ensureCapacity(size + 1);
        values[size] = value;
        size++;
    }

    /**
     * Removes all elements from the array.
     */
    public void clear()
    {
        size = 0;
    }

    /**
     * Sorts the array.
     */
    public void sort()
    {
        Arrays.sort(values, 0, size);
    }

    /**
     * Returns a static array copy.
     *
     * @return Array
     */
    public int[] toArray()
    {
        return Arrays.copyOf(values, size);
    }
}
