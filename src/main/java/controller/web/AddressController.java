package controller.web;

import dao.AddressDAO;
import dao.Impl.AddressDAOImpl;
import model.AddressObject;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/customer/address"})
public class AddressController extends HttpServlet {

    private AddressDAO addressDAO = new AddressDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserObject userSession = (UserObject) session.getAttribute("user");

        if (userSession == null) {
            resp.sendRedirect(req.getContextPath() + "/public/login");
            return;
        }

        // Lấy danh sách địa chỉ của user này
        List<AddressObject> addresses = addressDAO.getAddressesByUserId(userSession.getUserId());
        req.setAttribute("addressList", addresses);

        // Chuyển hướng tới trang JSP sổ địa chỉ
        req.getRequestDispatcher("/views/web/address.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        UserObject userSession = (UserObject) session.getAttribute("user");

        if (userSession == null) {
            resp.sendRedirect(req.getContextPath() + "/public/login");
            return;
        }

        String action = req.getParameter("action"); // Nhận biết đang là Thêm hay Sửa
        String receiverName = req.getParameter("receiverName");
        String receiverPhone = req.getParameter("receiverPhone");
        String addressDetail = req.getParameter("addressDetail");
        boolean isDefault = req.getParameter("isDefault") != null; // Nếu checkbox được tích thì sẽ có giá trị

        AddressObject address = new AddressObject();
        address.setUserId(userSession.getUserId());
        address.setReceiverName(receiverName);
        address.setReceiverPhone(receiverPhone);
        address.setAddressDetail(addressDetail);
        address.setDefault(isDefault);

        if ("add".equals(action)) {
            addressDAO.insertAddress(address);
        } else if ("edit".equals(action)) {
            int addressId = Integer.parseInt(req.getParameter("addressId"));
            address.setAddressId(addressId);
            addressDAO.updateAddress(address);
        }

        // Làm xong thì load lại trang địa chỉ
        resp.sendRedirect(req.getContextPath() + "/customer/address");
    }
}