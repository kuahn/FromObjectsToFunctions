package FromObjectsToFunctions

import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write chapter", "insert code", "draw diagrams")
    val toDoList = ToDoList(ListName("book"), items.map(::ToDoItem))
    val lists = mapOf(User("uberto") to listOf(toDoList))
    val app:HttpHandler = Zettai(lists)
    app.asServer(Jetty(8080)).start()
    println("Server started at http://localhost:8080/todo/uberto/book")
}

data class Zettai(val lists:Map<User, List<ToDoList>>): HttpHandler {
    val routes = routes(
        "/todo/{user}/{list}" bind Method.GET to ::showlist
    )
    override fun invoke(req: Request): Response = routes(req)

    val processFun =
        ::extractListData andThen
        ::fetchListContent andThen
        ::renderHtml andThen
        ::createResponse

    private fun showlist(request: Request): Response = processFun(request)

    fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    fun fetchListContent(listId:Pair<User, ListName>): ToDoList =
        lists[listId.first]?.firstOrNull { it.listName == listId.second } ?: error("List unknown")

    fun renderHtml(list:ToDoList): HtmlPage =
        HtmlPage("""
            <html>
                <body>
                    <h1>Zettai</h1>
                    <h2>${list.listName.name}</h2>
                    <table>
                        <tbody>
                            ${renderItems(list.items)}
                        </tbody>
                    </table>
                </body>
            </html>
        """.trimIndent())

    fun renderItems(items: List<ToDoItem>) =
        items.map {
            """<tr><td>${it.description}</td></tr>""".trimIndent()
        }.joinToString("")

    fun createResponse(html:HtmlPage): Response =
        Response(Status.OK).body(html.raw)
}

data class ToDoList(val listName:ListName, val items: List<ToDoItem>)
data class ListName(val name:String)
data class User(val name:String)
data class ToDoItem(val description: String)
enum class ToDoStatus { Todo, Inprogress, Done, Blocked }
data class HtmlPage(val raw:String)

typealias FUN<A, B> = (A) -> B

// 2.1
infix fun <A, B, C> FUN<A, B>.andThen(other: FUN<B, C>): FUN<A, C> = { a -> other(this(a)) }

// 2.2
class FunStack<TYPE> {
    private val stack = mutableListOf<TYPE>()

    fun push(item: TYPE) : FunStack<TYPE> {
        return FunStack<TYPE>().apply {
            stack.add(item)
        }
    }

    fun pop(): Pair<FunStack<TYPE>, TYPE> {
        return Pair(
            FunStack<TYPE>().apply {
                stack.removeAt(stack.size - 1)
            },
            stack.last()
        )
    }

    fun size(): Int = stack.size
}