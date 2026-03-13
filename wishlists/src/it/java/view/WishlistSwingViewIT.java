package view;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import businesslogic.WishlistController;
import daos.WishlistDAO;
import model.Item;
import model.Wishlist;

@RunWith(GUITestRunner.class)
public class WishlistSwingViewIT extends AssertJSwingJUnitTestCase {
	private WishlistController controller;
	private WishlistSwingView view;
	private WishlistDAO dao;

	private FrameFixture window;

	@Override
	protected void onSetUp() throws Exception {
		dao = new WishlistDAO("wishlists-pu-test");
		GuiActionRunner.execute(() -> {
			view = new WishlistSwingView();
			controller = new WishlistController(view, dao);
			view.setController(controller);
			controller.refreshWishlists();
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();
	}

	@Test
	@GUITest
	public void addWLBtnDisplayTheWLInTheListAndAddItToTheDB() {
		GenericTypeMatcher<AddWishlistSwingView> matcher = new GenericTypeMatcher<>(AddWishlistSwingView.class) {
			@Override
			protected boolean isMatching(AddWishlistSwingView component) {
				return component.isShowing();
			}
		};

		window.list("listWL").requireItemCount(0);
		window.button("btnAddWL").click();
		FrameFixture addWLWindow = WindowFinder.findFrame(matcher).using(robot());
		addWLWindow.textBox("textName").setText("Birthday");
		addWLWindow.textBox("textDesc").setText("My birthday gifts");
		addWLWindow.button("btnAdd").click();
		addWLWindow.requireNotVisible();
		window.list("listWL").requireItemCount(1);
		assertThat(dao.getAll()).hasSize(1);
	}

	@Test
	@GUITest
	public void tryingToAddAWishlistArleadyInTheListDisplayError() {
		GenericTypeMatcher<AddWishlistSwingView> matcher = new GenericTypeMatcher<>(AddWishlistSwingView.class) {
			@Override
			protected boolean isMatching(AddWishlistSwingView component) {
				return component.isShowing();
			}
		};

		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		GuiActionRunner.execute(() -> {
			controller.addWishlist(wl);
		});
		window.button("btnAddWL").click();
		FrameFixture addWLWindow = WindowFinder.findFrame(matcher).using(robot());
		addWLWindow.textBox("textName").setText("Birthday");
		addWLWindow.textBox("textDesc").setText("My birthday gifts");
		addWLWindow.button("btnAdd").click();
		window.label("lblError").requireText("Wishlist Birthday already exists");
	}

	@Test
	@GUITest
	public void removeWLBtnRemoveWLFromTheListAndTheDB() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		GuiActionRunner.execute(() -> {
			controller.addWishlist(wl);
		});
		window.list("listWL").selectItem(0);
		window.button("btnRemoveWL").click();
		window.list("listWL").requireItemCount(0);
		assertThat(dao.getAll()).isEmpty();
	}

	@Test
	@GUITest
	public void refreshBtnUpdateTheWLList() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		dao.add(wl);
		window.button("btnRefresh").click();
		window.list("listWL").requireItemCount(1);
	}

	@Test
	@GUITest
	public void addItemBtnAddItemToListAndDB() {
		GenericTypeMatcher<AddItemSwingView> matcher = new GenericTypeMatcher<>(AddItemSwingView.class) {
			@Override
			protected boolean isMatching(AddItemSwingView component) {
				return component.isShowing();
			}
		};
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		GuiActionRunner.execute(() -> {
			controller.addWishlist(wl);
		});

		window.list("listWL").selectItem(0);
		window.button("btnAddItem").click();
		FrameFixture addItemWindow = WindowFinder.findFrame(matcher).using(robot());
		addItemWindow.textBox("textName").setText("Phone");
		addItemWindow.textBox("textDesc").setText("Samsung Galaxy A52");
		addItemWindow.textBox("textPrice").setText("300");
		addItemWindow.button("btnAdd").click();
		addItemWindow.requireNotVisible();
		window.list("listItem").requireItemCount(1);
		assertThat(dao.getAllWlItems(wl)).hasSize(1);
	}

	@Test
	@GUITest
	public void tryingToAddAItemAlreadyExistenntWithinAWishlistListDisplayError() {
		GenericTypeMatcher<AddItemSwingView> matcher = new GenericTypeMatcher<>(AddItemSwingView.class) {
			@Override
			protected boolean isMatching(AddItemSwingView component) {
				return component.isShowing();
			}
		};
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		GuiActionRunner.execute(() -> {
			controller.addWishlist(wl);
		});
		window.list("listWL").selectItem(0);
		window.button("btnAddItem").click();
		FrameFixture addItemWindow = WindowFinder.findFrame(matcher).using(robot());
		addItemWindow.textBox("textName").setText("Phone");
		addItemWindow.textBox("textDesc").setText("Samsung Galaxy A52");
		addItemWindow.textBox("textPrice").setText("300");
		addItemWindow.button("btnAdd").click();
		window.list("listWL").selectItem(0);
		window.button("btnAddItem").click();
		addItemWindow = WindowFinder.findFrame(matcher).using(robot());
		addItemWindow.textBox("textName").setText("Phone");
		addItemWindow.textBox("textDesc").setText("Samsung Galaxy A52");
		addItemWindow.textBox("textPrice").setText("300");
		addItemWindow.button("btnAdd").click();
		window.label("lblError").requireText("Item Phone is already in the Wishlist");
		window.list("listWL").requireItemCount(1);
		window.list("listItem").requireItemCount(1);
	}

	@Test
	@GUITest
	public void removeItemBtnRemoveItemToListAndDB() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		GuiActionRunner.execute(() -> {
			controller.addWishlist(wl);
		});
		window.list("listWL").selectItem(0);
		GuiActionRunner.execute(() -> {
			controller.addItemToWishlist(item, wl);
		});
		window.list("listItem").selectItem(0);
		window.button("btnRemoveItem").click();
		window.list("listItem").requireItemCount(0);
		assertThat(dao.getAllWlItems(wl)).isEmpty();
	}

	@Test
	@GUITest
	public void refreshBtnUpdateTheItemList() {
		Wishlist wl = new Wishlist("Birthday", "My birthday gifts");
		Item item = new Item("Phone", "Samsung Galaxy A52", 300);
		GuiActionRunner.execute(() -> {
			controller.addWishlist(wl);
			dao.addItem(wl, item);
		});
		window.list("listWL").selectItem(0);
		window.list("listItem").requireItemCount(0);
		window.button("btnRefresh").click();
		window.list("listItem").requireItemCount(1);
	}

}
