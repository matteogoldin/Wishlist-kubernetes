package view;

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
import model.Wishlist;

@RunWith(GUITestRunner.class)
public class AddWishlistSwingViewTest extends AssertJSwingJUnitTestCase{
	private FrameFixture window;

	@Mock
	private WishlistController controller;
	private AutoCloseable closeable;
	private AddWishlistSwingView view;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			view = new AddWishlistSwingView();
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
		window.label("lblName");
		window.label("lblDesc");
		window.button("btnAdd").requireDisabled();
		window.textBox("textName");
		window.textBox("textDesc");
	}

	@Test
	@GUITest
	public void btnAddEnabledOnlyWhenTextFieldAreFilled() {
		window.button("btnAdd").requireDisabled();
		window.textBox("textName").setText("Birthday");
		window.button("btnAdd").requireDisabled();
		window.textBox("textName").setText(" ");
		window.textBox("textDesc").setText("My birthday gifts");
		window.button("btnAdd").requireDisabled();
		window.textBox("textName").setText("Birthday");
		window.button("btnAdd").requireEnabled();
	}

	@Test
	@GUITest
	public void btnAddCallControllerAddWishlist() {
		window.textBox("textName").setText("Birthday");
		window.textBox("textDesc").setText("My birthday gifts");
		window.button("btnAdd").click();
		verify(controller).addWishlist(isA(Wishlist.class));
	}

}
