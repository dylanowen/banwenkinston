package com.bankwenkinston.wallboard

import scala.util.Random

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Dec-2016
  */
object UniqueIds {
  val methodStrings: Array[String] = Array(
    "Assign", "Attribute", "Method",
    "Assign", "Relationship", "Method",
    "Build", "Attribute", "Method",
    "Build", "Element", "Method",
    "Build", "Response", "by", "System", "Routine", "Method",
    "Build", "UI", "Response", "Method",
    "Build", "Web", "Service", "Response", "Method",
    "Calculate", "Date", "Method",
    "Calculate", "Numeric", "Method",
    "Compare", "Instance", "Sets", "Method",
    "Derived", "Process", "Update", "Method",
    "Evaluate", "Boolean", "Expression", "Method",
    "Evaluate", "Conditions", "Method",
    "Enqueue", "Message", "Method",
    "Executable", "with", "YP", "returning", "Attribute",
    "Executable", "with", "YP", "returning", "Instance", "Set",
    "Get", "Attribute", "from", "Parameters", "Method",
    "Get", "Attribute", "by", "System", "Routine", "Method",
    "Get", "Element", "from", "Parameters", "Method",
    "Get", "Element", "by", "System", "Routine", "Method",
    "Get", "Instances", "Method",
    "Get", "Referenced", "Attribute", "Method",
    "Get", "Referenced", "Element", "Method",
    "Get", "Referenced", "Instance", "Set", "Method",
    "Get", "Specified", "Instances", "Method",
    "Get", "Instance", "Set", "from", "Parameters", "Method",
    "Get", "Instance", "Set", "by", "System", "Routine", "Method",
    "Instance", "Op", "Method",
    "Invoke", "Web", "Service", "Method",
    "Process", "Updates", "Method",
    "Conditional", "Select", "Attribute", "Method",
    "Select", "Attribute", "Method",
    "Spawn", "Internal", "Message", "Method",
    "Conditional", "Select", "from", "Instance", "Set", "Method",
    "Select", "from", "Instance", "Set", "Method",
    "Update", "by", "System", "Routine", "Method",
    "Web", "Service", "Background", "Process"
  )

  val rand = new Random(System.currentTimeMillis())
  def get: String = {
    var str: String = ""
    for (_ <- 0 until 3) {
      str += methodStrings(rand.nextInt(methodStrings.length))
    }

    str
  }
}
