/**
 * 
 */
package mmb.content.electric;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import mmb.engine.block.BlockEntity;
import mmb.engine.block.BlockEntityType;
import mmb.engine.item.Items;
import mmb.engine.java2d.TexGen;
import mmb.engine.rotate.RotatedImageGroup;
import mmb.engine.settings.GlobalSettings;

/**
 * @author oskar
 * A group of closely related electrical machines
 */
public class ElectricMachineGroup {
	@Nonnull public final List<@Nonnull ElectroMachineType> blocks;
	@Nonnull public final List<@Nonnull BufferedImage> images;
	@Nonnull public final List<@Nonnull RotatedImageGroup> textures;
	private static final VoltageTier[] volts = VoltageTier.values();
	/**
	 * Creates an electric machine group
	 * @param image machine's texture
	 * @param ctor
	 * @param id
	 * @param powermul 
	 */
	@SuppressWarnings("null")
	public ElectricMachineGroup(BufferedImage image, Function<@Nonnull ElectroMachineType, @Nonnull BlockEntity> ctor, String id, double powermul) {
		images = TexGen.generateMachineTextures(image);
		textures = images.stream()
				.map(RotatedImageGroup::create)
				.collect(Collectors.toList());
		IntStream stream = IntStream.range(0, 9);
		blocks = stream.mapToObj(num -> {
			RotatedImageGroup texture = textures.get(num);
			VoltageTier volt = volts[num];
			ElectroMachineType type = new ElectroMachineType(volt, texture, powermul);
			type.title(GlobalSettings.$res1("machine-"+id)+' '+volt.name);
			type.factory(() -> ctor.apply(type));
			type.finish("industry."+id+num);
			return type;
		}).collect(Collectors.toList());
		Items.tagItems("machine-"+id, blocks);
		for(int i = 0; i < 9; i++) {
			ElectroMachineType block = blocks.get(i);
			Items.tagItem("voltage-"+block.volt.name, block);
		}
	}
	public ElectricMachineGroup(BufferedImage image, Function<@Nonnull ElectroMachineType, @Nonnull BlockEntity> ctor, String id) {
		this(image, ctor, id, 1);
	}
	/**
	 * @author oskar
	 * A specialized block type for autogenerated electrical machines
	 */
	public static class ElectroMachineType extends BlockEntityType{
		@Nonnull public final VoltageTier volt;
		@Nonnull public final RotatedImageGroup rig;
		public final double powermul;
		/**
		 * Creates an electric machine type. The texture is set automatically
		 * @param volt voltage tier
		 * @param rig texture
		 */
		public ElectroMachineType(VoltageTier volt, RotatedImageGroup rig) {
			super();
			this.volt = volt;
			this.rig = rig;
			powermul = 1;
			texture(rig.U);
		}
		/**
		 * Creates an electric machine type. The texture is set automatically
		 * @param volt voltage tier
		 * @param rig texture
		 * @param powermul power mutiplier above the base power
		 */
		public ElectroMachineType(VoltageTier volt, RotatedImageGroup rig, double powermul) {
			super();
			this.volt = volt;
			this.rig = rig;
			this.powermul = powermul;
			texture(rig.U);
		}
	}
	/**
	 * Gets a machine with givn index
	 * @param index the machine index
	 * @return the {@code index}-th machine
	 */
	public @Nonnull ElectroMachineType get(int index) {
		return blocks.get(index);
	}
}
