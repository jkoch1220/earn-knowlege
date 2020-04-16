package com.rewe.digital.offerdetailsservice.actuate

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint
import org.springframework.stereotype.Component

@Component
@WebEndpoint(id = "healthcheck")
class HealthCheckEndpoint {
    @ReadOperation
    fun health(): WebEndpointResponse<Void> = WebEndpointResponse(WebEndpointResponse.STATUS_OK)
}
