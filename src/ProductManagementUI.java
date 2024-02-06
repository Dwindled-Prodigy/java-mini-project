import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ProductManagementUI extends JFrame {
    private JTextField productIdField, productNameField, priceField, quantityField, usernameField, passwordField;
    private JButton addProductButton, showProductsButton, updateProductButton, deleteProductButton, loginButton, addProductToOrderButton, placeOrderButton;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private ProductDao productDao;
    private UserDao userDao;
    private int selectedProductId;
    private User loggedInUser;
    private OrderDao orderDao;
    private JPanel buttonPanel;

    public ProductManagementUI() {
        setTitle("Product Management");
        setSize(800, 600);
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        productDao = new ProductDao();  // Initialize ProductDao
        userDao = new UserDao();        // Initialize UserDao
        orderDao = new OrderDao();
        initComponents();
        addComponents();
        selectedProductId = -1; // Initialize with an invalid value
        loggedInUser = null;
        showLoginPanel();
    }   

    private void addProductToOrder() {
        if (loggedInUser != null) {
            if (selectedProductId != -1) {
                try {
                    int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter quantity:"));
                    if (quantity > 0) {
                        Product selectedProduct = productDao.getProductById(selectedProductId);
                        double subtotal = selectedProduct.getPrice() * quantity;

                        int orderId = orderDao.createOrder(loggedInUser.getUserId(), subtotal);
                        orderDao.addOrderItem(orderId, selectedProductId, quantity, subtotal);

                        JOptionPane.showMessageDialog(this, "Product added to order successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a positive value.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a numeric value.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please log in to add products to the order.");
        }
    }


    private void initComponents() {
        productIdField = new JTextField(5);
        productNameField = new JTextField(20);
        priceField = new JTextField(10);
        quantityField = new JTextField(5);
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        // Add this button in initComponents method
        JButton showUserOrdersButton = new JButton("Show My Orders");
        showUserOrdersButton.addActionListener(e -> showUserOrders());

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());

        addProductToOrderButton = new JButton("Add Product to Order");
        addProductToOrderButton.addActionListener(e -> addProductToOrder());

        placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> placeOrder());

        addProductButton = new JButton("Add Product");
        addProductButton.addActionListener(e -> addProduct());

        showProductsButton = new JButton("Show Products");
        showProductsButton.addActionListener(e -> showProducts());

        updateProductButton = new JButton("Update Product");
        updateProductButton.addActionListener(e -> updateProduct());

        deleteProductButton = new JButton("Delete Product");
        deleteProductButton.addActionListener(e -> deleteProduct());

        tableModel = new DefaultTableModel();
        productTable = new JTable(tableModel);
        tableModel.addColumn("Product ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Price");
        tableModel.addColumn("Quantity");

        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addProductButton);
        buttonPanel.add(showProductsButton);
        buttonPanel.add(updateProductButton);
        buttonPanel.add(deleteProductButton);
        buttonPanel.add(showUserOrdersButton);
        buttonPanel.add(addProductToOrderButton); // Add the new button

        // Add a mouse listener to handle row selection
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedProductId = (int) productTable.getValueAt(selectedRow, 0);
                    productNameField.setText((String) productTable.getValueAt(selectedRow, 1));
                    priceField.setText(String.valueOf(productTable.getValueAt(selectedRow, 2)));
                    quantityField.setText(String.valueOf(productTable.getValueAt(selectedRow, 3)));
                }
            }
        });
    }
    
    private void placeOrder() {
        if (loggedInUser != null) {
            // Get the selected product and quantity
            Product selectedProduct = productDao.getProductById(selectedProductId);
            int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter quantity:"));
    
            // Calculate subtotal
            double subtotal = selectedProduct.getPrice() * quantity;
    
            // Create an order and get the order_id
            int orderId = orderDao.createOrder(loggedInUser.getUserId(), subtotal);
    
            // Add the order item
            orderDao.addOrderItem(orderId, selectedProductId, quantity, subtotal);
    
            JOptionPane.showMessageDialog(this, "Order placed successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Please log in to place an order.");
        }
    }
    
    private void addComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
    
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Product ID:"));
        inputPanel.add(productIdField);
        inputPanel.add(new JLabel("Product Name:"));
        inputPanel.add(productNameField);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(quantityField);
    
        JPanel loginPanel = new JPanel(new FlowLayout());
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
    
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addProductButton);
        buttonPanel.add(showProductsButton);
        buttonPanel.add(updateProductButton);
        buttonPanel.add(deleteProductButton);
        buttonPanel.add(addProductToOrderButton);
        buttonPanel.add(placeOrderButton);
    
        JScrollPane tableScrollPane = new JScrollPane(productTable);
    
        add(loginPanel);
        add(inputPanel);
        add(tableScrollPane, BorderLayout.CENTER );
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    

    
        private void login() {
        String username = usernameField.getText();
        String password = String.valueOf(((JPasswordField) passwordField).getPassword());
    
        User user = userDao.getUserByUsername(username);
    
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            showMainPanel();
            showProducts(); // Display products after successful login
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }
 


