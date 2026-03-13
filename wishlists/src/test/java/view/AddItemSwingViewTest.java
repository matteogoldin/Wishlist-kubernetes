package view;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

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
public class AddItemSwingViewTest extends AssertJSwingJUnitTestCase{
	private FrameFixture window;

	@Mock
	private WishlistController controller;
	private AutoCloseable closeable;
	private AddItemSwingView view;
	private Wishlist wl;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			wl = new Wishlist("Birthday", "My birthday gifts");
			view = new AddItemSwingView();
			view.setController(controller);
			view.setWl(wl);
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
		window.label("lblName");
		window.label("lblDesc");
		window.button("btnAdd").requireDisabled();
		window.textBox("textName");
		window.textBox("textDesc");
		window.textBox("textPrice");
	}

	@Test
	@GUITest
	public void btnAddEnabledOnlyWhenTextFieldAreCorrectlyFilled() {
		window.button("btnAdd").requireDisabled();

		window.textBox("textName").setText("Birthday");
		window.button("btnAdd").requireDisabled();

		window.textBox("textName").setText(" ");
		window.textBox("textDesc").setText("My birthday gifts");
		window.button("btnAdd").requireDisabled();

		window.textBox("textName").setText("Birthday");
		window.textBox("textDesc").setText("My birthday gifts");
		window.button("btnAdd").requireEnabled();

		window.textBox("textPrice").setText("one hundred");
		window.button("btnAdd").requireDisabled();
		window.label("lblPriceError").requireVisible();

		window.textBox("textPrice").setText("100");
		window.button("btnAdd").requireEnabled();

		window.textBox("textPrice").setText("100.00");
		window.button("btnAdd").requireEnabled();
	}

	@Test
	@GUITest
	public void btnAddCallControllerAddWishlist() {
		window.textBox("textName").setText("Phone");
		window.textBox("textDesc").setText("Samsung Galaxy A52");
		window.button("btnAdd").click();
		verify(controller).addItemToWishlist(isA(Item.class), eq(wl));
	}

}
