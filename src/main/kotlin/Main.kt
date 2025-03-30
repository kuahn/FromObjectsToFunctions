package FromObjectsToFunctions

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer

val htmlPage: String = """
<html>    
    <head>
        <title>My Page</title>
    </head>
    <body>
        <h1>Hello, world!!!!</h1>
    </body>
</html>
""".trimIndent()

var handle:HttpHandler = { Response(Status.OK).body(htmlPage) }

fun main() {
    handle.asServer(Jetty(8080)).start()
}