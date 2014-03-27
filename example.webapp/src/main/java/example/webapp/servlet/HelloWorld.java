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

public class HelloWorld extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
    	//ServletContext sc = (ServletContext) WebApplicationContextUtils.getWebApplicationContext();
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        EventDao eventDao = (EventDao) ctx.getBean("eventDao");
        Event event = null;
        StringBuilder sbStackTrace = null;
        for (int i = 0; i < 5 ; i++ ) {
            Location location = new Location();
            location.setAddress("1 high street");
            location.setName("location-"+i);

            event = new Event();
            event.setName("event name-"+i);
            event.setDescription("description");
            event.setLocation(location);
            event.setDate(new Timestamp(System.currentTimeMillis()));

            event = eventDao.save(event);

            System.out.println(event);
    		sbStackTrace = new StringBuilder();
    		for(StackTraceElement ele : Thread.currentThread().getStackTrace()) {
    			sbStackTrace.append( ele.toString());
    			sbStackTrace.append("\n");
    		}
        	
        }

        
    	
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Hello World!</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Hello World!</h1>");
        out.println("Stack trace [" + sbStackTrace.toString() + "]");
        out.print("<p>Date: " + event.getDate());
        out.println("</body>");
        out.println("</html>");
    }

//	private String getStackTraceElementAsString(StackTraceElement[] stackTrace) {
//		StringBuilder sb = new StringBuilder();
//		for(StackTraceElement ele : stackTrace) {
//			sb.append( ele.toString());
//			sb.append("\n");
//		}
//		return sb.toString();
//	}
}
