package view;

import java.util.List;

import model.Wishlist;

public interface WishlistView {

	void showAllWLs(List<Wishlist> wlList);

	void showAllItems(Wishlist wl);

	void showError(String string);

}
