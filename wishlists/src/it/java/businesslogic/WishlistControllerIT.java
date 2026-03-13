package businesslogic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import daos.WishlistDAO;
import model.Item;
import model.Wishlist;
import view.WishlistView;

class WishlistControllerIT {
	@Mock
	private WishlistView view;

	private WishlistDAO wlDao;

	private WishlistController controller;

	private AutoCloseable closeable;

	@BeforeEach
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		wlDao = new WishlistDAO("wishlists-pu-it");
		controller = new WishlistController(view, wlDao);
		for (Wishlist wl : wlDao.getAll())
			wlDao.remove(wl);
	}

	@AfterEach
	public void releaseMocks() throws Exception {
		wlDao.getAll().forEach(wl -> wlDao.remove(wl));
		closeable.close();
	}

	@Test
	void wlCorrectlyAdded() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		controller.addWishlist(wl);
		assertThat(controller.getWlList()).containsExactly(wl);
		verify(view).showAllWLs(controller.getWlList());
	}

	@Test
	void wlCorrectlyRemoved() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		controller.addWishlist(wl);
		controller.removeWishlist(wl);
		assertThat(controller.getWlList()).isEmpty();
		verify(view, times(2)).showAllWLs(any());
	}

	@Test
	void itemCorrectlyAddedToAWL() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		controller.addWishlist(wl);
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		controller.addItemToWishlist(item, wl);
		assertThat(wlDao.getAllWlItems(wl)).containsExactly(item);
	}

	@Test
	void itemCorrectlyRemovedFromAWL() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		controller.addWishlist(wl);
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		controller.addItemToWishlist(item, wl);
		assertThat(wlDao.getAllWlItems(wl)).containsExactly(item);
		assertThat(wlDao.getAllWlItems(wl)).contains(item);
		controller.removeItemFromWishlist(item, wl);
		assertThat(wlDao.getAllWlItems(wl)).isEmpty();
	}

	@Test
	void refreshWishlistsCorrectlyRetrieveAllTheWishlistsPersisted() {
		Wishlist wl1 = new Wishlist("Birthday", "My birthday gifts");
		Wishlist wl2 = new Wishlist("Christmas", "Gift ideas");
		wlDao.add(wl1);
		wlDao.add(wl2);
		assertThat(controller.getWlList()).isEmpty();
		controller.refreshWishlists();
		assertThat(controller.getWlList()).containsExactly(wl1, wl2);
	}

	@Test
	void refreshWishlistItemsCorrectlyRetrieveAllTheItems() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		controller.addWishlist(wl);
		Item item1 = new Item("Phone", "Samsung Galaxy A52", 300);
		Item item2 = new Item("Wallet", "D&G", 200);
		controller.addItemToWishlist(item1, wl);
		controller.addItemToWishlist(item2, wl);
		wl.getItems().clear();
		controller.refreshItems(wl);
		assertThat(wl.getItems()).containsExactly(item1, item2);
	}

}
