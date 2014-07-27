package org.duffqiu.logging.test.dsl

import java.io.File

import scala.language.implicitConversions
import scala.language.postfixOps

import org.duffqiu.logging.common.ANYLINE
import org.duffqiu.logging.common.FIRSTLINE
import org.duffqiu.logging.common.LASTLINE
import org.duffqiu.logging.common.LinePosition
import org.duffqiu.logging.common.LinePosition.WHOLELINE
import org.duffqiu.logging.common.LineType
import org.scalatest.Assertions

import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.DefaultCSVFormat

object LoggingTestDsl extends Assertions {
    type Value = String
    type Position = LinePosition
    type Result = Unit
    type LoggingWithLineType = (LoggingReader, LineType)
    type LoggingWithLineTypeValue = (LoggingReader, LineType, Value)
    type Value2Result = (Value => Result)
    type LoggingWithLineTypeFulFill = (LoggingReader, LineType, Value2Result)
    type LoggingWithLineTypeFulFillAtPosition = (LoggingReader, LineType, Value2Result, Position)

    class LoggingReader(val name: String, val path: String = "./", val delimiter: Char = ',') { out =>

        implicit object MyFormat extends DefaultCSVFormat {
            override val delimiter = out.delimiter
        }

        def in(newPath: String) = new LoggingReader(name, newPath)
        def with_delimiter(newDelimiter: Char) = new LoggingReader(name, path, newDelimiter)

        def open: CSVReader = CSVReader.open(new File(path + name))

    }

    class ReaderHelper(str2Reader: LoggingReader) {
        def in(lt: LineType) = (str2Reader, lt)

    }

    class LineTypeHelper(lwlty: LoggingWithLineType, fulFillAtPostionList: List[LoggingWithLineTypeFulFillAtPosition] = Nil) {

        def have(value: Value) = new CsvChecker((lwlty._1, lwlty._2, (checkValue: Value) =>
            {
                if (checkValue.trim() != value.trim()) {
                    fail("CSV value not matched, expect: " + checkValue + ", but get " + value)
                }
            }), fulFillAtPostionList)

        def and(value: Value) = have(value)

        def fulfill(fun: Value2Result) = new CsvChecker((lwlty._1, lwlty._2, fun), fulFillAtPostionList)
        def and_fulfill(fun: Value2Result) = fulfill(fun)

        def shouldOk(): Unit = {

            val reader = lwlty._1.open
            val stream = reader.toStream

            fulFillAtPostionList.foreach {
                case (reader, lineType, value2Result, pos) => {

                    def fetchValueFromLine(line: List[String]): String = {
                        pos match {
                            case LinePosition(WHOLELINE) => line.mkString(reader.delimiter.toString).trim()
                            case _ => line(pos.pos).trim()
                        }

                    }

                    lineType match {
                        case FIRSTLINE => value2Result(fetchValueFromLine(stream.head))
                        case LASTLINE => value2Result(fetchValueFromLine(stream.last))
                        case ANYLINE => stream.foreach(line => value2Result(fetchValueFromLine(line)))
                        case _ => fail("error line type")
                    }
                }

                case _ => fail("error data in check list")
            }

            reader.close
        }
    }

    class CsvChecker(lwltyf: LoggingWithLineTypeFulFill, fulFillAtPostionList: List[LoggingWithLineTypeFulFillAtPosition]) {

        def at(pos: Position) = {
            val newValueAtPosList = (lwltyf._1, lwltyf._2, lwltyf._3, pos) :: fulFillAtPostionList
            new LineTypeHelper((lwltyf._1, lwltyf._2), newValueAtPosList)
        }
    }

    implicit def string2LoggingHelper(name: String) = new LoggingReader(name)

    implicit def withLineType(str2Reader: LoggingReader) = new ReaderHelper(str2Reader)

    implicit def withCsvValue(lwlty: LoggingWithLineType) = new LineTypeHelper(lwlty)

}
