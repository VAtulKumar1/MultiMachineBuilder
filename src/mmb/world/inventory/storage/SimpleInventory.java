/**
 * 
 */
package mmb.world.inventory.storage;

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.ainslec.picocog.PicoWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import mmb.beans.Saver;
import mmb.debug.Debugger;
import mmb.world.crafting.RecipeOutput;
import mmb.world.inventory.Inventory;
import mmb.world.inventory.ItemLoader;
import mmb.world.inventory.ItemRecord;
import mmb.world.inventory.ItemStack;
import mmb.world.inventory.ItemLoader.ItemTarget;
import mmb.world.items.ItemEntry;
import monniasza.collects.Collects;
import monniasza.collects.selfset.HashSelfSet;
import monniasza.collects.selfset.SelfSet;

/**
 * @author oskar
 *
 */
public class SimpleInventory implements Inventory, Saver<JsonNode>{
	private static final Debugger debug = new Debugger("INVENTORIES");
	@Nonnull private SelfSet<ItemEntry, Node> contents = new HashSelfSet<>();
	private double volume = 0;
	private double capacity = 2;
	private final class Node implements ItemRecord{
		private Node(int amount, ItemEntry type) {
			this.amount = amount;
			this.type = type;
		}
		protected int amount;
		@Nonnull public final ItemEntry type;
		@Override
		public ItemEntry item() {
			return type;
		}
		@Override
		public int insert(int n) {
			if(n < 0) throw new IllegalArgumentException("Inserting negative items. Use extract() instead.");
			if(n == 0) return 0;
			
			double remainVolume = capacity - volume;
			int remainAmount = (int) (remainVolume / type.volume());
			int result = Math.min(n, remainAmount);
			
			volume += type.volume(result);
			amount += result;
			
			if(amount == result) OV_add(type);
			OV_insert(new ItemStack(type, result));
			OV_update(new ItemStack(type, amount));
			return result;
		}
		@Override
		public int extract(int n) {
			if(n < 0) return -insert(n);
			if(n == 0) return 0;
			
			String title = type.title();
			//debug.printl("PASS: Requesting extraction on "+title+" in amount "+n);
			//debug.printl("Remaining amount: "+amount);
			
			int result = Math.min(n, amount);
			
			volume -= type.volume(result);
			amount -= result;
			
			if(amount == 0) OV_remove(type);
			OV_extract(new ItemStack(type, result));
			OV_update(new ItemStack(type, amount));
			return result;
		}
		@Override
		public int amount() {
			return amount;
		}
		@Override
		public Inventory inventory() {
			return SimpleInventory.this;
		}
	}
	/**
	 * Creates an inventory with same contents as original
	 * @param inv source inventory
	 */
	public SimpleInventory(Inventory inv) {
		capacity = inv.capacity();
		for(ItemRecord irecord: inv) {
			insert(irecord.item(), irecord.amount());
		}
	}

	/**
	 * Creates an empty inventory
	 */
	public SimpleInventory() {
		// no initialization here
	}

	@Override
	public ItemRecord get(ItemEntry entry) {
		ItemRecord result = contents.get(entry);
		if(result == null) {
			Node node = new Node(0, entry);
			contents.add(node);
			return node;
		}
		return result;
	}

	@Override
	public int insert(ItemEntry ent, int amount) {
		Node node = contents.get(ent);
		if(node == null) {
			node = new Node(0, ent);
			contents.add(node);
		}
		return node.insert(amount);
	}

	@Override
	public int extract(ItemEntry ent, int amount) {
		//debug.printl("Requesting extraction on "+ent.title()+" in amount "+amount);
		Node node = contents.get(ent);
		if(node == null) return 0;
		return node.extract(amount);
	}

	@Override
	public double capacity() {
		return capacity;
	}
	
	/**
	 * Sets capacity of this inventory
	 * @param capacity new capacity
	 * @return this
	 */
	public @Nonnull SimpleInventory setCapacity(double capacity) {
		this.capacity = capacity;
		return this;
	}

