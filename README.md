# AWS Lambda API Gateway Servlet

This util allows for mapping any HttpServletRequest and HttpServletResponse to and from API gateway classes

## Installation
TODO

## Usage
Example with javalin and aws lambda handler:
```kotlin
class HelloHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    companion object{
        val app = Javalin.createStandalone()
        app.get("/") { ctx -> ctx.result("Hello World") }
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context) = app.servlet().serve(input)
}

```
