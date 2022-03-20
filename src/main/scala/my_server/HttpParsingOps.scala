package my_server

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.util.matching._

object HttpParsingOps {
  private def getMethod(rawRequest: String): Option[String] = {
    val methodPattern = new Regex("[A-Z]{2,}")
    methodPattern findFirstIn rawRequest
  }

  private def getFileName(rawRequest: String): Option[String] = {
    val fileNamePattern = new Regex("""\/(.*)[\s]+HTTP\/""")

    fileNamePattern.findFirstIn(rawRequest) match {
      case Some(fileNamePattern(fileName)) => Some(fileName)
      case _ => None
    }
  }

  private def getConnectionType(rawRequest: String): Option[String] = {
    val connectionTypePattern = new Regex("""Connection:[\s]*([^\s]*)""")
    connectionTypePattern.findFirstIn(rawRequest) match {
      case Some(connectionTypePattern(connectionType)) => Some(connectionType)
      case _ => None
    }
  }

  def parseToHttpRequest(rawRequest: String): Option[HttpRequest] = {
    val method: Option[String] = getMethod(rawRequest)
    val fileName: Option[String] = getFileName(rawRequest)
    val connectionType: Option[String] = getConnectionType(rawRequest)
    if (method.isDefined && fileName.isDefined && connectionType.isDefined)
      Some(HttpRequest(method.get, fileName.get, connectionType.get))
    else None
  }

  def getCurrentTimeString: String = {
    val dT = Calendar.getInstance().getTime
    val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss")
    dateFormat.format(dT) + " GMT"
  }

  def getContentLength(string: String) = {
    string.getBytes.length
  }

  def parseHttpResponseToString(response: HttpResponse): String = {
    response match {
      case SuccessHttpResponse(result) =>
        s"HTTP/1.1 200 OK\r\nContent-Length: ${getContentLength(result.toString)}\r\nContent-Type: text/plain\r\nLast-Modified: Fry, 18 Aug 2022 05:35:50 GMT\r\nAccept-Ranges: bytes\r\nDate: $getCurrentTimeString\r\n\r\n$result"
      case ErrorHttpResponse(errorCode) =>
        s"HTTP/1.1 $errorCode\r\nContent-Type: text/plain\r\nDate: $getCurrentTimeString\r\n"

    }
  }

}
