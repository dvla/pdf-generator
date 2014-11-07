package uk.gov.dvla.vdl.report

import java.io.ByteArrayInputStream

import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.{JRBeanCollectionDataSource, JsonDataSource}
import uk.gov.dvla.vdl.report.types.ReportParameter

import scala.collection.JavaConversions._
import scala.collection.mutable

package object types {
  type ReportParameter = (String, Object)
}

object DataSource {
  def fromJsonString(data: String) = {
    require(data != null, "JSON string is required")
    new JsonDataSource(new ByteArrayInputStream(data.getBytes))
  }

  def fromBean(data: AnyRef) = {
    require(data != null, "Java Bean™ object is required")
    new JRBeanCollectionDataSource(List(data), false)
  }
}

private[report] class Report(val template: String, val dataSource: JRDataSource, reportParameters: ReportParameter*) {

  val parameters = mutable.Map[String, Object](reportParameters:_*)

}

class JsonReport(template: String, data: String, parameters: ReportParameter*)
  extends Report(template, DataSource.fromJsonString(data), parameters: _*)

class BeanReport(template: String, data: AnyRef, parameters: ReportParameter*)
  extends Report(template, DataSource.fromBean(data), parameters: _*)