package step.learning.java231web.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RestStatusTest {

    @Test
    public void testStandardSuccessStatuses() {
        assertTrue(RestStatus.Ok.isOk());
        assertEquals(200, RestStatus.Ok.getCode());
        assertEquals("OK", RestStatus.Ok.getMessage());

        assertTrue(RestStatus.Created.isOk());
        assertEquals(201, RestStatus.Created.getCode());
        assertEquals("Created", RestStatus.Created.getMessage());

        assertTrue(RestStatus.Accepted.isOk());
        assertEquals(202, RestStatus.Accepted.getCode());

        assertTrue(RestStatus.NoContent.isOk());
        assertEquals(204, RestStatus.NoContent.getCode());
    }

    @Test
    public void testStandardClientErrorStatuses() {
        assertFalse(RestStatus.BadRequest.isOk());
        assertEquals(400, RestStatus.BadRequest.getCode());
        assertEquals("Bad Request", RestStatus.BadRequest.getMessage());

        assertFalse(RestStatus.Unauthorized.isOk());
        assertEquals(401, RestStatus.Unauthorized.getCode());

        assertFalse(RestStatus.Forbidden.isOk());
        assertEquals(403, RestStatus.Forbidden.getCode());

        assertFalse(RestStatus.NotFound.isOk());
        assertEquals(404, RestStatus.NotFound.getCode());

        assertFalse(RestStatus.MethodNotAllowed.isOk());
        assertEquals(405, RestStatus.MethodNotAllowed.getCode());

        assertFalse(RestStatus.Conflict.isOk());
        assertEquals(409, RestStatus.Conflict.getCode());

        assertFalse(RestStatus.UnprocessableEntity.isOk());
        assertEquals(422, RestStatus.UnprocessableEntity.getCode());

        assertFalse(RestStatus.TooManyRequests.isOk());
        assertEquals(429, RestStatus.TooManyRequests.getCode());
    }

    @Test
    public void testStandardServerErrorStatuses() {
        assertFalse(RestStatus.InternalServerError.isOk());
        assertEquals(500, RestStatus.InternalServerError.getCode());

        assertFalse(RestStatus.NotImplemented.isOk());
        assertEquals(501, RestStatus.NotImplemented.getCode());

        assertFalse(RestStatus.BadGateway.isOk());
        assertEquals(502, RestStatus.BadGateway.getCode());

        assertFalse(RestStatus.ServiceUnavailable.isOk());
        assertEquals(503, RestStatus.ServiceUnavailable.getCode());
    }

    @Test
    public void testNonStandardStatusesFromLecture() {
        assertFalse(RestStatus.HeaderRequired.isOk());
        assertEquals(440, RestStatus.HeaderRequired.getCode());
        assertEquals("Header Required", RestStatus.HeaderRequired.getMessage());

        assertFalse(RestStatus.HeaderMalformed.isOk());
        assertEquals(441, RestStatus.HeaderMalformed.getCode());
        assertEquals("Header Malformed", RestStatus.HeaderMalformed.getMessage());

        assertFalse(RestStatus.QueryParameterRequired.isOk());
        assertEquals(442, RestStatus.QueryParameterRequired.getCode());
        assertEquals("Query Parameter Required", RestStatus.QueryParameterRequired.getMessage());
    }

    @Test
    public void testProposedNonStandardStatusesForHomework() {
        assertFalse(RestStatus.QueryParameterMalformed.isOk());
        assertEquals(443, RestStatus.QueryParameterMalformed.getCode());

        assertFalse(RestStatus.TokenRequired.isOk());
        assertEquals(444, RestStatus.TokenRequired.getCode());

        assertFalse(RestStatus.TokenExpired.isOk());
        assertEquals(445, RestStatus.TokenExpired.getCode());

        assertFalse(RestStatus.TokenMalformed.isOk());
        assertEquals(446, RestStatus.TokenMalformed.getCode());

        assertFalse(RestStatus.ValidationFailed.isOk());
        assertEquals(447, RestStatus.ValidationFailed.getCode());

        assertFalse(RestStatus.LoginOccupied.isOk());
        assertEquals(448, RestStatus.LoginOccupied.getCode());

        assertFalse(RestStatus.AccountLocked.isOk());
        assertEquals(449, RestStatus.AccountLocked.getCode());

        assertFalse(RestStatus.DatabaseError.isOk());
        assertEquals(550, RestStatus.DatabaseError.getCode());

        assertFalse(RestStatus.ExternalServiceUnavailable.isOk());
        assertEquals(551, RestStatus.ExternalServiceUnavailable.getCode());
    }

    @Test
    public void testCustomStatusCreationAndChaining() {
        RestStatus custom = new RestStatus()
                .setOk(true)
                .setCode(200)
                .setMessage("Custom OK");

        assertTrue(custom.isOk());
        assertEquals(200, custom.getCode());
        assertEquals("Custom OK", custom.getMessage());

        RestStatus another = new RestStatus(true, 200, "Custom OK");
        assertEquals(custom, another);
        assertEquals(custom.hashCode(), another.hashCode());
        assertTrue(custom.toString().contains("Custom OK"));
    }
}
