package view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import businesslogic.WishlistController;
import model.Item;
import model.Wishlist;

public class WishlistSwingView extends JFrame implements WishlistView {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private AddWishlistSwingView addWLFrame;
	private AddItemSwingView addItemFrame;

	private transient WishlistController controller;

	private JList<Wishlist> listWL;
	private DefaultListModel<Wishlist> listWLModel;
	private JList<Item> listItem;
	private DefaultListModel<Item> listItemModel;
	private JLabel lblError;
	private JLabel lblWL;
	private JButton btnRefresh;
	private JScrollPane scrollPane1;
	private JLabel lblWLDesc;
	private JButton btnAddWL;
	private JButton btnRemoveWL;
	private JSeparator separator1;
	private JLabel lblItem;
	private JLabel lblItemDesc;
	private JButton btnAddItem;
	private JButton btnRemoveItem;
	private JSeparator separator2;

	private transient Wishlist selectedWL;

	public WishlistSwingView() {
		setTitle("Wishlist App");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 397);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 226, 42, 148, 0 };
		gbl_contentPane.rowHeights = new int[] { 23, 61, 34, 23, 2, 14, 61, 34, 23, 2, 14, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		listItemModel = new DefaultListModel<>();

		listWLModel = new DefaultListModel<>();

		lblWL = new JLabel("Wishlists:");
		lblWL.setName("lblWL");
		GridBagConstraints gbc_lblWL = new GridBagConstraints();
		gbc_lblWL.anchor = GridBagConstraints.SOUTH;
		gbc_lblWL.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblWL.insets = new Insets(0, 0, 5, 5);
		gbc_lblWL.gridx = 0;
		gbc_lblWL.gridy = 0;
		contentPane.add(lblWL, gbc_lblWL);

		btnRefresh = new JButton("Refresh");
		btnRefresh.setName("btnRefresh");
		GridBagConstraints gbc_btnRefresh = new GridBagConstraints();
		gbc_btnRefresh.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnRefresh.insets = new Insets(0, 0, 5, 0);
		gbc_btnRefresh.gridx = 2;
		gbc_btnRefresh.gridy = 0;
		contentPane.add(btnRefresh, gbc_btnRefresh);
		btnRefresh.addActionListener(e -> {
			clearError();
			int selectedIndex = listWL.getSelectedIndex();
			if (selectedIndex != -1) {
				Wishlist wl = listWLModel.getElementAt(selectedIndex);
				controller.refreshWishlists();
				if (listWLModel.contains(wl)) {
					controller.refreshItems(wl);
					listWL.setSelectedValue(wl, true);
				}
			} else {
				controller.refreshWishlists();
			}
		});
		scrollPane1 = new JScrollPane();
		scrollPane1.setName("scrollPane1");
		GridBagConstraints gbc_scrollPane1 = new GridBagConstraints();
		gbc_scrollPane1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane1.gridwidth = 3;
		gbc_scrollPane1.gridx = 0;
		gbc_scrollPane1.gridy = 1;
		contentPane.add(scrollPane1, gbc_scrollPane1);
		listWL = new JList<>(listWLModel);
		scrollPane1.setViewportView(listWL);
		listWL.setToolTipText("");
		listWL.setName("listWL");
		listWL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listWL.addListSelectionListener(e -> {
			int selectedIndex = listWL.getSelectedIndex();
			btnRemoveWL.setEnabled(selectedIndex != -1);
			btnAddItem.setEnabled(selectedIndex != -1);
			if (selectedIndex != -1) {
				showAllItems(listWLModel.elementAt(selectedIndex));
				lblWLDesc.setText(listWLModel.elementAt(selectedIndex).getDesc());
				lblItem.setText(String.format("Wishes in %s:", listWLModel.elementAt(selectedIndex).getName()));
				selectedWL = listWLModel.elementAt(selectedIndex);
			} else {
				listItemModel.clear();
				lblWLDesc.setText("");
				lblItem.setText("Select a Wishlist...");
			}
		});

		lblWLDesc = new JLabel("");
		lblWLDesc.setName("lblWLDesc");
		GridBagConstraints gbc_lblWLDesc = new GridBagConstraints();
		gbc_lblWLDesc.fill = GridBagConstraints.BOTH;
		gbc_lblWLDesc.insets = new Insets(0, 0, 5, 0);
		gbc_lblWLDesc.gridwidth = 3;
		gbc_lblWLDesc.gridx = 0;
		gbc_lblWLDesc.gridy = 2;
		contentPane.add(lblWLDesc, gbc_lblWLDesc);

		btnAddWL = new JButton("Add");
		btnAddWL.setName("btnAddWL");
		GridBagConstraints gbc_btnAddWL = new GridBagConstraints();
		gbc_btnAddWL.anchor = GridBagConstraints.NORTH;
		gbc_btnAddWL.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddWL.gridx = 0;
		gbc_btnAddWL.gridy = 3;
		contentPane.add(btnAddWL, gbc_btnAddWL);
		btnAddWL.addActionListener(e -> {
			clearError();
			addWLFrame = new AddWishlistSwingView();
			addWLFrame.setController(controller);
			addWLFrame.setVisible(true);
		});

		btnRemoveWL = new JButton("Remove");
		btnRemoveWL.setName("btnRemoveWL");
		btnRemoveWL.setEnabled(false);
		GridBagConstraints gbc_btnRemoveWL = new GridBagConstraints();
		gbc_btnRemoveWL.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRemoveWL.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveWL.gridx = 2;
		gbc_btnRemoveWL.gridy = 3;
		contentPane.add(btnRemoveWL, gbc_btnRemoveWL);
		btnRemoveWL.addActionListener(e -> {
			clearError();
			controller.removeWishlist(listWLModel.get(listWL.getSelectedIndex()));
		});

		separator1 = new JSeparator();
		separator1.setName("separator1");
		GridBagConstraints gbc_separator1 = new GridBagConstraints();
		gbc_separator1.anchor = GridBagConstraints.NORTH;
		gbc_separator1.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator1.insets = new Insets(0, 0, 5, 0);
		gbc_separator1.gridwidth = 3;
		gbc_separator1.gridx = 0;
		gbc_separator1.gridy = 4;
		contentPane.add(separator1, gbc_separator1);

		lblItem = new JLabel("Select a Wishlist...");
		lblItem.setName("lblItem");
		GridBagConstraints gbc_lblItem = new GridBagConstraints();
		gbc_lblItem.anchor = GridBagConstraints.NORTH;
		gbc_lblItem.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblItem.insets = new Insets(0, 0, 5, 0);
		gbc_lblItem.gridwidth = 3;
		gbc_lblItem.gridx = 0;
		gbc_lblItem.gridy = 5;
		contentPane.add(lblItem, gbc_lblItem);
		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setName("scrollPane2");
		GridBagConstraints gbc_scrollPane2 = new GridBagConstraints();
		gbc_scrollPane2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane2.gridwidth = 3;
		gbc_scrollPane2.gridx = 0;
		gbc_scrollPane2.gridy = 6;
		contentPane.add(scrollPane2, gbc_scrollPane2);
		listItem = new JList<>(listItemModel);
		scrollPane2.setViewportView(listItem);
		listItem.setName("listItem");
		listItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listItem.addListSelectionListener(e -> {
			int selectedIndex = listItem.getSelectedIndex();
			btnRemoveItem.setEnabled(selectedIndex != -1);
			if (selectedIndex != -1) {
				Item element = listItemModel.elementAt(selectedIndex);
				lblItemDesc.setText(String.format("%s (Price: %.2f€)", element.getDesc(), element.getPrice()));
			} else {
				lblItemDesc.setText("");
			}
		});

		lblItemDesc = new JLabel("");
		lblItemDesc.setName("lblItemDesc");
		GridBagConstraints gbc_lblItemDesc = new GridBagConstraints();
		gbc_lblItemDesc.fill = GridBagConstraints.BOTH;
		gbc_lblItemDesc.insets = new Insets(0, 0, 5, 0);
		gbc_lblItemDesc.gridwidth = 3;
		gbc_lblItemDesc.gridx = 0;
		gbc_lblItemDesc.gridy = 7;
		contentPane.add(lblItemDesc, gbc_lblItemDesc);

		btnAddItem = new JButton("Add");
		btnAddItem.setName("btnAddItem");
		btnAddItem.setEnabled(false);
		GridBagConstraints gbc_btnAddItem = new GridBagConstraints();
		gbc_btnAddItem.anchor = GridBagConstraints.NORTH;
		gbc_btnAddItem.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddItem.gridx = 0;
		gbc_btnAddItem.gridy = 8;
		contentPane.add(btnAddItem, gbc_btnAddItem);
		btnAddItem.addActionListener(e -> {
			clearError();
			addItemFrame = new AddItemSwingView();
			addItemFrame.setController(controller);
			addItemFrame.setWl(listWLModel.get(listWL.getSelectedIndex()));
			addItemFrame.setVisible(true);
		});

		btnRemoveItem = new JButton("Remove");
		btnRemoveItem.setName("btnRemoveItem");
		btnRemoveItem.setEnabled(false);
		GridBagConstraints gbc_btnRemoveItem = new GridBagConstraints();
		gbc_btnRemoveItem.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRemoveItem.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveItem.gridx = 2;
		gbc_btnRemoveItem.gridy = 8;
		contentPane.add(btnRemoveItem, gbc_btnRemoveItem);
		btnRemoveItem.addActionListener(e -> {
			clearError();
			controller.removeItemFromWishlist(listItemModel.get(listItem.getSelectedIndex()), selectedWL);
		});

		separator2 = new JSeparator();
		separator2.setName("separator2");
		GridBagConstraints gbc_separator2 = new GridBagConstraints();
		gbc_separator2.anchor = GridBagConstraints.NORTH;
		gbc_separator2.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator2.insets = new Insets(0, 0, 5, 0);
		gbc_separator2.gridwidth = 3;
		gbc_separator2.gridx = 0;
		gbc_separator2.gridy = 9;
		contentPane.add(separator2, gbc_separator2);

		lblError = new JLabel("");
		lblError.setName("lblError");
		lblError.setForeground(new Color(255, 0, 0));
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.fill = GridBagConstraints.BOTH;
		gbc_lblError.gridwidth = 3;
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 10;
		contentPane.add(lblError, gbc_lblError);
	}

	@Override
	public void showAllWLs(List<Wishlist> wlList) {
		listWLModel.clear();
		listWLModel.addAll(wlList);
	}

	@Override
	public void showAllItems(Wishlist wl) {
		listItemModel.clear();
		listItemModel.addAll(wl.getItems());
	}

	@Override
	public void showError(String errorMessage) {
		lblError.setText(errorMessage);
	}

	public void clearError() {
		lblError.setText("");
	}

	public void setController(WishlistController controller) {
		this.controller = controller;
	}

	DefaultListModel<Item> getListItemModel() {
		return listItemModel;
	}

	DefaultListModel<Wishlist> getListWLModel() {
		return listWLModel;
	}

	AddWishlistSwingView getAddWLFrame() {
		return addWLFrame;
	}

	AddItemSwingView getAddItemFrame() {
		return addItemFrame;
	}
}
