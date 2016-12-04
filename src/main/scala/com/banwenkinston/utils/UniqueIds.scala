package com.banwenkinston.utils

import java.io.InputStream

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

  private val adjectives: Array[String] = getWords("adjectives.txt")
  private val nouns: Array[String] = getWords("nouns.txt")
  private val rand = new Random(System.currentTimeMillis())

  private val adjectivesPerId = 1

  def get: String = {
    var str: String = ""
    for (_ <- 0 until adjectivesPerId) {
      str += adjectives(rand.nextInt(adjectives.length))
    }

    str + nouns(rand.nextInt(nouns.length))
  }

  private def getWords(resourceFile: String): Array[String] = {
    val stream : InputStream = getClass.getResourceAsStream("/" + resourceFile)

    scala.io.Source.fromInputStream(stream).getLines.map(_.capitalize).toArray
  }
}
