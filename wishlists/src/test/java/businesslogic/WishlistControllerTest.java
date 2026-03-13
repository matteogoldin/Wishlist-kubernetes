package businesslogic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import daos.WishlistDAO;
import model.Item;
import model.Wishlist;
import view.WishlistView;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wishlist Controller Tests")
class WishlistControllerTest {

    @Mock
    private WishlistView view;

    @Mock
    private WishlistDAO wlDao;

    @InjectMocks
    private WishlistController controller;

    private static final String ERROR_STRING = "Error: please try again or try to refresh";

    @Nested
    @DisplayName("Add Wishlist Tests")
    class AddingWishlistTests {

        @Test
        @DisplayName("Wishlist correctly added")
        void wlCorrectlyAdded() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            controller.addWishlist(wl);
            verify(wlDao).add(wl);
            verify(view).showAllWLs(controller.getWlList());
            assertAll(() -> assertThat(controller.getWlList()).hasSize(1),
                    () -> assertThat(controller.getWlList().get(0)).isEqualTo(wl));
        }

        @Test
        @DisplayName("Adding wishlist with the same name shows error")
        void addingWLWithSameNameShowError() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Wishlist wl2 = new Wishlist("Christmas", "Christmas gifts");
            Wishlist wl_dup = new Wishlist("Birthday", "Mum birthday gifts");
            doNothing().when(wlDao).add(isA(Wishlist.class));
            controller.addWishlist(wl);
            controller.addWishlist(wl2);
            controller.addWishlist(wl_dup);
            assertThat(controller.getWlList()).hasSize(2);
            verify(view, times(3)).showAllWLs(controller.getWlList());
            verify(view).showError("Wishlist Birthday already exists");
        }

        @Test
        @DisplayName("Database errors while adding a wishlist shows error")
        void addWLDatabaseErrorsShowsError() {
        	Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
        	doThrow(RuntimeException.class).when(wlDao).add(wl);
        	controller.addWishlist(wl);
        	assertThat(controller.getWlList()).isEmpty();
        	verify(view).showError(ERROR_STRING);
        	 verify(view).showAllWLs(controller.getWlList());
        }
    }

    @Nested
    @DisplayName("Remove Wishlist Tests")
    class RemovingWishlistTests {

        @Test
        @DisplayName("Wishlist correctly removed")
        void wlCorrectlyRemoved() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            controller.getWlList().add(wl);
            controller.removeWishlist(wl);
            verify(wlDao).remove(wl);
            assertThat(controller.getWlList()).isEmpty();
            verify(view).showAllWLs(controller.getWlList());
        }

        @Test
        @DisplayName("Removing a wishlist not persisted and not in the list does nothing")
        void removingAWLNotPersistedAndNotInTheListDoNothing() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            doNothing().when(wlDao).remove(wl);
            controller.removeWishlist(wl);
            verify(view).showAllWLs(controller.getWlList());
            assertThat(controller.getWlList()).isEmpty();
        }

        @Test
        @DisplayName("Removing a wishlist not persisted but in the wishlists list removes it from the list")
        void removingAWLNotPersistedButInTheWlListRemovesItFromTheList() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            doNothing().when(wlDao).remove(wl);
            controller.getWlList().add(wl);
            controller.removeWishlist(wl);
            verify(view).showAllWLs(controller.getWlList());
            assertThat(controller.getWlList()).isEmpty();
        }

        @Test
        @DisplayName("Other exception while removing a wishlist are managed")
        void otherExceptionWhileRemovingAWLAreManaged() {
            doThrow(new RuntimeException()).when(wlDao).remove(isA(Wishlist.class));
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            controller.removeWishlist(wl);
            assertThat(controller.getWlList()).isEmpty();
            verify(view).showError(ERROR_STRING);
        }
    }

    @Nested
    @DisplayName("Add Item Tests")
    class AddingItemTests {

        @Test
        @DisplayName("Item correctly added to wishlist")
        void itemCorrectlyAddedToWL() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            controller.getWlList().add(wl);
            controller.addItemToWishlist(item, wl);
            assertThat(wl.getItems()).containsOnly(item);
            verify(view).showAllItems(wl);
        }

        @Test
        @DisplayName("Correctly add duplicated item to different wishlist")
        void correctlyAddDuplicatedItemToDifferentWishlist() {
            Wishlist wl1 = new Wishlist("Birthday", "My birthday gifts");
            Wishlist wl2 = new Wishlist("Christmas", "Gift ideas");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            Item item_dup = new Item("Phone", "Samsung Galaxy A52", 300);
            controller.getWlList().add(wl1);
            controller.getWlList().add(wl2);
            controller.addItemToWishlist(item, wl1);
            controller.addItemToWishlist(item_dup, wl2);
            assertAll(() -> assertThat(wl1.getItems()).containsOnly(item),
                    () -> assertThat(wl2.getItems()).containsOnly(item_dup));
            verify(view, times(2)).showAllItems(isA(Wishlist.class));
            verify(wlDao, times(2)).addItem(any(), any());
        }

        @Test
        @DisplayName("Trying to add a duplicated item to the same wishlist shows error")
        void tryingToAddADuplicatedItemToSameWishlistShowError() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            Item item2 = new Item("Wallet", "Leather", 100);
            Item item_dup = new Item("Phone", "Samsung Galaxy A52", 300);
            doNothing().when(wlDao).addItem(isA(Wishlist.class), isA(Item.class));
            controller.getWlList().add(wl);
            controller.addItemToWishlist(item, wl);
            controller.addItemToWishlist(item2, wl);
            controller.addItemToWishlist(item_dup, wl);
            assertThat(wl.getItems()).containsOnly(item, item2);
            verify(wlDao, times(2)).addItem(isA(Wishlist.class), isA(Item.class));
            verify(view, times(3)).showAllItems(wl);
            verify(view).showError("Item Phone is already in the Wishlist");
        }

        @Test
        @DisplayName("Trying to add an item in a wishlist not persisted shows error")
        void tryingToAddAnItemInAWlNotPersistedShowError() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            doThrow(new RuntimeException()).when(wlDao).addItem(isA(Wishlist.class), isA(Item.class));
            controller.addItemToWishlist(item, wl);
            assertThat(wl.getItems()).isEmpty();
            verify(view).showError(ERROR_STRING);
            verify(view).showAllItems(wl);
            verify(wlDao, times(0)).getAllWlItems(wl);
        }

        @Test
        @DisplayName("Other exception while adding an item to a wishlist are managed")
        void otherExceptionWhileAddingAnItemToAWLAreManaged() {
            doThrow(new RuntimeException()).when(wlDao).addItem(isA(Wishlist.class), isA(Item.class));
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            controller.getWlList().add(wl);
            controller.addItemToWishlist(item, wl);
            assertThat(wl.getItems()).isEmpty();
            verify(view).showError(ERROR_STRING);
            verify(wlDao, times(0)).getAllWlItems(wl);
        }
    }

    @Nested
    @DisplayName("Remove Item Tests")
    class RemovingItemTests {

        @Test
        @DisplayName("Correctly removing an item")
        void correctlyRemovingAnItem() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            controller.getWlList().add(wl);
            wl.getItems().add(item);
            controller.removeItemFromWishlist(item, wl);
            assertThat(wl.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Remove an object not persisted but in the list removes the object from the list")
        void removeAnObjectNotPersistedButInTheListRemoveTheObjectFromTheList() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            doNothing().when(wlDao).removeItem(wl, item);
            controller.getWlList().add(wl);
            wl.getItems().add(item);
            controller.removeItemFromWishlist(item, wl);
            assertThat(wl.getItems()).isEmpty();
            verify(view).showAllItems(wl);
            verify(wlDao).removeItem(wl, item);
        }

        @Test
        @DisplayName("Other exceptions while removing an item from a wishlist are managed")
        void otherExceptionWhileRemovingAnItemFromAWLAreManaged() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            doThrow(new RuntimeException()).when(wlDao).removeItem(wl, item);
            controller.getWlList().add(wl);
            wl.getItems().add(item);
            controller.removeItemFromWishlist(item, wl);
            assertThat(wl.getItems()).hasSize(1);
            verify(view).showError(ERROR_STRING);
            verify(wlDao, times(0)).getAllWlItems(wl);
        }
    }

    @Nested
    @DisplayName("Refresh Tests")
    class RefreshTests {

        @Test
        @DisplayName("Refresh wishlists gets wishlists from the DAO and sends them to the view")
        void refreshWishlistsGetWlsFromTheDaoAndSendThemToTheView() {
            controller.refreshWishlists();
            verify(wlDao).getAll();
            verify(view).showAllWLs(controller.getWlList());
        }

        @Test
        @DisplayName("Refresh wishlist update the wishlist list if not up to date")
        void refreshWishlistUpdateTheWishlistsListIfNotUpToDate() {
        	 Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
        	 when(wlDao.getAll()).thenReturn(Arrays.asList(wl));
        	 controller.refreshWishlists();
        	 assertThat(controller.getWlList()).contains(wl);
        	 verify(view).showAllWLs(controller.getWlList());
        }

        @Test
        @DisplayName("Refresh wishlist manages exception from DAO")
        void refreshWishlistManagesExceptionFromDao() {
            when(wlDao.getAll()).thenThrow(new RuntimeException());
            controller.refreshWishlists();
            verify(wlDao).getAll();
            verify(view, times(0)).showAllWLs(controller.getWlList());
            verify(view).showError(ERROR_STRING);
        }

        @Test
        @DisplayName("Refresh items gets items from the DAO and sends them to the view")
        void refreshItemsGetItemsFromTheDaoAndSendThemToTheView() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            when(wlDao.getAllWlItems(wl)).thenReturn(Arrays.asList(item));
            controller.refreshItems(wl);
            assertThat(wl.getItems()).isEqualTo(Arrays.asList(item));
            verify(wlDao).getAllWlItems(wl);
            verify(view).showAllItems(wl);
        }

        @Test
        @DisplayName("Refresh items manages exception from DAO")
        void refreshItemsManagesExceptionFromDao() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            when(wlDao.getAllWlItems(wl)).thenThrow(new RuntimeException());
            controller.refreshItems(wl);
            verify(wlDao).getAllWlItems(wl);
            verify(view, times(0)).showAllItems(wl);
            verify(view).showError(ERROR_STRING);
        }
    }
}


