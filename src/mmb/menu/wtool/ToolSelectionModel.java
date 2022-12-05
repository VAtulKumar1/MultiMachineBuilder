/**
 * 
 */
package mmb.menu.wtool;

import javax.annotation.Nullable;

import mmb.menu.world.window.WorldWindow;
import mmbeng.debug.Debugger;
import mmbeng.worlds.world.Player;

/**
 * @author oskar
 *
 */
public class ToolSelectionModel {
	private WindowTool toolIL;
	private WindowTool toolTL;
	@Nullable public final WorldWindow window;
	private static final Debugger debug = new Debugger("TOOL SELECTOR");
	public ToolSelectionModel(@Nullable WorldWindow window) {
		this.window = window;
	}

	public void toolSelectedItemList(@Nullable WindowTool tool) {
		debug.printl("IL tool: "+tool);
		toolIL = tool;
		resetTools();
	}
	public void toolSelectedToolList(@Nullable WindowTool tool) {
		debug.printl("TL tool: "+tool);
		toolTL = tool;
		resetTools();
	}
	
	private void resetTools() {
		debug.printl("Reset tools");
		WorldWindow window0 = window;
		if(toolTL == null) {
			setTool(toolIL);
		} else if(window0 != null) {
			Player p = window0.getPlayer();
			if(!(p == null || p.isCreative())) { //true
				if(toolIL == null) {
					setTool(window0.std);
				}else {
					setTool(toolIL);
				}
			}else {
				setTool(toolTL);
			}
		}else {
			setTool(toolTL);
		}
	}

	private WindowTool tool;
	public void setTool(@Nullable WindowTool newTool) {
		if(tool == newTool) return;
		if(tool != null) {
			tool.deselected();
			debug.printl("Old tool: " + tool.id());
		}else {
			debug.printl("Old tool: null");
		}
		if(newTool != null) {
			newTool.selected();
			if(window != null) newTool.setWindow(window);
			debug.printl("New tool: " + newTool.id());
		}else {
			debug.printl("New tool: null");
			return;
		}
		tool = newTool;
	}
	public WindowTool getTool() {
		return tool;
	}
}
