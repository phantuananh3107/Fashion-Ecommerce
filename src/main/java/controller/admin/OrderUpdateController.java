package controller.admin;

import dao.Impl.OrderDAOImpl;
import dao.OrderDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/admin-update-order")
public class OrderUpdateController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ĐÃ SỬA BƯỚC 1: Bắt buộc phải có dòng này để Servlet đọc được tiếng Việt có dấu (như "Chờ xử lý")
        request.setCharacterEncoding("UTF-8");

        String orderIdParam = request.getParameter("orderId");
        String userIdParam = request.getParameter("userId");
        String paymentStatus = request.getParameter("paymentStatus");
        String orderStatus = request.getParameter("orderStatus");

        if(orderIdParam != null && paymentStatus != null && orderStatus != null) {
            int orderId = Integer.parseInt(orderIdParam);

            // Xử lý an toàn nếu userId bị rỗng
            int userId = 0;
            if (userIdParam != null && !userIdParam.isEmpty()) {
                userId = Integer.parseInt(userIdParam);
            }

            OrderDAO orderDAO = new OrderDAOImpl();

            // Biến check này sẽ = true nếu DB cập nhật thành công, = false nếu thất bại
            boolean check = orderDAO.updateOrder(orderId, userId, orderStatus, paymentStatus);

            // ĐÃ SỬA BƯỚC 2: Check kỹ kết quả trước khi Redirect
            if (check) {
                response.sendRedirect(request.getContextPath() + "/admin/admin-order-edit?orderId=" + orderId + "&status=success");
            } else {
                // Nếu DB không update được thì báo fail (Bạn có thể thêm alert màu đỏ bên JSP sau nếu muốn)
                response.sendRedirect(request.getContextPath() + "/admin/admin-order-edit?orderId=" + orderId + "&status=fail");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/admin-order-list");
        }
    }
}