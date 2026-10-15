package listener;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import util.OrderStatus;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Quét đơn SEPAY hết hạn mỗi 1 phút và thực hiện hủy + hoàn kho.
 * Điều kiện UPDATE có điều kiện (WHERE payment_status = 'Chờ thanh toán online')
 * được thực thi bên trong cancelAndRestoreStock → tránh tranh chấp với webhook.
 */
@WebListener
public class SePayExpirationListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sepay-expiration-thread");
            t.setDaemon(true);
            return t;
        });
        // Bắt đầu sau 1 phút, lặp mỗi 1 phút
        scheduler.scheduleAtFixedRate(this::processExpiredOrders, 1, 1, TimeUnit.MINUTES);
    }

    private void processExpiredOrders() {
        try {
            OrderDAO orderDAO = new OrderDAOImpl();
            List<Integer> expiredIds = orderDAO.findExpiredSepayOrderIds();
            for (int orderId : expiredIds) {
                try {
                    // cancelAndRestoreStock dùng UPDATE có điều kiện bên trong
                    int rows = orderDAO.cancelAndRestoreStock(
                            orderId,
                            OrderStatus.PAY_EXPIRED_SEPAY,
                            OrderStatus.ORDER_CANCELLED);
                    if (rows == 1) {
                        System.out.println("[SePayExpiration] Đơn " + orderId + " hết hạn — đã hoàn kho.");
                    }
                    // rows == 0 nghĩa là webhook vừa xử lý xong trước → bỏ qua
                } catch (Exception e) {
                    // Bắt từng đơn riêng để không dừng vòng lặp
                    System.err.println("[SePayExpiration] Lỗi xử lý đơn " + orderId + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Bắt tất cả exception để luồng không chết
            System.err.println("[SePayExpiration] Lỗi quét đơn hết hạn: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
