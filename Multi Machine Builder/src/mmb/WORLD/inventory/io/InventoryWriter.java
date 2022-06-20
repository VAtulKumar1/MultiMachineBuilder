/**
 * 
 */
package mmb.WORLD.inventory.io;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import mmb.WORLD.inventory.ItemStack;
import mmb.WORLD.items.ItemEntry;

/**
 * @author oskar
 * Pushes items into inventory
 */
public interface InventoryWriter {
	/**
	 * Pushes given item entry to the given inventory
	 * @param ent itemto insert
	 * @param amount number of items
	 * @return number of items inserted into inventory
	 */
	public int write(ItemEntry ent, int amount);
	/**
	 * @param stack item stack to insert
	 * @return number of items inserted into inventory
	 */
	public default int write(ItemStack stack) {
		return write(stack.item, stack.amount);
	}
	/**
	 * @param ent item to insert
	 * @return number of items inserted into inventory, here 0 or 1
	 */
	public default int write(ItemEntry ent) {
		return write(ent, 1);
	}
	
	/**
	 * Represents an interface which does not allow input
	 */
	@Nonnull public static final InventoryWriter NONE = (ent, amt) -> 0;
	
	/**
	 * Writes items to the first writer, only if items match the filter.
	 * Otherwise it writes them to the second writer.
	 * @author oskar
	 */
	public static class Shunting implements InventoryWriter{
		private final InventoryWriter ifTrue;
		private final InventoryWriter ifFalse;
		private final Predicate<ItemEntry> filter;
		/**
		 * Creates a shunting writer
		 * @param ifTrue items go here if filter accepts them
		 * @param ifFalse items go here if filter rejects them
		 * @param filter item filter
		 */
		public Shunting(InventoryWriter ifTrue, InventoryWriter ifFalse, Predicate<ItemEntry> filter) {
			this.ifTrue = ifTrue;
			this.ifFalse = ifFalse;
			this.filter = filter;
		}
		@Override
		public int write(ItemEntry ent, int amount) {
			if(filter.test(ent)) 
				return ifTrue.write(ent, amount);
			return ifFalse.write(ent, amount);
		}
		
	}
	/**
	 * Writes items to inventory, only if items match the filter.
	 * Otherwise it rejects them.
	 * @author oskar
	 */
	public static class Filtering implements InventoryWriter{
		private final InventoryWriter writer;
		private final Predicate<ItemEntry> filter;
		/**
		 * Creates a filtering writer
		 * @param writer backing inventory writer
		 * @param filter item filter
		 */
		public Filtering(InventoryWriter writer, Predicate<ItemEntry> filter) {
			this.writer = writer;
			this.filter = filter;
		}
		@Override
		public int write(ItemEntry ent, int amount) {
			if(filter.test(ent)) 
				return writer.write(ent, amount);
			return 0;
		}
	}
	/**
	 * @author oskar
	 * Writes items to inventory,
	 * preferring the first inventory.
	 * If it fails, tries writing second inventory.
	 * If both fail, rejects some or all items
	 */
	public static class Priority implements InventoryWriter{
		private final InventoryWriter first;
		private final InventoryWriter other;
		/**
		 * Creates a priority writer
		 * @param here preferred inventory
		 * @param other secondary inventory
		 */
		public Priority(InventoryWriter here, InventoryWriter other) {
			this.first = here;
			this.other = other;
		}

		@Override
		public int write(ItemEntry ent, int amount) {
			int writeFirst = first.write(ent, amount);
			if(writeFirst == amount) {
				return amount; //all accepted
			}
			int next = amount - writeFirst;
			int writeSecond = other.write(ent, next);
			return writeFirst+writeSecond;
		}		
	}
	/**
	 * @author oskar
	 * Writes items to inventories
	 */
	public static class Splitter implements InventoryWriter{
		private final InventoryWriter[] writers;
		/**
		 * Creates a splitter with given writers;
		 * @param writers list of targeted writers
		 */
		public Splitter(InventoryWriter... writers) {
			this.writers = writers;
		}
		private int pos = 0;
		@Override
		public int write(ItemEntry ent, int amount) {
			next();
			int remaining = amount;
			int transferred = 0;
			for(int i = 0; i < writers.length; i++) {
				InventoryWriter writer = writers[pos];
				int now = writer.write(ent, remaining);
				remaining -= now;
				transferred += now;
				if(remaining == 0) return transferred;
				next();
			}
			return transferred;
		}
		
		private int next() {
			pos++;
			if(pos >= writers.length) pos = 0;
			return pos;
		}
		
	}
}
