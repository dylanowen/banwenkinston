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
