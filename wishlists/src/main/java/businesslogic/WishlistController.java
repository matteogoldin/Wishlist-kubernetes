package businesslogic;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import daos.WishlistDAO;
import model.Item;
import model.Wishlist;
import view.WishlistView;

public class WishlistController {
	private WishlistView view;
	private WishlistDAO wlDao;
	private List<Wishlist> wlList;

	private static final Logger LOGGER = LogManager.getLogger(WishlistController.class);
	private static final String LOG_ERROR = "Database error";
	private static final String ERROR_STRING = "Error: please try again or try to refresh";

	public WishlistController(WishlistView view, WishlistDAO wlDao) {
		this.view = view;
		this.wlDao = wlDao;
		wlList = new ArrayList<>();
	}

	public void addWishlist(Wishlist wl) {
		if(wlList.stream().noneMatch(w -> w.getName().equals(wl.getName()))) {
			try {
				wlDao.add(wl);
				wlList.add(wl);
				LOGGER.info(() -> String.format("Wishlist %s correctly inserted", wl.getName()));
			} catch (RuntimeException e) {
				view.showError(ERROR_STRING);
				LOGGER.error(LOG_ERROR);
			}
		} else {
			String error = String.format("Wishlist %s already exists", wl.getName());
			view.showError(error);
			LOGGER.error(error);
		}
		view.showAllWLs(wlList);
	}

	public void removeWishlist(Wishlist wl) {
		try {
			wlDao.remove(wl);
			wlList.remove(wl);
			LOGGER.info(() -> String.format("Wishlist %s correctly removed", wl.getName()));
		} catch (RuntimeException e) {
			view.showError(ERROR_STRING);
			LOGGER.error(LOG_ERROR);
		}
		view.showAllWLs(wlList);
	}

	public void addItemToWishlist(Item item, Wishlist wl) {
		if(wl.getItems().stream().noneMatch(it -> it.getName().equals(item.getName()))) {
			try {
				wlDao.addItem(wl, item);
				wl.addItem(item);
				LOGGER.info(() -> String.format("Item %s correctly added to Wishlist %s", item.getName(), wl.getName()));
			} catch (RuntimeException e) {
				view.showError(ERROR_STRING);
				LOGGER.error(LOG_ERROR);
			}
		} else {
			String error = String.format("Item %s is already in the Wishlist", item.getName());
			view.showError(error);
			LOGGER.error(error);
		}
		view.showAllItems(wl);

	}

	public void removeItemFromWishlist(Item item, Wishlist wl) {
		try {
			wlDao.removeItem(wl, item);
			wl.removeItem(item);
			LOGGER.info(() -> String.format("Item %s correctly removed from Wishlist %s", item.getName(), wl.getName()));
		} catch (RuntimeException e) {
			view.showError(ERROR_STRING);
			LOGGER.error(LOG_ERROR);
		}
		view.showAllItems(wl);
	}

	public void refreshWishlists() {
		try {
			wlList = wlDao.getAll();
			view.showAllWLs(wlList);
		} catch (RuntimeException e) {
			view.showError(ERROR_STRING);
		}
	}

	public void refreshItems(Wishlist wl) {
		try {
			wl.setItems(wlDao.getAllWlItems(wl));
			view.showAllItems(wl);
		} catch (RuntimeException e) {
			view.showError(ERROR_STRING);
		}
	}

	List<Wishlist> getWlList() {
		return wlList;
	}

}
