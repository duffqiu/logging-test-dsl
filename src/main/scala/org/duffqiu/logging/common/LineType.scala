package org.duffqiu.logging.common

abstract class LineType(lineType: String)

case object FIRSTLINE extends LineType("FirstLine")
case object LASTLINE extends LineType("LastLine")
case object ANYLINE extends LineType("AnyLine")
