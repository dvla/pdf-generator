package uk.gov.dvla.vdl.report

package object exception {
  
  class NoCompiledTemplateException(template: String) extends RuntimeException(s"Template $template needs to be compiled first")
  
}
