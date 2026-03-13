package view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import businesslogic.WishlistController;
import model.Wishlist;
import utils.Generated;

public class AddWishlistSwingView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textName;

	private transient WishlistController controller;
	private JLabel lblName;
	private JLabel lblDesc;
	private JTextArea textDesc;
	private JButton btnAdd;

	 /**
	 * Create the frame.
	 */
	public AddWishlistSwingView() {
		MyDocumentListener mdc = new MyDocumentListener();

		setTitle("Add Wishlist");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		lblName = new JLabel("Name:");
		lblName.setName("lblName");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.insets = new Insets(0, 0, 5, 0);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		contentPane.add(lblName, gbc_lblName);

		textName = new JTextField();
		textName.setName("textName");
		GridBagConstraints gbc_textName = new GridBagConstraints();
		gbc_textName.insets = new Insets(0, 0, 5, 0);
		gbc_textName.fill = GridBagConstraints.HORIZONTAL;
		gbc_textName.gridx = 0;
		gbc_textName.gridy = 1;
		contentPane.add(textName, gbc_textName);
		textName.setColumns(10);
		textName.getDocument().addDocumentListener(mdc);

		lblDesc = new JLabel("Description:");
		lblDesc.setName("lblDesc");
		GridBagConstraints gbc_lblDesc = new GridBagConstraints();
		gbc_lblDesc.anchor = GridBagConstraints.WEST;
		gbc_lblDesc.insets = new Insets(0, 0, 5, 0);
		gbc_lblDesc.gridx = 0;
		gbc_lblDesc.gridy = 2;
		contentPane.add(lblDesc, gbc_lblDesc);

		textDesc = new JTextArea();
		textDesc.setName("textDesc");
		GridBagConstraints gbc_textDesc = new GridBagConstraints();
		gbc_textDesc.insets = new Insets(0, 0, 5, 0);
		gbc_textDesc.fill = GridBagConstraints.BOTH;
		gbc_textDesc.gridx = 0;
		gbc_textDesc.gridy = 3;
		contentPane.add(textDesc, gbc_textDesc);
		textDesc.getDocument().addDocumentListener(mdc);

		btnAdd = new JButton("Add");
		btnAdd.setEnabled(false);
		btnAdd.setName("btnAdd");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 4;
		contentPane.add(btnAdd, gbc_btnAdd);
		btnAdd.addActionListener(e -> {
			String name = textName.getText();
			String desc = textDesc.getText();
			Wishlist wl = new Wishlist(name, desc);
			controller.addWishlist(wl);
			this.dispose();
		});
	}

	WishlistController getController() {
		return controller;
	}

	void setController(WishlistController controller) {
		this.controller = controller;
	}

	class MyDocumentListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			btnAddEnabler();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			btnAddEnabler();
		}

		@Generated
		@Override
		public void changedUpdate(DocumentEvent e) { /* Not used */ }

		private void btnAddEnabler() {
			btnAdd.setEnabled(!textName.getText().trim().isEmpty() && !textDesc.getText().trim().isEmpty());
		}
	}

}
