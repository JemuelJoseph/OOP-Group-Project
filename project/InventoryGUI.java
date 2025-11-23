package project;

//AI USE on line 213 (Prompt: "How to ensure a string contains only characters or a single quote in Java")
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InventoryGUI extends JFrame {

    private JTabbedPane tabbedPane;
    private boolean isNewItem = false;
     private DefaultListModel<Item> itemListModel;
    private JList<Item> itemList;

    // SALES TAB FIELDS
    private JComboBox<Item> cbSaleItems;
    private JTextField txtSaleQty;
    private JButton btnSaleAdd;
    private JButton btnSaleRemove;
    private JButton btnSaleComplete;

    private JTable tblSaleLines;
    private DefaultTableModel saleTableModel;
    private JTextField txtSaleTotal;

    // Sales data
    private ArrayList<SaleItem> currentSaleItems = new ArrayList<>();
    private ArrayList<Sale> completedSales = new ArrayList<>();

    public InventoryGUI() {

        setTitle("Inventory Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        // Add the two required tabs
        tabbedPane.addTab("Inventory", createInventoryPanel());
        tabbedPane.addTab("Sales", createSalesPanel());

        add(tabbedPane);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private long getNextInventoryNumber(DefaultListModel<Item> model) {
        long maxNum = 1000; // start number if list is empty
        for (int i = 0; i < model.size(); i++) {
            long num = model.get(i).getInventoryNum();
            if (num > maxNum)
                maxNum = num;
        }
        return maxNum + 1;
    }

    //Inventory
    private JPanel createInventoryPanel() {

        //MAIN PANEL
        JPanel panel = new JPanel(new BorderLayout());

        //Tool Bar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnAddItem = new JButton("Add Item");
        JButton btnDeleteItem = new JButton("Delete Item");
        JButton btnAddStock = new JButton("Add Stock");
        JButton btnDepleteStock = new JButton("Deplete Stock");
        JButton btnSave = new JButton("Save");

        toolBar.add(btnAddItem);
        toolBar.add(btnDeleteItem);
        toolBar.add(btnAddStock);
        toolBar.add(btnDepleteStock);
        toolBar.add(btnSave);

        panel.add(toolBar, BorderLayout.NORTH);

        //LIST OF ITEMS
        DefaultListModel<Item> itemListModel = new DefaultListModel<>();
        JList<Item> itemList = new JList<>(itemListModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(itemList);
        panel.add(scrollPane, BorderLayout.CENTER);

        //Data Panels
        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));
        dataPanel.setLayout(new GridLayout(8, 2, 5, 5)); // extra row for type

        //Radio buttons for item type
        JLabel lblType = new JLabel("Item Type:");
        JRadioButton rbtnPerishable = new JRadioButton("Perishable");
        JRadioButton rbtnNonPerishable = new JRadioButton("Non-Perishable");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(rbtnPerishable);
        typeGroup.add(rbtnNonPerishable);
        JPanel typePanel = new JPanel();
        typePanel.add(rbtnPerishable);
        typePanel.add(rbtnNonPerishable);

        JLabel lblInvNum = new JLabel("Inventory Number:");
        JTextField txtInvNum = new JTextField();

        JLabel lblName = new JLabel("Name:");
        JTextField txtName = new JTextField();

        JLabel lblStock = new JLabel("Amount in Stock:");
        JTextField txtStock = new JTextField();

        JLabel lblMinStock = new JLabel("Minimum Stock:");
        JTextField txtMinStock = new JTextField();

        JLabel lblPrice = new JLabel("Unit Price:");
        JTextField txtPrice = new JTextField();

        JLabel lblExpiry = new JLabel("Expiry Date (Perishable) (YYYY-MM-DD):");
        JTextField txtExpiry = new JTextField();

        JLabel lblWarranty = new JLabel("Warranty Days (Non-Perishable):");
        JTextField txtWarranty = new JTextField();

        //Make non-editable fields
        txtInvNum.setEditable(false);
        txtStock.setEditable(false);

        //Add fields to panel
        dataPanel.add(lblType);
        dataPanel.add(typePanel);
        dataPanel.add(lblInvNum);
        dataPanel.add(txtInvNum);
        dataPanel.add(lblName);
        dataPanel.add(txtName);
        dataPanel.add(lblStock);
        dataPanel.add(txtStock);
        dataPanel.add(lblMinStock);
        dataPanel.add(txtMinStock);
        dataPanel.add(lblPrice);
        dataPanel.add(txtPrice);
        dataPanel.add(lblExpiry);
        dataPanel.add(txtExpiry);
        dataPanel.add(lblWarranty);
        dataPanel.add(txtWarranty);

        panel.add(dataPanel, BorderLayout.SOUTH);

        //Auto fill when selected an item
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {

                Item item = itemList.getSelectedValue();
                if (item == null)
                    return;

                //Fill common fields
                txtInvNum.setText(String.valueOf(item.getInventoryNum()));
                txtName.setText(item.getName());
                txtStock.setText(String.valueOf(item.getAmtInStock()));
                txtMinStock.setText(String.valueOf(item.getMinimumStock()));
                txtPrice.setText(String.valueOf(item.getUnitPrice()));

                //Reset fields
                txtExpiry.setText("");
                txtWarranty.setText("");
                rbtnPerishable.setSelected(false);
                rbtnNonPerishable.setSelected(false);

                //Fill perishable or non-perishable fields
                if (item instanceof PerishableItem p) {
                    txtExpiry.setText(p.getExpirDate().toString());
                    rbtnPerishable.setSelected(true);
                } else if (item instanceof NonPerishableItem np) {
                    txtWarranty.setText(String.valueOf(np.getWarrantyPeriod()));
                    rbtnNonPerishable.setSelected(true);
                }

                //Not in new item mode
                isNewItem = false;
            }
        });

        //Saving items
        btnSave.addActionListener(e -> {
            Item selectedItem = itemList.getSelectedValue();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(panel, "No item selected!");
                return;
            }

            String name = txtName.getText().trim();
            String minStr = txtMinStock.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String expiryStr = txtExpiry.getText().trim();
            String warrantyStr = txtWarranty.getText().trim();

            //Validate Name
            if (name.isEmpty() || !name.matches("^[a-zA-Z\\s']+$")) {
                JOptionPane.showMessageDialog(panel, "Name must contain only letters, spaces, or apostrophes.");
                return;
            }

            //Validate Minimum Stock
            int minStock;
            try {
                minStock = Integer.parseInt(minStr);
                if (minStock <= 0) throw new Exception("Minimum stock must be greater than 0.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Minimum Stock: " + ex.getMessage());
                return;
            }

            //Validate Unit Price
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) throw new Exception("Unit price must be greater than 0.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Unit Price: " + ex.getMessage());
                return;
            }

            long invNum = selectedItem.getInventoryNum();
            int amtInStock = selectedItem.getAmtInStock(); // Preserve current stock

            //Detect type change
            boolean wantsPerishable = rbtnPerishable.isSelected();
            boolean wantsNonPerishable = rbtnNonPerishable.isSelected();

            if (!wantsPerishable && !wantsNonPerishable) {
                JOptionPane.showMessageDialog(panel, "Please select Perishable or Non-Perishable.");
                return;
            }

            //Handle Perishable
            if (wantsPerishable) {
                java.time.LocalDate expiry;
                try {
                    expiry = java.time.LocalDate.parse(expiryStr);
                    if (!expiry.isAfter(java.time.LocalDate.now())) {
                        JOptionPane.showMessageDialog(panel, "Expiry date must be after today.");
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid Expiry Date. Use YYYY-MM-DD.");
                    return;
                }

                Item updatedItem = new PerishableItem(invNum, name, amtInStock, price, minStock, expiry);

                //Replace in list
                int index = itemList.getSelectedIndex();
                itemListModel.set(index, updatedItem);
                itemList.setSelectedIndex(index);

                
            } else { 
                //Non-Perishable
                int warranty;
                try {
                    warranty = Integer.parseInt(warrantyStr);
                    if (warranty <= 0) throw new Exception("Warranty must be greater than 0.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid Warranty: " + ex.getMessage());
                    return;
                }

                Item updatedItem = new NonPerishableItem(invNum, name, amtInStock, price, minStock, warranty);

                //Replace in list
                int index = itemList.getSelectedIndex();
                itemListModel.set(index, updatedItem);
                itemList.setSelectedIndex(index);
            }

            JOptionPane.showMessageDialog(panel, "Item updated successfully!");
        });


        //Add to inventory/Clear for new item
        btnAddItem.addActionListener(e -> {

            //FIRST CLICK —ENABLE ENTRY MODE
            if (!isNewItem) {
                isNewItem = true;

                txtInvNum.setText("");
                txtName.setText("");
                txtStock.setText("0");
                txtMinStock.setText("");
                txtPrice.setText("");
                txtExpiry.setText("");
                txtWarranty.setText("");

                txtInvNum.setEditable(true);
                txtStock.setEditable(true);

                JOptionPane.showMessageDialog(panel, "Enter details for new item, then click Add Item again to save.");
                return;
            }

            //SECOND CLICK —VALIDATE AND SAVE ITEM
            String invStr = txtInvNum.getText().trim();
            String name = txtName.getText().trim();
            String stockStr = txtStock.getText().trim();
            String minStr = txtMinStock.getText().trim();
            String priceStr = txtPrice.getText().trim();

            //Validate Inventory Number
            long invNum;
            try {
                invNum = Long.parseLong(invStr);
                if (invNum <= 0)
                    throw new Exception("Inventory Number must be greater than 0.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Inventory Number: " + ex.getMessage());
                return;
            }

            //alidate Name
            if (name.isEmpty() || !name.matches("^[a-zA-Z\\s']+$")) {
                JOptionPane.showMessageDialog(panel, "Name must contain only letters, spaces, or apostrophes.");
                return;
            }

            if (!name.matches("^[a-zA-Z\\s']+$")) {
                JOptionPane.showMessageDialog(panel, "Name must contain only letters, spaces, or apostrophes.");
                return;
            }

            //Validate Amount in Stock
            int stock;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0)
                    throw new Exception("Amount in stock must be >= 0.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Amount in Stock: " + ex.getMessage());
                return;
            }

            //Validate Minimum Stock
            int minStock;
            try {
                minStock = Integer.parseInt(minStr);
                if (minStock <= 0)
                    throw new Exception("Minimum stock must be greater than 0.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Minimum Stock: " + ex.getMessage());
                return;
            }

            //Validate Unit Price
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0)
                    throw new Exception("Unit price must be greater than 0.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Unit Price: " + ex.getMessage());
                return;
            }

            //Validate Item Type selection
            if (!rbtnPerishable.isSelected() && !rbtnNonPerishable.isSelected()) {
                JOptionPane.showMessageDialog(panel, "Please select item type.");
                return;
            }

            //Validate perishable expiry date
            java.time.LocalDate expiry = null;
            if (rbtnPerishable.isSelected()) {
                try {
                    expiry = java.time.LocalDate.parse(txtExpiry.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid Expiry Date. Use YYYY-MM-DD.");
                    return;
                }
            }

            //Validate non-perishable warranty
            int warranty = 0;
            if (rbtnNonPerishable.isSelected()) {
                try {
                    warranty = Integer.parseInt(txtWarranty.getText().trim());
                    if (warranty <= 0)
                        throw new Exception("Warranty must be greater than 0.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid Warranty: " + ex.getMessage());
                    return;
                }
            }

            //Date Validation
            if (rbtnPerishable.isSelected()) {
                try {
                    expiry = java.time.LocalDate.parse(txtExpiry.getText().trim());

                    if (!expiry.isAfter(java.time.LocalDate.now())) {
                        JOptionPane.showMessageDialog(panel, "Expiry date must be after today.");
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid Expiry Date. Use format YYYY-MM-DD.");
                    return;
                }
            }

            //Check duplicates
            for (int i = 0; i < itemListModel.size(); i++) {
                Item it = itemListModel.get(i);
                if (it.getInventoryNum() == invNum) {
                    JOptionPane.showMessageDialog(panel, "Inventory number already exists!");
                    return;
                }
                if (it.getName().equalsIgnoreCase(name)) {
                    JOptionPane.showMessageDialog(panel, "Item name already exists!");
                    return;
                }
            }

            //Create item
            Item newItem;
            if (rbtnPerishable.isSelected()) {
                newItem = new PerishableItem(invNum, name, stock, price, minStock, expiry);
            } else {
                newItem = new NonPerishableItem(invNum, name, stock, price, minStock, warranty);
            }

            itemListModel.addElement(newItem);
            JOptionPane.showMessageDialog(panel, "Item added successfully!");

            //Reset form
            isNewItem = false;
            txtInvNum.setEditable(false);
            txtStock.setEditable(false);
            txtInvNum.setText("");
            txtName.setText("");
            txtStock.setText("");
            txtMinStock.setText("");
            txtPrice.setText("");
            txtExpiry.setText("");
            txtWarranty.setText("");
            rbtnPerishable.setSelected(false);
            rbtnNonPerishable.setSelected(false);
        });

        //Add to inventory with dialouge box
        btnAddStock.addActionListener(e -> {
            Item item = itemList.getSelectedValue();
            if (item == null) {
                JOptionPane.showMessageDialog(panel, "No item selected!");
                return;
            }

            String input = JOptionPane.showInputDialog(panel, "Enter amount to add:");
            if (input == null)
                return;

            try {
                int amt = Integer.parseInt(input);
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(panel, "Amount should be greater than 0.");
                    return;
                }
                item.setAmtInStock(item.getAmtInStock() + amt);
                txtStock.setText(String.valueOf(item.getAmtInStock()));
                itemList.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid number!");
            }
        });

        //Depleting a strock with dialogue box
        btnDepleteStock.addActionListener(e -> {
            Item item = itemList.getSelectedValue();
            if (item == null) {
                JOptionPane.showMessageDialog(panel, "No item selected!");
                return;
            }

            String input = JOptionPane.showInputDialog(panel, "Enter amount to remove:");
            if (input == null)
                return;

            try {
                int amt = Integer.parseInt(input);
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(panel, "Amount should be greater than 0.");
                    return;
                }
                if (amt > item.getAmtInStock()) {
                    JOptionPane.showMessageDialog(panel, "Not enough stock!");
                    return;
                }
                item.setAmtInStock(item.getAmtInStock() - amt);
                txtStock.setText(String.valueOf(item.getAmtInStock()));
                itemList.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid number!");
            }
        });

        //Deleting an item with confirmation box
        btnDeleteItem.addActionListener(e -> {
            Item item = itemList.getSelectedValue();
            if (item == null) {
                JOptionPane.showMessageDialog(panel, "No item selected!");
                return;
            }

            if (!isNewItem) {
                //Clear fields
                txtInvNum.setText(String.valueOf(getNextInventoryNumber(itemListModel)));
                txtName.setText("");
                txtStock.setText("0");
                txtMinStock.setText("");
                txtPrice.setText("");
                txtExpiry.setText("");
                txtWarranty.setText("");
                rbtnPerishable.setSelected(false);
                rbtnNonPerishable.setSelected(false);

            int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure you want to delete \"" + item.getName() + "\"?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                itemListModel.removeElement(item);

                //Clearing each fields
                txtInvNum.setText("");
                txtName.setText("");
                txtStock.setText("");
                txtMinStock.setText("");
                txtPrice.setText("");
                txtExpiry.setText("");
                txtWarranty.setText("");
                rbtnPerishable.setSelected(false);
                rbtnNonPerishable.setSelected(false);
            }
        }
    });

        //This is 2 example items used for testing ofc
        itemListModel.addElement(new PerishableItem(1001, "Milk", 25, 12.50, 5, java.time.LocalDate.now().plusDays(5)));
        itemListModel.addElement(new NonPerishableItem(2002, "Laptop", 5, 850.00, 1, 365));

        return panel;
    }

     // SALES TAB
    private JPanel createSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top controls: item selection + quantity + buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        cbSaleItems = new JComboBox<>();
        reloadSalesComboBox(); // load items from inventory list

        txtSaleQty = new JTextField(5);

        btnSaleAdd = new JButton("Add");
        btnSaleRemove = new JButton("Remove");
        btnSaleComplete = new JButton("Complete Sale");

        JButton btnRefreshItems = new JButton("Refresh Items");

        topPanel.add(new JLabel("Item:"));
        topPanel.add(cbSaleItems);
        topPanel.add(new JLabel("Quantity:"));
        topPanel.add(txtSaleQty);
        topPanel.add(btnSaleAdd);
        topPanel.add(btnSaleRemove);
        topPanel.add(btnSaleComplete);
        topPanel.add(btnRefreshItems);

        // Table for sale line items
        String[] cols = {"Inventory #", "Item Name", "Quantity", "Unit Price", "Line Total"};
        saleTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // don't allow manual edits
            }
        };
        tblSaleLines = new JTable(saleTableModel);
        JScrollPane tableScroll = new JScrollPane(tblSaleLines);

        // Bottom: total
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JLabel("Sale Total:"));
        txtSaleTotal = new JTextField(10);
        txtSaleTotal.setEditable(false);
        bottomPanel.add(txtSaleTotal);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Listeners 

        btnRefreshItems.addActionListener(e -> reloadSalesComboBox());

        btnSaleAdd.addActionListener(e -> {
            try {
                handleAddSaleItem();
            } catch (InvalidEntryException | NotEnoughStockException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid numeric quantity.",
                        "Invalid Quantity",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnSaleRemove.addActionListener(e -> handleRemoveSaleItem());

        btnSaleComplete.addActionListener(e -> {
            try {
                handleCompleteSale();
            } catch (NotEnoughStockException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Not Enough Stock",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        return panel;
    }

    // Load items from the inventory JList into the sales combo box
    private void reloadSalesComboBox() {
        if (cbSaleItems == null) return;
        cbSaleItems.removeAllItems();
        if (itemListModel == null) return;

        for (int i = 0; i < itemListModel.size(); i++) {
            cbSaleItems.addItem(itemListModel.get(i));
        }
    }

    private void handleAddSaleItem() throws InvalidEntryException, NotEnoughStockException {
        Item selectedItem = (Item) cbSaleItems.getSelectedItem();
        if (selectedItem == null) {
            throw new InvalidEntryException("No item selected.");
        }

        String qtyText = txtSaleQty.getText().trim();
        if (qtyText.isEmpty()) {
            throw new InvalidEntryException("Quantity cannot be empty.");
        }

        int quantity = Integer.parseInt(qtyText);
        if (quantity <= 0) {
            throw new InvalidEntryException("Quantity must be a positive number.");
        }

        // check stock before adding
        if (quantity > selectedItem.getAmtInStock()) {
            throw new NotEnoughStockException("Not enough stock for this sale.");
        }

        double unitPrice = selectedItem.calculatePrice();
        double lineTotal = unitPrice * quantity;

        // add to table
        saleTableModel.addRow(new Object[]{
                selectedItem.getInventoryNum(),
                selectedItem.getName(),
                quantity,
                unitPrice,
                lineTotal
        });

        // add to current sale structure
        SaleItem saleItem = new SaleItem(selectedItem.getInventoryNum(), selectedItem.getName(), unitPrice, quantity);
        currentSaleItems.add(saleItem);

        recalculateSaleTotal();
        txtSaleQty.setText("");
    }

    private void handleRemoveSaleItem() {
        int row = tblSaleLines.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a line item to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (row >= 0 && row < currentSaleItems.size()) {
            currentSaleItems.remove(row);
        }
        saleTableModel.removeRow(row);
        recalculateSaleTotal();
    }

    private void handleCompleteSale() throws NotEnoughStockException {
        if (currentSaleItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no line items in this sale.", "Empty Sale", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // combine quantities per item
        Map<Long, Integer> qtyPerItem = new HashMap<>();
        for (SaleItem si : currentSaleItems) {
            long inv = si.getInventoryNum();
            int q = (int) si.getQuantity();
            qtyPerItem.put(inv, qtyPerItem.getOrDefault(inv, 0) + q);
        }

        // final stock check
        for (Map.Entry<Long, Integer> entry : qtyPerItem.entrySet()) {
            long invNum = entry.getKey();
            int needed = entry.getValue();

            Item item = findItemByInventoryNum(invNum);
            if (item == null) continue;
            if (needed > item.getAmtInStock()) {
                throw new NotEnoughStockException("Not enough stock to complete sale for item: " + item.getName());
            }
        }

        // deplete stock using Item.depleteStock()
        for (Map.Entry<Long, Integer> entry : qtyPerItem.entrySet()) {
            long invNum = entry.getKey();
            int needed = entry.getValue();

            Item item = findItemByInventoryNum(invNum);
            if (item != null) {
                item.depleteStock(needed);
            }
        }

        // create Sale object and store in history
        ArrayList<SaleItem> copy = new ArrayList<>(currentSaleItems);
        Sale sale = new Sale(copy);
        completedSales.add(sale);

        JOptionPane.showMessageDialog(this, "Sale completed.\nInvoice: " + sale.getInvoiceNumber() + "\nTotal: $" + String.format("%.2f", sale.calculateTotal()), "Sale Completed", JOptionPane.INFORMATION_MESSAGE);

        // clear sale
        currentSaleItems.clear();
        saleTableModel.setRowCount(0);
        txtSaleTotal.setText("");
        txtSaleQty.setText("");

        // refresh inventory list display (stock changed text)
        if (itemList != null) {
            itemList.repaint();
        }
    }

    private void recalculateSaleTotal() {
        double total = 0.0;
        for (int i = 0; i < saleTableModel.getRowCount(); i++) {
            Object val = saleTableModel.getValueAt(i, 4); // Line Total column
            if (val instanceof Number) {
                total += ((Number) val).doubleValue();
            } else {
                try {
                    total += Double.parseDouble(val.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        txtSaleTotal.setText(String.format("%.2f", total));
    }

    private Item findItemByInventoryNum(long invNum) {
        if (itemListModel == null) return null;
        for (int i = 0; i < itemListModel.size(); i++) {
            Item it = itemListModel.get(i);
            if (it.getInventoryNum() == invNum) return it;
        }
        return null;
    }

    // checked exceptions for the assignment
    static class NotEnoughStockException extends Exception {
        public NotEnoughStockException(String msg) {
            super(msg);
        }
    }

    static class InvalidEntryException extends Exception {
        public InvalidEntryException(String msg) {
            super(msg);
        }
    }

    public static void main(String[] args) {
        new InventoryGUI();
    }
}
