import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    public int createOrder(int userId, double totalAmount) {
        int orderId = -1;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO OrderTable (user_id, order_date, total_amount) VALUES (?, CURRENT_TIMESTAMP, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setDouble(2, totalAmount);
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderId;
    }

    public void addOrderItem(int orderId, int productId, int quantity, double subtotal) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO OrderItem (order_id, product_id, quantity, subtotal) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setDouble(4, subtotal);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orderList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM OrderTable WHERE user_id = ?")) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Order order = new Order();
                    order.setOrderId(resultSet.getInt("order_id"));
                    order.setUserId(resultSet.getInt("user_id"));
                    order.setOrderDate(resultSet.getTimestamp("order_date"));
                    order.setTotalAmount(resultSet.getDouble("total_amount"));

                    // Include order items for each order
                    order.setOrderItems(getOrderItemsByOrderId(order.getOrderId()));

                    orderList.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderList;
    }

    private List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM OrderItem WHERE order_id = ?")) {
            preparedStatement.setInt(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderItemId(resultSet.getInt("order_item_id"));
                    orderItem.setOrderId(resultSet.getInt("order_id"));
                    orderItem.setProductId(resultSet.getInt("product_id"));
                    orderItem.setQuantity(resultSet.getInt("quantity"));
                    orderItem.setSubtotal(resultSet.getDouble("subtotal"));

                    orderItems.add(orderItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderItems;
    }
}
