package cz.muni.fi.pv168.library.web;

import cz.muni.fi.pv168.library.Customer;
import cz.muni.fi.pv168.library.CustomerManager;
import cz.muni.fi.pv168.library.EntityNotFoundException;
import cz.muni.fi.pv168.library.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @surname Lenka (433591)
 * @version 16.04.2016
 */
@WebServlet(name = "CustomerController", urlPatterns = {"/Customer/*"})
public class CustomerController extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/customers";

    private final static Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();
        String name, surname, address, phoneNumber;
        Long id;
        switch(action) {
            case "/add":
                //načtení POST parametrů z formuláře
                name = request.getParameter("name");
                surname = request.getParameter("surname");
                address = request.getParameter("address");
                phoneNumber = request.getParameter("phoneNumber");

                //zpracování dat - vytvoření záznamu v databázi
                try {
                    Customer customer = new Customer(name, surname, phoneNumber, address);
                    getCustomerManager().createCustomer(customer);
                    log.debug("created {}",customer);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (IllegalArgumentException e) {
                    request.setAttribute("chyba", "Je nutné vyplnit všetky položky!");
                    listCustomers(request, response);
                    return;
                }
                catch (ServiceFailureException e) {
                    log.error("Cannot add customer", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                id = Long.valueOf(request.getParameter("id"));
                name = request.getParameter("name");
                surname = request.getParameter("surname");
                address = request.getParameter("address");
                phoneNumber = request.getParameter("phoneNumber");
                Customer customer;
                try {
                    customer = getCustomerManager().findCustomerById(id);
                } catch (ServiceFailureException | EntityNotFoundException e) {
                    log.error("Cannot find customer", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
                if (name != null || name.length() == 0 ) {
                    customer.setName(name);
                }
                if (surname != null || surname.length() == 0 ) {
                    customer.setSurname(surname);
                }
                if (address != null || address.length() == 0 ) {
                    customer.setAddress(address);
                }
                if (phoneNumber != null || phoneNumber.length() == 0 ) {
                    customer.setPhoneNumber(phoneNumber);
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    getCustomerManager().updateCustomer(customer);
                    log.debug("updated {}",customer);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    log.error("Cannot update customer", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }


            case "/delete":
                try {
                    id = Long.valueOf(request.getParameter("id"));
                    if (id == null) {
                        request.setAttribute("chyba", "Je nutné vyplnit id!");
                        listCustomers(request, response);
                        return;
                    }
                    getCustomerManager().deleteCustomer(id);
                    log.debug("deleted customer {}",id);
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException | EntityNotFoundException e) {
                    log.error("Cannot delete customer", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }

            case "/list":
                listCustomers(request,response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Unknown action: " + action);
        }

    }

    private CustomerManager getCustomerManager() {

        return (CustomerManager) getServletContext().getAttribute("customerManager");
    }

    private void listCustomers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(getCustomerManager());
        List<Customer> customers = getCustomerManager().findAllCustomers();
        request.setAttribute("customers", customers);
        request.getRequestDispatcher("/WEB-INF/jsp/customer/list.jsp").forward(request, response);
    }

}
