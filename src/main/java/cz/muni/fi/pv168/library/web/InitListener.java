package cz.muni.fi.pv168.library.web;

import cz.muni.fi.pv168.library.*;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

/**
 * @author Lenka (433591)
 * @version 16.04.2016
 */
public class InitListener implements ServletContextListener {

    @Resource(name = "jdbc/bookDB")
    private DataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BookManager bookManager = new BookManagerImpl(dataSource);
        CustomerManager customerManager = new CustomerManagerImpl(dataSource);
        LeaseManager leaseManager = new LeaseManagerImpl(dataSource, bookManager, customerManager);

        ServletContext servletContext = sce.getServletContext();

        servletContext.setAttribute("customerManager", customerManager);
        servletContext.setAttribute("bookManager", bookManager);
        servletContext.setAttribute("leaseManager", leaseManager);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
