package app;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import businesslogic.WishlistController;
import daos.WishlistDAO;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import view.WishlistSwingView;

@Command(mixinStandardHelpOptions = true)
public class WishlistApp implements Callable<Void>{
	@Option(names = { "--persistence-unit" }, description = "Persistence Unit name")
	private String persistenceUnit = "wishlists-pu";

	private static final Logger LOGGER = LogManager.getLogger(WishlistApp.class);

	public static void main(String[] args) {
		new CommandLine(new WishlistApp()).execute(args);
	}

	@Override
	public Void call() {
		EventQueue.invokeLater(() -> {
			try {
				WishlistSwingView view = new WishlistSwingView();
				WishlistDAO dao = new WishlistDAO(persistenceUnit);
				WishlistController controller = new WishlistController(view, dao);
				view.setController(controller);
				view.setVisible(true);
				controller.refreshWishlists();
			} catch (Exception e) {
				LOGGER.error("Error occurs starting the application");
			}
		});
		return null;
	}
}
