package controller.web;

import dao.Impl.VoucherDAOImpl;
import dao.VoucherDAO;
import model.VoucherObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/check-voucher")
public class CheckVoucherController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        VoucherDAO voucherDAO = new VoucherDAOImpl();
        VoucherObject v = voucherDAO.getVoucherByCode(code);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (v != null) {
            // Trả về số tiền giảm nếu mã đúng
            response.getWriter().write("{\"success\": true, \"discount\": " + v.getDiscountAmount() + "}");
        } else {
            // Báo lỗi nếu mã sai hoặc hết hạn
            response.getWriter().write("{\"success\": false, \"message\": \"Mã không hợp lệ hoặc đã hết lượt!\"}");
        }
    }
}