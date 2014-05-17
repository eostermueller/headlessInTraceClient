package example.webapp.servlet;
import java.io.*;
import java.sql.Timestamp;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import example.webapp.dao.interfaces.EventDao;
import example.webapp.entity.Event;
import example.webapp.entity.Location;

public class HelloExecuteQuery extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
    	//ServletContext sc = (ServletContext) WebApplicationContextUtils.getWebApplicationContext();
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    	
        EventDao eventDao = (EventDao) ctx.getBean("eventDao");
        
        int count = eventDao.countAll();
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Hello World!</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Hello World!</h1>");
        out.println("Event count [" + count + "]");
        out.println("</body>");
        out.println("</html>");
        
        
    }

}
