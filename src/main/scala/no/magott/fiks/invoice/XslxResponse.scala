package no.magott.fiks.invoice

import java.io.OutputStream

import com.sun.xml.internal.ws.message.StringHeader
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import unfiltered.response.{Ok, ContentType, ComposeResponse}

/**
 *
 */
case class XslxResponse(xslx: XSSFWorkbook) extends ComposeResponse(ContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ~> XslxResponseStremaer(xslx))
case class XslxResponseStremaer(xslx: XSSFWorkbook) extends unfiltered.response.ResponseStreamer {
  override def stream(os: OutputStream): Unit = {
    xslx.write(os)
  }
}
