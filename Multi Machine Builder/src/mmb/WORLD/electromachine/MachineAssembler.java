/**
 * 
 */
package mmb.WORLD.electromachine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mmb.BEANS.BlockActivateListener;
import mmb.WORLD.contentgen.ElectricMachineGroup.ElectroMachineType;
import mmb.WORLD.crafting.helper.ComplexCatalyzedItemProcessHelper;
import mmb.WORLD.crafting.recipes.ComplexCatalyzedProcessingRecipeGroup;
import mmb.WORLD.electric.Electricity;
import mmb.WORLD.gui.window.WorldWindow;
import mmb.WORLD.inventory.io.InventoryReader;
import mmb.WORLD.inventory.io.InventoryWriter;
import mmb.WORLD.inventory.storage.SingleItemInventory;
import mmb.WORLD.items.ItemEntry;
import mmb.WORLD.rotate.RotatedImageGroup;
import mmb.WORLD.rotate.Side;
import mmb.WORLD.worlds.MapProxy;
import mmb.WORLD.worlds.world.World;

/**
 * @author oskar
 *
 */
public class MachineAssembler extends CommonMachine implements BlockActivateListener{
	@Override
	public RotatedImageGroup getImage() {
		return type.rig;
	}

	@Nonnull private final ComplexCatalyzedItemProcessHelper helper;
	@Nonnull public final ComplexCatalyzedProcessingRecipeGroup group;
	/**
	 * The current catalyst
	 */
	@Nonnull public final SingleItemInventory catalyst = new SingleItemInventory();
	
	//Constructor
	public MachineAssembler(ElectroMachineType type, ComplexCatalyzedProcessingRecipeGroup group) {
		super(type);
		this.recipes = group;
		helper = new ComplexCatalyzedItemProcessHelper(group, in, out0, 1000, elec, type.volt, () -> catalyst.getContents());
		this.group = group;
	}
	
	//Block I/O
	@Override
	public InventoryReader getOutput(Side s) {
		return out0.createReader();
	}
	@Override
	public InventoryWriter getInput(Side s) {
		return in.createWriter();
	}

	@Override
	public Electricity getElectricalConnection(Side s) {
		return Electricity.insertOnly(elec);
	}

	//Save/load
	@Override
	protected void save1(ObjectNode node) {
		helper.save(node);
		JsonNode bat = elec.save();
		node.set("energy", bat);
		node.set("in", in.save());
		node.set("out", out0.save());
		node.put("pass", pass);
		node.put("autoex", autoExtract);
		node.set("catalyst", ItemEntry.saveItem(catalyst.getContents()));
	}

	@Override
	protected void load1(ObjectNode node) {
		helper.load(node);
		JsonNode bat = node.get("energy");
		elec.load(bat);
		in.load(node.get("in"));
		in.setCapacity(2);
		out0.load(node.get("out"));
		out0.setCapacity(2);
		JsonNode passNode = node.get("pass");
		if(passNode != null) pass = passNode.asBoolean();
		JsonNode autoNode = node.get("autoex");
		if(autoNode != null) autoExtract = autoNode.asBoolean();
		catalyst.setContents(ItemEntry.loadFromJson(node.get("catalyst")));
	}

	public final ComplexCatalyzedProcessingRecipeGroup recipes;
	//GUI
	@Override
	public void click(int blockX, int blockY, World map, @Nullable WorldWindow window, double partX, double partY) {
		if(window == null) return;
		if(tab != null) return;
		tab = new MachineTab(this, window);
		window.openAndShowWindow(tab, group.title+' '+type.volt.name);
		helper.refreshable = tab;
		tab.refreshProgress(0, null);
	}

	@Override protected void onTick0(MapProxy map) {
		helper.cycle();
	}

	@Override
	public ComplexCatalyzedProcessingRecipeGroup recipes() {
		return group;
	}

	@Override
	protected CommonMachine copy0() {
		MachineAssembler copy = new MachineAssembler(type, group);
		copy.catalyst.setContents(catalyst.getContents());
		copy.helper.set(helper);
		return copy;
	}

	@Override
	public String machineName() {
		return group.title;
	}

	@Override
	public SingleItemInventory catalyst() {
		return catalyst;
	}
	
	
}
