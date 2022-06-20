/**
 * 
 */
package mmb.WORLD.texture;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mmb.WORLD.block.BlockEntry;

/**
 * @author oskar
 * Draws a block using teture
 */
public class TextureDrawer implements BlockDrawer {
	/**
	 * The image which is drawn
	 */
	public final BufferedImage img;
	private int LOD = -1;
	@Nonnull private final Icon icon;
	/**
	 * @param img the image to be drawn
	 */
	public TextureDrawer(BufferedImage img) {
		this.img = img;
		icon = new ImageIcon(img);
	}
	@Override
	public void draw(BlockEntry ent, int x, int y, Graphics g, int w, int h) {
		g.drawImage(img, x, y, w, h, null);
	}
	@Override
	public Icon toIcon() {
		return icon;
	}
	@Override
	public int LOD() {
		if(LOD < 0) 
			LOD = LODs.calcLOD(img);
		return LOD;
	}
}
