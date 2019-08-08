package se.davison.aws.lambda.apigatewayservlet

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import javax.servlet.http.HttpServlet

fun HttpServlet.serve(event: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
    val response = LambdaHttpServletResponse()
    this.service(LambdaHttpServletRequest(event), response)
    return response.gatewayResponseObject
}