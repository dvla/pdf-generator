package uk.gov.dvla.vdl.report.pdf

import java.io.{ByteArrayOutputStream, InputStream}
import java.util

import net.sf.jasperreports.engine._
import net.sf.jasperreports.engine.xml.JRXmlLoader
import net.sf.jasperreports.export.SimplePdfExporterConfiguration
import net.sf.jasperreports.export.`type`.PdfaConformanceEnum
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

  /*
    Generates regular PDF in form of byte array for given report descriptor
   */
  def generate(descriptor: Report): Array[Byte] = {
    generate(descriptor, null)
  }

  /**
    Generates PDF in form of byte array for given report descriptor.<br /><br />
    Method supports PDF/A standard. To generate PDF/A document conformance level is required.
    To generate PDF compatible with PDF/A standard report template needs to meet minimal requirements like:
    <ul>
      <li>must use only embeddable fonts</li>
      <li>must not use any transparent images</li>
    </ul>
   */
  def generate(descriptor: Report, conformanceLevel: PdfConformanceLevel): Array[Byte] = {
    require(descriptor.template != null, "Template parameter is required")
    require(descriptor.dataSource != null, "Data source parameter is required")

    val compiledReport: JasperReport = compiledReports.getOrElse(descriptor.template, throw new NoCompiledTemplateException(descriptor.template))
    val print: JasperPrint = JasperFillManager.fillReport(compiledReport, descriptor.parameters, descriptor.dataSource)

    new Exporter(conformanceLevel).export(print)
  }

}

class PdfConformanceLevel(conformanceLevel: PdfaConformanceEnum) {

  def configuration = {
    val configuration = new SimplePdfExporterConfiguration
    configuration.setIccProfilePath("icc/AdobeRGB1998.icc")
    configuration.setPdfaConformance(conformanceLevel)
    configuration.setTagged(true)
    configuration
  }
}

object A1AConformance extends PdfConformanceLevel(PdfaConformanceEnum.PDFA_1A)

object A1BConformance extends PdfConformanceLevel(PdfaConformanceEnum.PDFA_1B)

private[pdf] class Exporter(conformanceLevel: PdfConformanceLevel) {

  import net.sf.jasperreports.engine.export.JRPdfExporter
  import net.sf.jasperreports.export._

  def export(print: JasperPrint): Array[Byte] = {
    val input = new JasperPrintInput(print)
    val output = new ByteArrayOutput

    val exporter = new JRPdfExporter

    if (conformanceLevel != null) {
      exporter.setConfiguration(conformanceLevel.configuration)
    }
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

