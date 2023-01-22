package io.oath.juror.utils

import com.google.common.base.CaseFormat

private[juror] object FormatConversion {

  def convertUpperCamelToLowerHyphen(str: String): String = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, str)
}
