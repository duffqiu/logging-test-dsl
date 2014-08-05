package org.duffqiu.logging.common

import scala.language.implicitConversions

/**
 * @author DuffQiu
 *
 *
 */
case class LinePosition(at: Int)

object LinePosition {

    val WHOLELINE = -1

    implicit def Int2LinePosition(pos: Int) = LinePosition(pos)

}
