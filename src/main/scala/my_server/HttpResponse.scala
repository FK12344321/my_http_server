package my_server

import java.text.SimpleDateFormat
import java.util.Calendar

class HttpResponse(val code: String)

case class ErrorHttpResponse(errorCode: String) extends HttpResponse(errorCode)

case class SuccessHttpResponse(fileNumbersSum: BigDecimal) extends HttpResponse("200")
