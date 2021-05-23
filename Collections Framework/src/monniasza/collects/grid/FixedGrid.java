/**
 * 
 */
package monniasza.collects.grid;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author oskar
 * @param <T> type of data
 * Contains a grid with fixed size
 */
public class FixedGrid<T> implements Grid<T> {
	private final Object[][] arr;
	private final int h;
	private final int w;
	
	/**
	 * @param w width
	 * @param h height
	 */
	public FixedGrid(int w, int h) {
		arr = new Object[w][h];
		this.h = h;
		this.w = w;
	}
	public FixedGrid(int s) {
		this(s, s);
	}
	
	/**
	 * Creates a rectangular FixedGrid with data
	 * @param w width
	 * @param h height
	 * @param data data to be written
	 * @throws NullPointerException if {@code data} is null
	 * @throws IllegalArgumentException if {@code data}'s size is less than {@code w} × {@code h}
	 */
	@SafeVarargs
	public FixedGrid(int w, int h, T... data) {
		this(w, h);
		int reqd = size();
		if(data.length < reqd) {
			throw new IllegalArgumentException("Minimum amount of items: "+reqd+", provided: "+data.length);
		}
		for(int i = 0, y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				set(x, y, data[i]);
			}
		}
	}
	
	/**
	 * Creates a square FixedGrid with data
	 * @param s size of each side
	 * @param data data to be written
	 * @throws NullPointerException if {@code data} is null
	 * @throws IllegalArgumentException if {@code data}'s size is less than {@code s}²
	 */
	@SafeVarargs
	public FixedGrid(int s, T... data) {
		this(s, s, data);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(arr);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Grid) {
			Grid<?> other = (Grid<?>) obj;
			if(other.width() != arr.length) return false;
			if(other.height() != h) return false;
			for(int i = 0; i < w; i++) {
				for(int j = 0; j < h; j++) {
					if(!Objects.equals(other.get(i, j), arr[i][j])) return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void set(int x, int y, T data) {
		arr[x][y] = data;
	}

	@SuppressWarnings({"unchecked", "unchested"})
	@Override
	public T get(int x, int y) {
		return (T) arr[x][y];
	}

	@Override
	public int width() {
		return w;
	}
	
	@Override
	public int height() {
		return h;
	}

}
