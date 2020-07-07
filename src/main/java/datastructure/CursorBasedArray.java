package datastructure;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class CursorBasedArray<T extends Comparable<T>> {

    T[] array;
    int size;
    int cursor;

    private T emptyValue;

    public CursorBasedArray(Class<T> clazz, T emptyValue, int size) {
        this.emptyValue = emptyValue;
        this.array = (T[])Array.newInstance(clazz, size);
        Arrays.fill(array, emptyValue);
        this.size = size;
        this.cursor = 0;
    }

    public int size() {
        return size;
    }

    public int cursorPosition() {
        return cursor;
    }

    public T emptyValue() {
        return emptyValue;
    }

    public int add(T element) {
        if (cursor < size) {
            array[cursor] = element;
            nextFreeCursorPosition();
            return cursor - 1;
        } else {
            throw indexOutOfBoundsException(cursor);
        }
    }

    public void add(int index, T element) {
        if (index < size && index >= 0) {
            array[index] = element;
            if (index > cursor) {
                cursor = index;
            }
        } else {
            throw indexOutOfBoundsException(index);
        }
    }

    public T get(int index) {
        if (index < size && index >= 0) {
            return array[index];
        } else {
            throw indexOutOfBoundsException(index);
        }
    }

    public boolean contains(T element) {
        for (T value : array) {
            if (value.equals(element)) {
                return true;
            }
        }
        return false;
    }

    public int find(T element) {
        for (int i = 0; i < array.length; i++) {
            T value = array[i];
            if (value.equals(element)) {
                return i;
            }
        }
        throw new IllegalStateException(String.format("Element %s was not found.", element));
    }

    public void clear() {
        Arrays.fill(array, emptyValue);
        cursor = 0;
    }

    public List<T> asList() {
        return Arrays.asList(array);
    }

    private void nextFreeCursorPosition() {
        cursor++;
        while (cursor < size && !array[cursor].equals(emptyValue)) {
            cursor++;
        }
    }

    private IndexOutOfBoundsException indexOutOfBoundsException(int index) {
        return new IndexOutOfBoundsException("Index " + index + " for array of size " + size);
    }
}
