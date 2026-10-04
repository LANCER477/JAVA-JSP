package step.learning.java231web.services.form;

import jakarta.servlet.http.HttpServletRequest;

public interface IFormParseService {
    FormParseResult parse(HttpServletRequest request);
}
