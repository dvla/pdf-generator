package uk.gov.dvla.vdl.pdf

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.dvla.vdl.report.JsonReport

import scala.io.Source.fromFile

class GeneratorTest extends FlatSpec with Matchers {

  val generator = new Generator

  behavior of "PDF generator"

  it should "throw exception if template is missing" in {
    val report = new JsonReport(null, fromFile(resource("data/sample.json")).mkString)

    intercept[IllegalArgumentException] {
      generator.generate(report)
    }

  }

  it should "throw exception if data source is missing" in {
    val report = new JsonReport(resource("reports/sample.jrxml"), null)

    intercept[IllegalArgumentException] {
      generator.generate(report)
    }

  }

  it should "generate valid PDF printout from data source and parameters" in {
    val report = new JsonReport(resource("reports/sample.jrxml"), fromFile(resource("data/sample.json")).mkString)
      .withParameter("GENERATED_BY" -> "Kainos Software Ltd")

    val printout: Array[Byte] = generator.generate(report)

    printout.length should not be 0
  }

  private def resource(resource: String): String = {
    getClass.getResource(s"/$resource").getPath
  }

}
