package webapp.webpresentation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import webapp.services.ApplicationException;
import webapp.services.SaleService;
import webapp.services.SalesDTO;

/**
 * Created by tester to remove Sales created in tests
 * @author Alexandre Figueiredo fc570999
 */
@WebServlet("/RemoveSalePageController")
public class RemoveSalePageController extends PageController{
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		SaleService ss = SaleService.INSTANCE;
	
		SalesHelper sh = new SalesHelper();
		request.setAttribute("salesHelper", sh);
		try{
			String id = request.getParameter("id");
			String vat = request.getParameter("vat");
			
			if (isInt(sh, id, "Invalid sale id") && isInt(sh,vat, "Invalid VAT number")) {
				int saleId = intValue(id);
				int vatNumber = intValue(vat);
				ss.removeSale(vatNumber, saleId);
				SalesDTO s = ss.getSaleByCustomerVat(vatNumber);
				sh.fillWithSales(s.sales);
				request.getRequestDispatcher("SalesInfo.jsp").forward(request, response);
			}
		} catch (ApplicationException e) {
			sh.addMessage("It was not possible to fulfill the request: " + e.getMessage());
			request.getRequestDispatcher("CustomerError.jsp").forward(request, response); 
		}
	}
}