private void showLoginPanel() {
    clearFields();
    getContentPane().removeAll(); // Remove existing components
    setLayout(new BorderLayout());
    add(new JLabel("Please log in to continue."), BorderLayout.NORTH);
    add(new JLabel(" "), BorderLayout.CENTER); // Add space
    add(new JLabel(" "), BorderLayout.SOUTH); // Add space
    addComponents(); // Add login components
    revalidate();
    repaint();
}

// Add this method in ProductManagementUI
private void showUserOrders() {
    if (loggedInUser != null) {
        // Retrieve orders for the logged-in user
        List<Order> userOrders = orderDao.getOrdersByUserId(loggedInUser.getUserId());

        // Display user orders in a dialog or other UI component
        StringBuilder ordersInfo = new StringBuilder("Your Orders:\n");
        for (Order order : userOrders) {
            ordersInfo.append("Order ID: ").append(order.getOrderId()).append(", Total Amount: $")
                    .append(order.getTotalAmount()).append("\n");

            // Display order items
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = productDao.getProductById(orderItem.getProductId());
                ordersInfo.append("   - ").append(product.getName()).append(", Quantity: ")
                        .append(orderItem.getQuantity()).append(", Subtotal: $")
                        .append(orderItem.getSubtotal()).append("\n");
            }

            ordersInfo.append("\n");
        }

        JOptionPane.showMessageDialog(this, ordersInfo.toString(), "Your Orders", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, "Please log in to view your orders.");
    }
}


private void showMainPanel() {
    clearFields();
    getContentPane().removeAll(); // Remove existing components
    setLayout(new BorderLayout());
    addComponents(); // Add main components
    revalidate();
    repaint();
}

    private void addProduct() {
        String productIdText = productIdField.getText();
        String productName = productNameField.getText();
        double price = Double.parseDouble(priceField.getText());
        int quantity = Integer.parseInt(quantityField.getText());

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO Product (product_id, name, price, stock_quantity) VALUES (?, ?, ?, ?)")) {
            if (!productIdText.isEmpty()) {
                int productId = Integer.parseInt(productIdText);
                preparedStatement.setInt(1, productId);
            } else {
                preparedStatement.setNull(1, java.sql.Types.INTEGER);
            }

            preparedStatement.setString(2, productName);
            preparedStatement.setDouble(3, price);
            preparedStatement.setInt(4, quantity);
            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product added successfully!");
            clearFields();
            showProducts(); // Refresh the table after adding a product
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage());
        }
    }

    private void updateProduct() {
        if (selectedProductId != -1) {
            String productName = productNameField.getText();
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "UPDATE Product SET name=?, price=?, stock_quantity=? WHERE product_id=?")) {
                preparedStatement.setString(1, productName);
                preparedStatement.setDouble(2, price);
                preparedStatement.setInt(3, quantity);
                preparedStatement.setInt(4, selectedProductId);
                preparedStatement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                clearFields();
                showProducts(); // Refresh the table after updating a product
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
        }
    }

    private void deleteProduct() {
        if (selectedProductId != -1) {
            int confirmResult = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirmResult == JOptionPane.YES_OPTION) {
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(
                             "DELETE FROM Product WHERE product_id=?")) {
                    preparedStatement.setInt(1, selectedProductId);
                    preparedStatement.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    clearFields();
                    showProducts(); // Refresh the table after deleting a product
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
        }
    }

    private void showProducts() {
        // Clear existing table data
        tableModel.setRowCount(0);

        // Retrieve products from the database
        List<Product> productList = productDao.getAllProducts();

        // Populate the table with product data
        for (Product product : productList) {
            Object[] rowData = {product.getProductId(), product.getName(), product.getPrice(), product.getStockQuantity()};
            tableModel.addRow(rowData);
        }

        // Reset selected product ID
        selectedProductId = -1;
    }

    private void clearFields() {
        productIdField.setText("");
        productNameField.setText("");
        priceField.setText("");
        quantityField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductManagementUI().setVisible(true));
    }
}