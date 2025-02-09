package mmb.menu.main;

import static mmb.engine.settings.GlobalSettings.*;

import java.awt.*;

import javax.swing.*;

import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.awt.Desktop;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;

import mmb.NN;
import mmb.engine.debug.Debugger;
import mmb.engine.gl.HalfVecTest;
import mmb.engine.mods.Mods;
import mmb.engine.settings.GlobalSettings;
import mmb.engine.settings.Settings;
import mmb.menu.FullScreen;
import mmb.menu.MMBFrame;
import mmb.menu.MenuHelper;
import mmb.menu.components.BoundCheckBoxMenuItem;

/**
 * A singleton main menu class
 * @author oskar
 */
public class MainMenu extends MMBFrame {
	private static final long serialVersionUID = -7953512837841781519L;
	private static final Debugger debug = new Debugger("Main menu");
	
	//ToolKit API
	/** The singleton instance of main menu*/
	@NN public static final MainMenu INSTANCE = new MainMenu();
	/**
	 * Add a main menu tab
	 * @param tab tab contents
	 * @param name tab name
	 */
	public void addTab(Component tab, String name) {
		tabbedPane.add(name, tab);
	}
	/**
	 * Add a main menu toolbar entry
	 * @param menu {@link JMenu} to add
	 */
	public void addToolBarEntry(JMenu menu) {
		menuBar.add(menu);
	}
	protected JTabbedPane tabbedPane;
	
	
	private final JPanel contentPane;
	
	//Various webistes
	/** The website for this game */
	public static final String GITHUB = "https://multimachinebuilder.github.io";
	
	private JButton btnExit;
	private JLabel timerLBL;
	private JButton prank;
	
	/** Launch the application. */
	public static void create() {
		Settings.addSettingBool("fullscreen", false, FullScreen.isFullScreen);		
		FullScreen.setWindow(INSTANCE);
	}

	private MainMenu() {
		debug.printl("MainMenu created");
		setTitle("MultiMachineBuilder - "+Mods.mods.size()+" mods");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setIconImage(MMBFrame.GEAR);
		
		setJMenuBar(menuBar);
		menuBar.add(mnNewMenu);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel aside = new JPanel();
		contentPane.add(aside, BorderLayout.WEST);
		BoxLayout layout = new BoxLayout(aside, BoxLayout.Y_AXIS);
		aside.setLayout(layout);
		
		JButton btnWebsite = new JButton($res("cgui-website"));
		btnWebsite.setToolTipText("Open this game's website");
		btnWebsite.setModel(MenuHelper.btnmWeb);
		aside.add(btnWebsite);
		
		btnExit = new JButton($res("cgui-exit"));
		btnExit.setBackground(Color.RED);
		btnExit.setToolTipText("Exit the game");
		btnExit.addActionListener(e -> System.exit(0));
		aside.add(btnExit);
		
		prank = new JButton($res("AprilFoolsGarblerTooltip"));
		prank.setBackground(Color.YELLOW);
		prank.setToolTipText($res("AprilFoolsGarblerPrank"));
		prank.addActionListener(e -> {
			garbler.setValue(true);
			Thread thread = new Thread(HalfVecTest::run);
			thread.start();
		});
		aside.add(prank);
		
		timerLBL = new JLabel("Current time goes here");
		timerLBL.setToolTipText("The current time");
		aside.add(timerLBL);
		java.util.Timer timer = new java.util.Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				refreshTime();
			}
		}, 0, 1000);
		Runtime.getRuntime().addShutdownHook(new Thread(timer::cancel));

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab($res("cgui-saves"), null, PanelSaves.INSTANCE, null);
		tabbedPane.addTab($res("cgui-mods"), null, PanelMods.INSTANCE, null);
		tabbedPane.addTab($res("cgui-settings"), null, new PanelSettings(), null);
		tabbedPane.addTab($res("cgui-shop"), new PanelShop());
	}
	
	private void refreshTime() {
		GregorianCalendar date = new GregorianCalendar();
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH);
		int day = date.get(Calendar.DAY_OF_MONTH);
		int hour = date.get(Calendar.HOUR);
		int min = date.get(Calendar.MINUTE);
		int sec = date.get(Calendar.SECOND);
		
		StringBuilder message = new StringBuilder()
				.append(year)
				.append('/')
				.append(month)
				.append('/')
				.append(day)
				.append(' ');
		if(hour < 10) message.append('0');
		message.append(hour).append(':');
		if(min < 10) message.append('0');
		message.append(min).append(':');
		if(sec < 10) message.append('0');
		message.append(sec);
		timerLBL.setText(message.toString());
	}
	@Override
	public void destroy() {
		//unused
	}
}