	@Override
	public double volume() {
		return volume;
	}

	@SuppressWarnings("null")
	@Override
	public Iterator<ItemRecord> iterator() {
		return Collects.downcastIterator(contents.iterator());
	}

	@Override
	public void load(@Nullable JsonNode data) {	
		if(data == null) return;
		//Prepare
		contents.clear();
		capacity = 2;
		volume = 0;
		
		if(!data.isArray()) {
			debug.printl("Expected array, got "+data.getNodeType());
		}
		//Ensure it is an array
		ArrayNode arr = (ArrayNode) data;
		
		if(arr.size() == 0) {
			debug.printl("Empty data array");
			return;
		}
		ItemLoader.load(arr, new ItemTarget() {
			@Override
			public void addItem(ItemEntry ent, int amt) {
				Node existing = contents.get(ent); //get existing node if it exists
				if(existing == null) { //A new entry
					Node irecord = new Node(amt, ent);
					volume += irecord.volume();
					contents.add(irecord);
				}else{ //It already exists
					existing.insert(amt);
				}
			}
			@Override
			public void setCapacity(double cap) {
				capacity = cap;
			}
		});
	}

	@Override
	public @Nonnull JsonNode save() {
		return ItemLoader.save(contents, capacity); //return the saved inventory
	}

	@Override
	public ItemRecord nget(ItemEntry entry) {
		return contents.get(entry);
	}

	@Override
	public boolean isEmpty() {
		return contents.isEmpty();
	}

	@Override
	public int size() {
		return contents.size();
	}
	
	@Override
	public String toString() {
		PicoWriter writer = new PicoWriter();
		writer.writeln("Volume: "+volume);
		writer.writeln("Capacity: "+capacity);
		writer.writeln("Size: "+contents.size());
		writer.indentRight();
		for(ItemRecord record: this) {
			writer.writeln(record.amount()+" � "+record.item());
		}
		return writer.toString();
	}

	@Override
	public void clear() {
		volume = 0;
		contents.clear();
	}
	/**
	 * Replaces all inventory contents and settings with given inventory
	 * @param in
	 */
	public void set(Inventory in) {
		capacity = in.capacity();
		clear();
		for(ItemRecord irecord: in) {
			double vol = irecord.volume();
			contents.add(new Node(irecord.amount(), irecord.item()));
			volume += vol;
		}
	}
	
	//Auxiliary methods for ListenableInventory and other programmable inventories
	/**
	 * An overridable method for use in listenable inventories
	 * @param stk type and amount of items inserted
	 */
	protected void OV_insert(ItemStack stk) {
		//to be overridden
	} 
	/**
	 * An overridable method for use in listenable inventories
	 * @param stk type and amount of items iremoved
	 */
	protected void OV_extract(ItemStack stk) {
		//to be overridden
	}
	/**
	 * An overridable method for use in listenable inventories
	 * @param stk type and new amount of items for the specified type
	 */
	protected void OV_update(ItemStack stk) {
		//to be overridden
	}
	/**
	 * An overridable method for use in listenable inventories
	 * @param item an item removed completely
	 */
	protected void OV_remove(ItemEntry item) {
		//to be overridden
	}
	/**
	 * An overridable method for use in listenable inventories
	 * @param item a new item added to the inventory
	 */
	protected void OV_add(ItemEntry item) {
		//to be overridden
	}

	@Override
	public int bulkInsert(RecipeOutput ent, int amount) {
		double remain = remainVolume();
		double task = ent.outVolume();
		int tasksInVolume = (int)(remain/task);
		int max = Math.min(amount, tasksInVolume);
		for(Entry<ItemEntry> entry: ent.getContents().object2IntEntrySet()) {
			insert(entry.getKey(), entry.getIntValue()*max);
		}
		debug.printl("score: "+max);
		return max;
	}
}
