package my_server

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.{ServerSocket, Socket}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

object HttpServer extends App {
  import HttpParsingOps._

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  def stringToNumbers(fileContent: String): List[BigDecimal] = {
    import scala.util.matching._

    val numberPattern = new Regex("""[\d]{1,}\.{0,1}[\d]*[\n]*""")
    (numberPattern findAllIn fileContent).toList.map(num => BigDecimal(num))
  }

  def getFileNumbersSum(filename: String): BigDecimal = {
    import scala.io.Source
    val pathToCurrentDirectory = System.getProperty("user.dir")
    val filePath = s"$pathToCurrentDirectory/$filename"
    val source = Source.fromFile(filePath)
    val numbers = stringToNumbers(source.getLines().mkString(" "))
    source.close()
    numbers.sum
  }

  def processHttpRequest(rawRequest: String): HttpResponse = {
    val httpRequest: Option[HttpRequest] = HttpParsingOps.parseToHttpRequest(rawRequest)
    httpRequest match {
      case None => ErrorHttpResponse("400 Bad Request")
      case Some(request) =>
        if (request.method != "GET") ErrorHttpResponse("404 Not Found")
        else SuccessHttpResponse(getFileNumbersSum(request.fileName))
    }
  }

  @tailrec
  def printComputedResponses(out: PrintStream, responses: List[Future[HttpResponse]]): List[Future[HttpResponse]] = {
    if (responses.nonEmpty && responses.head.isCompleted) {
      val currentResponse = responses.head.value
      out.println(parseHttpResponseToString(currentResponse.get.get))
      printComputedResponses(out, responses.tail)
    }
    else responses
  }

  def processHttpRequests(socket: Socket): Unit = {
    var currentLine: String = ""
    var currentHttpRequestString: String = ""
    var currentlyProcessedRequests: List[Future[HttpResponse]] = List()

    while(currentLine ne null) {
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = new PrintStream(socket.getOutputStream)
      currentLine = in.readLine()
      if (currentHttpRequestString.nonEmpty && currentLine.isEmpty) {
        val temp = currentHttpRequestString
        currentlyProcessedRequests = currentlyProcessedRequests.appended(Future {
          processHttpRequest(temp)
        })


        currentlyProcessedRequests = printComputedResponses(out, currentlyProcessedRequests)
        currentHttpRequestString = ""
      }
      else if (currentLine.isEmpty) {
        currentlyProcessedRequests = printComputedResponses(out, currentlyProcessedRequests)
      }
      else currentHttpRequestString += currentLine
    }
  }


  while (true) {
    val serverSocket = new ServerSocket(8000)
    val socket = serverSocket.accept()
    print("Accepted")
    Try {processHttpRequests(socket)}
    try {
      processHttpRequests(socket)
      println("Connection ended successfully")
    }
    catch {
      case ex: Throwable => println("Connection ended unexpectedly ")
    }
  }
}
