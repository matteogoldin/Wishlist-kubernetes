package wishlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import utils.SQLClient;
import view.AddItemSwingView;
import view.AddWishlistSwingView;
import view.WishlistSwingView;

@RunWith(GUITestRunner.class)
public class WishlistSwingAppE2E extends AssertJSwingJUnitTestCase{
	private String persistenceUnit = "wishlists-pu-it";
	private SQLClient client;
	private FrameFixture mainWindow;
	private FrameFixture addWLWindow;
	private FrameFixture addItemWindow;

	@Override
	protected void onSetUp() throws Exception {
		client = new SQLClient(persistenceUnit);
		client.initEmptyDB();
		client.insertWishlist("Birthday", "My birthday party");
		client.insertItem("Birthday", "Phone", "Samsung Galaxy A52", 300);
		application("app.WishlistApp").withArgs("--persistence-unit=" + persistenceUnit).start();
		mainWindow = WindowFinder.findFrame(new GenericTypeMatcher<>(WishlistSwingView.class) {
			@Override
			protected boolean isMatching(WishlistSwingView frame) {
				return frame.isShowing();
			}
		}).using(robot());
	}

	@Override
	@After
	public void onTearDown() {
		if (mainWindow != null)
			mainWindow.cleanUp();
	}

	@Test
	@GUITest
	public void addingAWLWithAddButtonDisplaysItOnTheView() {
		mainWindow.button("btnAddWL").click();
		addWLWindow = WindowFinder.findFrame(new GenericTypeMatcher<>(AddWishlistSwingView.class) {
			@Override
			protected boolean isMatching(AddWishlistSwingView frame) {
				return frame.isShowing();
			}
		}).using(robot());
		addWLWindow.requireVisible();
		addWLWindow.textBox("textName").setText("Christmas");
		addWLWindow.textBox("textDesc").setText("My Christmas wishes");
		addWLWindow.button("btnAdd").click();
		addWLWindow.requireNotVisible();
		assertThat(mainWindow.list("listWL").contents()).anySatisfy(wl -> assertThat(wl).isEqualTo("Christmas"));
		mainWindow.list("listWL").requireItemCount(2);
	}

	@Test
	@GUITest
	public void addingAnAlreadyExistentWLShowError() {
		mainWindow.button("btnAddWL").click();
		addWLWindow = WindowFinder.findFrame(new GenericTypeMatcher<>(AddWishlistSwingView.class) {
			@Override
			protected boolean isMatching(AddWishlistSwingView frame) {
				return frame.isShowing();
			}
		}).using(robot());
		addWLWindow.requireVisible();
		addWLWindow.textBox("textName").setText("Birthday");
		addWLWindow.textBox("textDesc").setText("My birthday wishes");
		addWLWindow.button("btnAdd").click();
		addWLWindow.requireNotVisible();
		mainWindow.list("listWL").requireItemCount(1);
		mainWindow.label("lblError").requireText("Wishlist Birthday already exists");
	}

	@Test
	@GUITest
	public void addingAnItemWithAddButtonDisplaysItOnTheView() {
		mainWindow.list("listWL").selectItem(0);
		mainWindow.button("btnAddItem").click();
		addItemWindow = WindowFinder.findFrame(new GenericTypeMatcher<>(AddItemSwingView.class) {
			@Override
			protected boolean isMatching(AddItemSwingView frame) {
				return frame.isShowing();
			}
		}).using(robot());
		addItemWindow.requireVisible();
		addItemWindow.textBox("textName").setText("Wallet");
		addItemWindow.textBox("textDesc").setText("Leather");
		addItemWindow.textBox("textPrice").setText("100");
		addItemWindow.button("btnAdd").click();
		addItemWindow.requireNotVisible();
		assertThat(mainWindow.list("listItem").contents()).anySatisfy(item -> assertThat(item).isEqualTo("Wallet"));
		mainWindow.list("listItem").requireItemCount(2);
	}

	@Test
	@GUITest
	public void addingAnAlreadyExistentItemShowError() {
		GenericTypeMatcher<AddItemSwingView> matcher = new GenericTypeMatcher<>(AddItemSwingView.class) {
			@Override
			protected boolean isMatching(AddItemSwingView frame) {
				return frame.isShowing();
			}
		};
		mainWindow.list("listWL").selectItem(0);
		mainWindow.button("btnAddItem").click();
		addItemWindow = WindowFinder.findFrame(matcher).using(robot());
		addItemWindow.requireVisible();
		addItemWindow.textBox("textName").setText("Wallet");
		addItemWindow.textBox("textDesc").setText("Leather");
		addItemWindow.textBox("textPrice").setText("100");
		addItemWindow.button("btnAdd").click();
		addItemWindow.requireNotVisible();
		mainWindow.list("listItem").requireItemCount(2);

		mainWindow.list("listWL").selectItem(0);
		mainWindow.button("btnAddItem").click();
		addItemWindow = WindowFinder.findFrame(matcher).using(robot());
		addItemWindow.requireVisible();
		addItemWindow.textBox("textName").setText("Wallet");
		addItemWindow.textBox("textDesc").setText("Leather");
		addItemWindow.textBox("textPrice").setText("100");
		addItemWindow.button("btnAdd").click();
		addItemWindow.requireNotVisible();
		mainWindow.list("listItem").requireItemCount(2);
		mainWindow.label("lblError").requireText("Item Wallet is already in the Wishlist");
	}

}
