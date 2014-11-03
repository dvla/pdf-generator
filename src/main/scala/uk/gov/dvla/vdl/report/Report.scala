package uk.gov.dvla.vdl.report

import java.io.ByteArrayInputStream

import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JsonDataSource

import scala.collection.mutable

class Report(val template: String, val dataSource: JRDataSource) {

  private val _parameters: mutable.Map[String, Object] = mutable.Map[String, Object]()

  def parameters: mutable.Map[String, Object] = _parameters

  def withParameter(parameter: (String, Object)): Report = {
    _parameters.put(parameter._1, parameter._2)

    this
  }

  def withParameters(parameters: Map[String, Object]): Report = {
    parameters.foreach(parameter => _parameters.put(parameter._1, parameter._2))

    this
  }
}

class JsonReport(template: String, data: String) extends Report(template, if (data != null) new JsonDataSource(new ByteArrayInputStream(data.getBytes)) else null)
