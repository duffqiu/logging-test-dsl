package org.duffqiu.logging.common

import scala.language.implicitConversions

case class LinePosition(pos: Int)

object LinePosition {

    val WHOLELINE = -1

    implicit def Int2LinePosition(pos: Int) = LinePosition(pos)

}
