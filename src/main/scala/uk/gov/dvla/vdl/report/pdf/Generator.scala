package uk.gov.dvla.vdl.report.pdf

import java.io.{ByteArrayOutputStream, InputStream}
import java.util

import net.sf.jasperreports.engine._
import net.sf.jasperreports.engine.xml.JRXmlLoader
import uk.gov.dvla.vdl.report.Report
import uk.gov.dvla.vdl.report.exception.NoCompiledTemplateException

class Generator {

  import scala.collection.JavaConversions._

  private val compiledReports = collection.concurrent.TrieMap[String, JasperReport]()

  def compile(name: String, template: InputStream) = {
    val compiledReport = JasperCompileManager.compileReport(JRXmlLoader.load(template))
    compiledReports.put(name, compiledReport)
    compiledReport
  }

  def generate(descriptor: Report): Array[Byte] = {
    require(descriptor.template != null, "Template parameter is required")
    require(descriptor.dataSource != null, "Data source parameter is required")

    val compiledReport: JasperReport = compiledReports.getOrElse(descriptor.template, throw new NoCompiledTemplateException(descriptor.template))
    val print: JasperPrint = JasperFillManager.fillReport(compiledReport, descriptor.parameters, descriptor.dataSource)

    new Exporter().export(print)
  }

}

private[pdf] class Exporter() {

  import net.sf.jasperreports.engine.export.JRPdfExporter
  import net.sf.jasperreports.export._

  def export(print: JasperPrint): Array[Byte] = {
    val input = new JasperPrintInput(print)
    val output = new ByteArrayOutput

    val exporter = new JRPdfExporter
    exporter.setExporterInput(input)
    exporter.setExporterOutput(output)
    exporter.exportReport()

    output.getOutputStream.toByteArray
  }

  private class JasperPrintInput(print: JasperPrint) extends ExporterInput {

    import scala.collection.JavaConversions._

    override def getItems: util.List[ExporterInputItem] = {
      List(print).map(toExporterInputItem)
    }

    private def toExporterInputItem(print: JasperPrint): ExporterInputItem = new ExporterInputItem {
      override def getJasperPrint: JasperPrint = print

      override def getConfiguration: ReportExportConfiguration = null
    }

  }

  private class ByteArrayOutput extends OutputStreamExporterOutput {

    private val stream = new ByteArrayOutputStream

    override def getOutputStream: ByteArrayOutputStream = stream

    override def close(): Unit = stream.close()

  }

}

