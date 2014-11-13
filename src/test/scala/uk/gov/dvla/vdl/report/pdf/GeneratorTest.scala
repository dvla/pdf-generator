package uk.gov.dvla.vdl.report.pdf

import java.io.InputStream
import java.util.Date

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.dvla.vdl.report.JsonReport
import uk.gov.dvla.vdl.report.exception.NoCompiledTemplateException

import scala.io.Source.fromFile

class GeneratorTest extends FlatSpec with Matchers {

  val reportName: String = "sample"

  val generator = new Generator

  behavior of "PDF generator"

  it should "throw exception if template is missing" in {
    val report = new JsonReport(null, fromFile(resourcePath("data/sample.json")).mkString)

    intercept[IllegalArgumentException] {
      generator.generate(report)
    }

  }

  it should "throw exception if trying generate report without prior template compilation" in {
    intercept[NoCompiledTemplateException] {
      generator.generate(
        new JsonReport(reportName, fromFile(resourcePath(s"data/$reportName.json")).mkString)
      )
    }
  }

  it should "generate PDF from data source and parameters" in {

    generator.compile(reportName, resourceAsStream(s"reports/$reportName.jrxml"))

    val printout: Array[Byte] = generator.generate(
      new JsonReport(reportName, fromFile(resourcePath(s"data/$reportName.json")).mkString,
        "GENERATED_BY" -> "Kainos Software Ltd",
        "GENERATION_DATE" -> new Date()
      )
    )

    printout.length should not be 0
  }

  it should "generate PDF/A 1A from data source and parameters" in {

    generator.compile(reportName, resourceAsStream(s"reports/$reportName.jrxml"))

    val printout: Array[Byte] = generator.generate(
      new JsonReport(reportName, fromFile(resourcePath(s"data/$reportName.json")).mkString,
        "GENERATED_BY" -> "Kainos Software Ltd",
        "GENERATION_DATE" -> new Date()
      ),
      A1AConformance
    )

    printout.length should not be 0
  }

  it should "generate PDF/A 1B from data source and parameters" in {

    generator.compile(reportName, resourceAsStream(s"reports/$reportName.jrxml"))

    val printout: Array[Byte] = generator.generate(
      new JsonReport(reportName, fromFile(resourcePath(s"data/$reportName.json")).mkString,
        "GENERATED_BY" -> "Kainos Software Ltd",
        "GENERATION_DATE" -> new Date()
      ),
      A1BConformance
    )

    printout.length should not be 0
  }

  private def resourcePath(resource: String): String = {
    getClass.getResource(s"/$resource").getPath
  }

  private def resourceAsStream(resource: String): InputStream = {
    getClass.getResourceAsStream(s"/$resource")
  }

}
