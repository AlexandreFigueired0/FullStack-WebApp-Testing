package webapp.webpresentation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import webapp.persistence.AddressFinder;
import webapp.persistence.PersistenceException;
import webapp.services.AddressesDTO;
import webapp.services.ApplicationException;
import webapp.services.CustomerService;
import webapp.services.SaleService;
import webapp.services.SalesDTO;
import webapp.services.SalesDeliveryDTO;

@WebServlet("/RemoveDeliveryPageController")
public class RemoveDeliveryPageController extends PageController{
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CustomerService cs = CustomerService.INSTANCE;
		SaleService ss = SaleService.INSTANCE;
		
		AddressesHelper ash = new AddressesHelper();
		request.setAttribute("addressesHelper", ash);
		SalesHelper ssh = new SalesHelper();
		request.setAttribute("salesHelper", ssh);
		SalesDeliveryHelper sdh = new SalesDeliveryHelper();
		request.setAttribute("saleDeliveryHelper", sdh);
		
		try{
			String vat = request.getParameter("vat");
			String delivery = request.getParameter("delivery_id");
			
			if(vat != null && delivery != null) {
				if (isInt(sdh, vat, "Invalid VAT ") && isInt(sdh, delivery, "Invalid Delivery Id")) {
					int deliveryId = intValue(delivery);
					int customerVat = intValue(vat);
					ss.removeDelivery(customerVat, deliveryId);
					SalesDeliveryDTO sdd = ss.getSalesDeliveryByVat(customerVat);
					sdh.fillWithSalesDelivery(sdd.sales_delivery); 
					request.getRequestDispatcher("SalesDeliveryInfo.jsp").forward(request, response);
				}
			}

		} catch (ApplicationException e) {
			sdh.addMessage("It was not possible to fulfill the request: " + e.getMessage());
			request.getRequestDispatcher("CustomerError.jsp").forward(request, response); 
		}
	}
}
