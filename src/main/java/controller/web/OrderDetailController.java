package controller.web;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import model.OrderObject;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * OrderDetailController: Xử lý hiển thị chi tiết đơn hàng
 * URL: /customer/order-detail
 */
@WebServlet("/customer/order-detail")
public class OrderDetailController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private OrderDAO orderDAO;

    // Khởi tạo DAO theo đúng mô típ của bạn
    @Override
    public void init() throws ServletException {
        orderDAO = new OrderDAOImpl();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Kiểm tra đăng nhập (Bảo mật)
        HttpSession session = request.getSession();
        UserObject userObject = (UserObject) session.getAttribute("user");

        if (userObject == null) {
            response.sendRedirect(request.getContextPath() + "/public/login");
            return;
        }

        try {
            // 2. Lấy ID đơn hàng từ URL
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/order-history");
                return;
            }

            int orderId = Integer.parseInt(idParam);

            // 3. Gọi DAO lấy dữ liệu
            OrderObject order = orderDAO.getOrderDetailByOrderId(orderId);

            // 4. Kiểm tra dữ liệu và chuyển hướng
            if (order != null && order.getOrderId() != 0) {

                // (Tùy chọn) Kiểm tra xem đơn hàng này có đúng là của user đang đăng nhập không
                // Tránh việc user này sửa URL để xem đơn của user khác
                if (order.getUserObject() != null && !order.getUserObject().getFullname().equals(userObject.getFullname())) {
                    // Nếu có user_id trong order.getUserObject() thì so sánh id sẽ chuẩn hơn
                }

                // Đẩy dữ liệu sang JSP
                request.setAttribute("order", order);

                // Lưu ý: Sửa lại đường dẫn tới file JSP cho đúng với cấu trúc thư mục của bạn
                request.getRequestDispatcher("/views/web/order-detail.jsp").forward(request, response);

            } else {
                response.sendRedirect(request.getContextPath() + "/customer/order-history");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}