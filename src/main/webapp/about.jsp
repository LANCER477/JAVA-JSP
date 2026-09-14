<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>About servlets</h1>
        <h2>Сервлети - JSP контролери</h2>
        <p>
            Сервлети - спеціалізовані класи для серверних (мережних)
            задач. Дочірні класи НТТР-призначення грають роль 
            контролерів. Описуються в кодовій частині проєкту 
            (тоді як JSP - у webapp). Підключаються до сервера
            одним з трьох варіантів:
        </p>
        <ul>
            <li>
                За допомогою інструкцій у web.xml
        <pre>
        <!-- Аналог інструкції homeServlet = new HomeServlet()  -->
        &lt;servlet&gt;
            &lt;servlet-name&gt;homeServlet&lt;/servlet-name&gt;
            &lt;servlet-class&gt;step.learning.java231web.servlets.HomeServlet&lt;/servlet-class&gt;
        &lt;/servlet&gt;

        &lt;servlet-mapping&gt;
            &lt;servlet-name&gt;homeServlet&lt;/servlet-name&gt;
            &lt;url-pattern&gt;&lt;/url-pattern&gt;
        &lt;/servlet-mapping&gt;
        </pre>
            </li>
            <li>
                За допомогою анотацій @WebServlet перед описом класу:<br/>
                @WebServlet("/about")<br/>
                public class AboutServlet extends HttpServlet {...
            </li>
            <li>
                Як частина інверсії управління
            </li>            
        </ul>
        <h2>Передача даних</h2>
        <p>
            Для передачі даних від сервлету до представлення (JSP)
            можна скористатись методом 
            <code>req.setAttribute("key", Value);</code>.
            У преставлення дані вилучаються методом 
            <code>request.getAttribute("key")</code>.
            Дана технологія також може використовуватись для задач
            шаблонізації (передаючи назву представлення), або для 
            контролю проходження сервлету, оскільки представлення
            самі по собі є активними і можуть оброблятись сервером.
            <br/>
            Контроль проходження сервлету: <%= request.getAttribute("servlet") %>
        </p>
    </body>
</html>
