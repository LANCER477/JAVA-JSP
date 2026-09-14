<%@page contentType="text/html" pageEncoding="UTF-8"%>
<div class="card">
    <h2>About servlets</h2>
    <h3>Сервлети - JSP контролери</h3>
    <p>
        Сервлети - спеціалізовані класи для серверних (мережних)
        задач. Дочірні класи НТТР-призначення грають роль 
        контролерів. Описуються в кодовій частині проєкту 
        (тоді як JSP - у webapp). Підключаються до сервера
        одним з трьох варіантів:
    </p>
    <ul>
        <li>
            За допомогою інструкцій у <code>web.xml</code>
            <pre>
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
            За допомогою анотацій <code>@WebServlet</code> перед описом класу:<br/>
            <code>@WebServlet("/about")</code><br/>
            <code>public class AboutServlet extends HttpServlet {...</code>
        </li>
        <li>
            Як частина інверсії управління (IoC / Guice)
        </li>            
    </ul>
    <h3>Передача даних</h3>
    <p>
        Для передачі даних від сервлету до представлення (JSP)
        можна скористатись методом 
        <code>req.setAttribute("key", Value);</code>.
        У представлення дані вилучаються методом 
        <code>request.getAttribute("key")</code>.
        Дана технологія також може використовуватись для задач
        шаблонізації (передаючи назву представлення), або для 
        контролю проходження сервлету, оскільки представлення
        самі по собі є активними і можуть оброблятись сервером.
        <br/><br/>
        Контроль проходження сервлету: <b><%= request.getAttribute("servlet") %></b>
    </p>
</div>
