package datastructure;

import java.lang.reflect.Array;
import java.util.Arrays;

public class CursorBasedArray<T> {

    T[] array;
    int size;
    int cursor;

    public CursorBasedArray(Class<T> clazz, int size) {
        this.array = (T[])Array.newInstance(clazz, size);
        this.size = size;
        this.cursor = 0;
    }

    public int cursorPosition() {
        return cursor;
    }

    public int add(T element) {
        if (cursor < size) {
            array[cursor] = element;
            nextFreeCursorPosition();
            return cursor - 1;
        } else {
            throw throwIndexOutOfBoundsException(cursor);
        }
    }

    public void add(int index, T element) {
        if (index < size && index >= 0) {
            array[index] = element;
        } else {
            throw throwIndexOutOfBoundsException(index);
        }
    }

    public T get(int index) {
        if (index < size && index >= 0) {
            return array[index];
        } else {
            throw throwIndexOutOfBoundsException(index);
        }
    }

    public boolean contains(T element) {
        return Arrays.asList(array).contains(element);
    }

    public void clear() {
        Arrays.fill(array, null);
        cursor = 0;
    }

    private void nextFreeCursorPosition() {
        cursor++;
        while (cursor < size && array[cursor] != null) {
            cursor++;
        }
    }

    private IndexOutOfBoundsException throwIndexOutOfBoundsException(int index) {
        return new IndexOutOfBoundsException("Index " + index + " for array of size " + size);
    }
}
