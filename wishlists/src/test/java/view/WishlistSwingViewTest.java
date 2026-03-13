package view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import businesslogic.WishlistController;
import model.Item;
import model.Wishlist;

@RunWith(GUITestRunner.class)
public class WishlistSwingViewTest extends AssertJSwingJUnitTestCase {
	private FrameFixture window;

	@Mock
	private WishlistController controller;
	private AutoCloseable closeable;
	private WishlistSwingView view;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			view = new WishlistSwingView();
			view.setController(controller);
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();
	}

	@After
	public void OnTearDown() throws Exception {
		closeable.close();
	}

	@Test
	@GUITest
	public void testInitialStateOfComponents() {
		window.label("lblWL");
		window.list("listWL");
		window.button("btnAddWL").requireEnabled();
		window.button("btnRemoveWL").requireDisabled();
		window.list("listItem");
		window.button("btnRefresh").requireEnabled();
		window.label("lblItem");
		window.button("btnAddItem").requireDisabled();
		window.button("btnRemoveItem").requireDisabled();
		window.label("lblError");
		window.label("lblWLDesc");
		window.label("lblItemDesc");
		window.scrollPane("scrollPane1");
		window.scrollPane("scrollPane2");
	}

	@Test
	@GUITest
	public void wlSelectedInListWLEnablesRemoveButtonsAndShowItems() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		wl.getItems().add(item);
		GuiActionRunner.execute(() -> view.getListWLModel().addElement(wl));
		window.list("listWL").selectItem(0);
		window.button("btnRemoveWL").requireEnabled();
		window.button("btnAddItem").requireEnabled();
		assertThat(window.list("listItem").valueAt(0)).isEqualTo(wl.getItems().get(0).toString());
		window.label("lblItem").requireText("Wishes in Birthday:");
		window.list("listWL").clearSelection();
		window.button("btnRemoveWL").requireDisabled();
		window.button("btnAddItem").requireDisabled();
		window.list("listItem").requireItemCount(0);
		window.label("lblItem").requireText("Select a Wishlist...");
	}

	@Test
	@GUITest
	public void itemSelectedInListItemEnablesRemoveButtons() {
		GuiActionRunner.execute(() -> view.getListItemModel().addElement(new Item("Phone", "Samsung Galaxy A52", 300)));
		window.button("btnRemoveItem").requireDisabled();
		window.list("listItem").selectItem(0);
		window.button("btnRemoveItem").requireEnabled();
	}

	@Test
	@GUITest
	public void showAllWLDisplayTheWLsInTheListWL() {
		List<Wishlist> wls = new ArrayList<>();
		Wishlist wl1 = new Wishlist("Birthday", "My birthday gifts");
		Wishlist wl2 = new Wishlist("Christmas", "Gift list");
		wls.add(wl1);
		wls.add(wl2);
		GuiActionRunner.execute(() -> view.showAllWLs(wls));
		assertThat(window.list("listWL").valueAt(0)).isEqualTo(wls.get(0).toString());
		assertThat(window.list("listWL").valueAt(1)).isEqualTo(wls.get(1).toString());
	}

	@Test
	@GUITest
	public void showAllItemsTheWLsInTheListWL() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item1 = new Item("Phone", "Samsung Galaxy A52", 300);
		Item item2 = new Item("Wallet", "D&G", 200);
		wl.getItems().add(item1);
		wl.getItems().add(item2);
		GuiActionRunner.execute(() -> view.showAllItems(wl));
		assertThat(window.list("listItem").valueAt(0)).isEqualTo(wl.getItems().get(0).toString());
		assertThat(window.list("listItem").valueAt(1)).isEqualTo(wl.getItems().get(1).toString());
	}

	@Test
	@GUITest
	public void showErrorDisplayMessageOnErrorLabelUntilNextUIInteraction() throws InterruptedException {
		String error = "Error: please try again";
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		wl.getItems().add(item);
		GuiActionRunner.execute(() -> {
			view.getListWLModel().addElement(wl);
			view.getListItemModel().addElement(item);
		});

		GuiActionRunner.execute(() -> view.showError(error));
		window.label("lblError").requireText(error);
		window.button("btnRefresh").click();
		window.label("lblError").requireText("");

		GuiActionRunner.execute(() -> view.showError(error));
		window.label("lblError").requireText(error);
		window.button("btnAddWL").click();
		view.getAddWLFrame().dispose();
		window.label("lblError").requireText("");

		GuiActionRunner.execute(() -> view.showError(error));
		window.label("lblError").requireText(error);
		window.list("listWL").selectItem(0);
		window.button("btnRemoveWL").click();
		window.label("lblError").requireText("");

		GuiActionRunner.execute(() -> view.showError(error));
		window.label("lblError").requireText(error);
		window.button("btnAddItem").click();
		view.getAddItemFrame().dispose();
		window.label("lblError").requireText("");

		GuiActionRunner.execute(() -> view.showError(error));
		window.label("lblError").requireText(error);
		window.list("listItem").selectItem(0);
		window.button("btnRemoveItem").click();
		window.label("lblError").requireText("");
	}

	@Test
	@GUITest
	public void btnRemoveWLRemoveSelectedWLFromListWL() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		doAnswer(e -> view.getListWLModel().removeElement(wl)).when(controller).removeWishlist(wl);
		GuiActionRunner.execute(() -> view.getListWLModel().addElement(wl));
		window.list("listWL").selectItem(0);
		window.button("btnRemoveWL").click();
		assertThat(view.getListWLModel().contains(wl)).isFalse();
		verify(controller).removeWishlist(wl);
	}

	@Test
	@GUITest
	public void listWLSelectionDisplayTheWlDescription() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		GuiActionRunner.execute(() -> view.getListWLModel().addElement(wl));
		window.label("lblWLDesc").requireText("");
		window.list("listWL").selectItem(0);
		window.label("lblWLDesc").requireText(wl.getDesc());
		window.list("listWL").clearSelection();
		window.label("lblWLDesc").requireText("");
	}

	@Test
	@GUITest
	public void listItemSelectionDisplayTheItemDescriptionAndPriceAndEnableRemoveItemButton() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		wl.getItems().add(item);
		GuiActionRunner.execute(() -> view.getListItemModel().addElement(item));
		window.label("lblItemDesc").requireText("");
		window.list("listItem").selectItem(0);
		window.button("btnRemoveItem").requireEnabled();
		//To match all float print format on different OS
		window.label("lblItemDesc").requireText(String.format("Samsung Galaxy A52 (Price: %.2f€)", item.getPrice()));
		window.list("listItem").clearSelection();
		window.label("lblItemDesc").requireText("");
		window.button("btnRemoveItem").requireDisabled();
	}

	@Test
	@GUITest
	public void btnRemoveItemRemoveSelectedItemFromListItem() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		doAnswer(e -> view.getListItemModel().removeElement(item)).when(controller).removeItemFromWishlist(item, wl);
		wl.getItems().add(item);
		GuiActionRunner.execute(() -> {
			view.getListWLModel().addElement(wl);
			view.getListItemModel().addElement(item);
		});
		window.list("listWL").selectItem(0);
		window.list("listItem").selectItem(0);
		window.button("btnRemoveItem").click();
		verify(controller).removeItemFromWishlist(item, wl);
		assertThat(view.getListItemModel().contains(item)).isFalse();
	}

	@Test
	@GUITest
	public void btnRefreshWithNoSelectionOnlyUpdateWLs() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		doNothing().when(controller).refreshWishlists();
		GuiActionRunner.execute(() -> {
			view.getListWLModel().addElement(wl);
		});
		window.button("btnRefresh").click();
		verify(controller).refreshWishlists();
		verify(controller, times(0)).refreshItems(wl);
	}

	@Test
	@GUITest
	public void btnRefreshWithWLSelectedIfStillExistsRefreshItsItems() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		wl.getItems().add(item);
		doAnswer(e -> {
			view.getListWLModel().removeElement(wl);
			view.getListWLModel().addElement(wl);
			return null;
		}).when(controller).refreshWishlists();
		doNothing().when(controller).refreshItems(wl);
		GuiActionRunner.execute(() -> {
			view.getListWLModel().addElement(wl);
			view.getListItemModel().addElement(item);
		});
		window.list("listWL").selectItem(0);
		window.button("btnRefresh").click();
		window.list("listWL").requireSelection(0);
		verify(controller).refreshWishlists();
		verify(controller).refreshItems(wl);
	}

	@Test
	@GUITest
	public void btnRefreshWithWLSelectedIfNotExistsAnymoreDoesntRefreshItsItems() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		wl.getItems().add(item);
		doAnswer(e -> view.getListWLModel().removeElement(wl)).when(controller).refreshWishlists();
		GuiActionRunner.execute(() -> {
			view.getListWLModel().addElement(wl);
			view.getListItemModel().addElement(item);
		});
		window.list("listWL").selectItem(0);
		window.button("btnRefresh").click();
		verify(controller).refreshWishlists();
		verify(controller, times(0)).refreshItems(wl);
		assertThat(view.getListWLModel().getSize()).isZero();
		assertThat(view.getListItemModel().getSize()).isZero();
	}

	@Test
	@GUITest
	public void btnAddWLSetVisibleAddWLFrame() {
		assertThat(view.getAddWLFrame()).isNull();
		window.button("btnAddWL").click();
		assertThat(view.getAddWLFrame().isActive()).isTrue();
		assertThat(view.getAddWLFrame().getController()).isEqualTo(controller);
	}

	@Test
	@GUITest
	public void btnAddItemSetVisibleAddItemFrame() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		GuiActionRunner.execute(() -> view.getListWLModel().addElement(wl));
		window.list("listWL").selectItem(0);
		assertThat(view.getAddItemFrame()).isNull();
		window.button("btnAddItem").click();
		assertThat(view.getAddItemFrame().isActive()).isTrue();
		assertThat(view.getAddItemFrame().getController()).isEqualTo(controller);
		assertThat(view.getAddItemFrame().getWl()).isEqualTo(wl);
	}

}
