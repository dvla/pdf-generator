package uk.gov.dvla.vdl.pdf

import java.io.{ByteArrayOutputStream, File}
import java.util

import net.sf.jasperreports.engine._
import net.sf.jasperreports.engine.xml.JRXmlLoader
import uk.gov.dvla.vdl.report.Report

class Generator {
  import scala.collection.JavaConversions._

  private val compiledReports = collection.concurrent.TrieMap[String, JasperReport]()

  def generate(descriptor: Report): Array[Byte] = {
    require(descriptor.template != null, "Template parameter is required")
    require(descriptor.dataSource != null, "Data source parameter is required")

    val report: JasperReport = compiledReports.get(descriptor.template) match {
      case Some(compiledReport) => compiledReport
      case None =>
        def compileReport(template: String) = {
          JasperCompileManager.compileReport(JRXmlLoader.load(new File(descriptor.template)))
        }
        val compiledReport = compileReport(descriptor.template)
        compiledReports.put(descriptor.template, compiledReport)
        compiledReport
    }
    val print: JasperPrint = JasperFillManager.fillReport(report, descriptor.parameters, descriptor.dataSource)

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

