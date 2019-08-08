# AWS Lambda API Gateway Servlet

This util allows for mapping any HttpServletRequest and HttpServletResponse to and from API gateway classes

## Installation
1. Add Jitpack to repos in gradle.build
    
    ```kotlin
    repositories {
       maven {
           url = uri("https://jitpack.io")
       }
    }
    ```

2. Add dependency
    ```kotlin
    dependencies {
        compile("com.github.richarddd:aws-lambda-api-gateway-servlet:master-SNAPSHOT")
    }
    ```

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
