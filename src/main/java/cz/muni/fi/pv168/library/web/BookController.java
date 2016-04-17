package cz.muni.fi.pv168.library.web;

import cz.muni.fi.pv168.library.Book;
import cz.muni.fi.pv168.library.BookManager;
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
 * @author Lenka (433591)
 * @version 16.04.2016
 */
@WebServlet(name = "BookController", urlPatterns = {"/Book/*"})
public class BookController extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/books";

    private final static Logger log = LoggerFactory.getLogger(BookController.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();
        String name, author, genre;
        Long id;
        switch(action) {
            case "/add":
                //načtení POST parametrů z formuláře
                name = request.getParameter("name");
                author = request.getParameter("author");
                genre = request.getParameter("genre");
                //kontrola vyplnění hodnot
                if (name == null || name.length() == 0 ) {
                    request.setAttribute("chyba", "Je nutné vyplnit meno!");
                    listBooks(request, response);
                    return;
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    Book book = new Book(name);
                    book.setAuthor(author);
                    book.setGenre(genre);
                    getBookManager().createBook(book);
                    log.debug("created {}",book);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    log.error("Cannot add book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                id = Long.valueOf(request.getParameter("id"));
                name = request.getParameter("name");
                author = request.getParameter("author");
                genre = request.getParameter("genre");
                Book book;
                try {
                    book = getBookManager().findBookById(id);
                } catch (ServiceFailureException | EntityNotFoundException e) {
                    log.error("Cannot find book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
                if (name != null || name.length() == 0 ) {
                    book.setName(name);
                }
                if (author != null || author.length() == 0 ) {
                    book.setAuthor(author);
                }
                if (genre != null || genre.length() == 0 ) {
                    book.setGenre(genre);
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    getBookManager().updateBook(book);
                    log.debug("updated {}",book);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    log.error("Cannot update book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }


            case "/delete":
                try {
                    id = Long.valueOf(request.getParameter("id"));
                    if (id == null) {
                        request.setAttribute("chyba", "Je nutné vyplnit id!");
                        listBooks(request, response);
                        return;
                    }
                    getBookManager().deleteBook(id);
                    log.debug("deleted book {}",id);
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException | EntityNotFoundException e) {
                    log.error("Cannot delete book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }

            case "/list":
                listBooks(request,response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Unknown action: " + action);
        }

    }

    private BookManager getBookManager() {
        
        return (BookManager) getServletContext().getAttribute("bookManager");
    }

    private void listBooks(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(getBookManager());
        List<Book> books = getBookManager().findAllBooks();
        request.setAttribute("books", books);
        request.getRequestDispatcher("/WEB-INF/jsp/book/list.jsp").forward(request, response);
    }

}
