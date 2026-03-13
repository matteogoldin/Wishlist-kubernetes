package daos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.persistence.NoResultException;
import jakarta.persistence.RollbackException;
import model.Item;
import model.Wishlist;
import utils.SQLClient;

@DisplayName("Wishlist DAO Tests")
class WishlistDAOTest {

    private WishlistDAO wDao;
    private SQLClient client;
    private String persistentUnit = "wishlists-pu-test";

    @BeforeEach
    void setup() {
        wDao = new WishlistDAO(persistentUnit);
        client = new SQLClient(persistentUnit);
        client.initEmptyDB();
    }

    @Nested
    @DisplayName("Add Wishlist Tests")
    class AddWishlistTests {

        @Test
        @DisplayName("Wishlist correctly inserted")
        void wishlistCorrectlyInserted() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            wDao.add(wl);
            Wishlist wl_dup = client.findWishlist(wl.getName());
            assertThat(wl).isEqualTo(wl_dup);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Adding a wishlist that already exists raises an exception")
        void addingAWishlistThatAlreadyExistsRaiseAnException() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Wishlist wl_dup = new Wishlist("Birthday", "My mum birthday gifts");
            wDao.add(wl_dup);
            assertThatThrownBy(() -> wDao.add(wl)).isInstanceOf(RollbackException.class);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Other exceptions for add wishlist are managed")
        void otherExceptionAreManaged() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            wDao.getEmf().close();
            assertThatThrownBy(() -> wDao.add(wl)).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Remove Wishlist Tests")
    class RemoveWishlistTests {

        @Test
        @DisplayName("Wishlist correctly removed")
        void wishlistCorrectlyRemoved() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            client.insertWishlist(wl.getName(), wl.getDesc());
            wDao.remove(wl);
            assertThat(client.findWishlist(wl.getName())).isNull();
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Removing a non-persisted wishlist does not throw an exception")
        void removingANonPersistedWishlist() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            assertDoesNotThrow(() -> wDao.remove(wl));
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Removing a wishlist removes also its items")
        void removingAWishlistRemovesAlsoItsItems() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            client.insertWishlist(wl.getName(), wl.getDesc());
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            wl.getItems().add(item);
            client.mergeWishlist(wl);
            assertThat(client.findItem(wl.getName(), item.getName())).isNotNull();
            wDao.remove(wl);
            String wl_name = wl.getName();
            String item_name = item.getName();
            assertThatThrownBy(() -> client.findItem(wl_name, item_name)).isInstanceOf(NoResultException.class);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("Get All Wishlists Tests")
    class GetAllWishlistsTests {

        @Test
        @DisplayName("Get all when the database is empty returns an empty list")
        void getAllWhenDatabaseIsEmptyReturnEmptyList() {
            assertThat(wDao.getAll()).isEmpty();
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Get all when the database is not empty returns a not empty list")
        void getAllWhenDatabaseIsNotEmptyReturnANotEmptyList() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            client.insertWishlist(wl.getName(), wl.getDesc());
            assertThat(wDao.getAll()).hasSize(1);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Get all correctly retrieves all the wishlists")
        void getAllCorrectlyRetrieveAllTheWishlists() {
            Wishlist wl1 = new Wishlist("Birthday", "My birthday gifts");
            Wishlist wl2 = new Wishlist("Christmas", "Gift ideas");
            client.insertWishlist(wl1.getName(), wl1.getDesc());
            client.insertWishlist(wl2.getName(), wl2.getDesc());
            List<Wishlist> wlList = wDao.getAll();
            assertAll(() -> assertThat(wlList).hasSize(2),
                      () -> assertThat(wlList).contains(wl1),
                      () -> assertThat(wlList).contains(wl2));
            assertThat(wDao.getEm().isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("Find By Id Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Find by Id correctly retrieves a wishlist")
        void findByIdCorrectlyRetrieveAWishlist() {
            Wishlist wl1 = new Wishlist("Birthday", "My birthday gifts");
            client.insertWishlist(wl1.getName(), wl1.getDesc());
            assertThat(wDao.findById(wl1.getName())).isEqualTo(wl1);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Find by Id returns null if wishlist is not persisted")
        void findByIdReturnNullIfWishlistIsNotPersisted() {
            assertThat(wDao.findById("Birthday")).isNull();
            assertThat(wDao.getEm().isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("Add Item Tests")
    class AddItemTests {

        @Test
        @DisplayName("Add item adds an item to a wishlist")
        void addItemAddAnItemToAWishlist() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            client.insertWishlist(wl.getName(), wl.getDesc());
            String wl_name = wl.getName();
            String item_name = item.getName();
            assertThatThrownBy(() -> client.findItem(wl_name, item_name)).isInstanceOf(NoResultException.class);
            wDao.addItem(wl, item);
            assertThat(client.findItem(wl_name, item_name)).isEqualTo(item);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Add item can raise an exception")
        void addItemCanRaiseException() {
            Wishlist wl = null;
            Item item = null;
            assertThatThrownBy(() -> wDao.addItem(wl, item)).isInstanceOf(RuntimeException.class);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("Remove Item Tests")
    class RemoveItemTests {

        @Test
        @DisplayName("Remove item removes an item from the wishlist")
        void removeItemRemovesItemFromTheWishlist() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            client.insertWishlist(wl.getName(), wl.getDesc());
            Item item = new Item("Phone", "Samsung Galaxy A52", 300);
            wl.getItems().add(item);
            client.insertItem(wl.getName(), item.getName(), item.getDesc(), item.getPrice());
            assertThat(client.findItem(wl.getName(), item.getName())).isNotNull();
            wDao.removeItem(wl, item);
            String wl_name = wl.getName();
            String item_name = item.getName();
            assertThatThrownBy(() -> client.findItem(wl_name, item_name)).isInstanceOf(NoResultException.class);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Remove item can raise an exception")
        void removeItemCanRaiseException() {
            Wishlist wl = null;
            Item item = null;
            assertThatThrownBy(() -> wDao.removeItem(wl, item)).isInstanceOf(RuntimeException.class);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("Get All Item Tests")
    class GetAllItemTests {

        @Test
        @DisplayName("Get all wishlist items returns all the items associated with a wishlist")
        void getAllWlItemsReturnsAllTheItemsAssociatedToAList() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            client.insertWishlist(wl.getName(), wl.getDesc());
            Item item1 = new Item("Phone", "Samsung Galaxy A52", 300);
            Item item2 = new Item("Wallet", "D&G", 100);
            client.insertItem(wl.getName(), item1.getName(), item1.getDesc(), item1.getPrice());
            client.insertItem(wl.getName(), item2.getName(), item2.getDesc(), item2.getPrice());
            List<Item> itList = wDao.getAllWlItems(wl);
            assertThat(itList).containsExactly(item1, item2);
            assertThat(wDao.getEm().isOpen()).isFalse();
        }

        @Test
        @DisplayName("Get all wishlist items on a non-persisted wishlist returns an empty list")
        void getAllWlItemsOnANonPersistedWLReturnsAnEmptyList() {
            Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
            assertThat(wDao.getAllWlItems(wl)).isEmpty();
            assertThat(wDao.getEm().isOpen()).isFalse();
        }
    }
}
