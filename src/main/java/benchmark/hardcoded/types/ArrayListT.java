package benchmark.hardcoded.types;

import java.util.*;

public class ArrayListT<T> implements RandomAccess, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 8683452581122892189L;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final Object[] EMPTY_ELEMENTDATA = new Object[0];
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = new Object[0];

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    transient Object[] elementData;

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;

    /**
     * The default constructor which creates a custom ArrayList with an initial capacity of 10 elements
     */
    public ArrayListT() {
        this(10);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArrayListT(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else {
            if (initialCapacity != 0) {
                throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
            }

            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * A version of rangeCheck used by add and addAll.
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    public boolean add(T var1) {
        this.ensureCapacityInternal(this.size + 1);
        this.elementData[this.size++] = var1;
        return true;
    }

    public void add(int var1, T var2) {
        this.rangeCheckForAdd(var1);
        this.ensureCapacityInternal(this.size + 1);
        System.arraycopy(this.elementData, var1, this.elementData, var1 + 1, this.size - var1);
        this.elementData[var1] = var2;
        ++this.size;
    }

    private void ensureCapacityInternal(int minCapacity) {
        this.ensureExplicitCapacity(calculateCapacity(this.elementData, minCapacity));
    }


    private static int calculateCapacity(Object[] var0, int var1) {
        return var0 == DEFAULTCAPACITY_EMPTY_ELEMENTDATA ? Math.max(10, var1) : var1;
    }


    private void ensureExplicitCapacity(int var1) {
        if (var1 - this.elementData.length > 0) {
            this.grow(var1);
        }

    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    public T get(int i) {
        rangeCheck(i);

        return (T) elementData[i];
    }

    public int size() {
        return this.size;
    }
}
