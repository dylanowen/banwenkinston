package com.banwenkinston.utils

import org.json4s.JString
import org.json4s.JsonAST._

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Nov-2016
  */
object JsonUtils {
  class JsonMappingException(message: String) extends Exception(message)

  implicit class JStringToString(val underlying: JValue) extends AnyVal {
    def toText: String = this.underlying match {
      case str: JString => str.values
      case unexpected => throw new JsonMappingException("Expected JString found: " + unexpected)
    }
    def toDecimal: BigDecimal = this.underlying match {
      case num: JLong => num.values
      case num: JDouble => num.values
      case num: JInt => BigDecimal(num.values)
      case num: JDecimal => num.values
      case num: JNumber => throw new JsonMappingException("This isn't implemented: " + num)
      case unexpected => throw new JsonMappingException("Expected JNumber found: " + unexpected)
    }
    def toInt: BigInt = this.underlying match {
      case num: JLong => num.values
      case num: JDouble => BigDecimal(num.values).toBigInt()
      case num: JInt => num.values
      case num: JDecimal => num.values.toBigInt()
      case num: JNumber => throw new JsonMappingException("This isn't implemented: " + num)
      case unexpected => throw new JsonMappingException("Expected JLong or JInt instead found: " + unexpected)
    }
    def toList: List[JValue] = this.underlying match {
      case list: JArray => list.arr
      case singular => List(singular)
    }
  }
}
