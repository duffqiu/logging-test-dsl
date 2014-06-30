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

    class LineTypeHelper(lwlty: LoggingWithLineType) {
        def have(value: Value) = (lwlty._1, lwlty._2, value)
        def and(value: Value) = (lwlty._1, lwlty._2, value)
        def fulfill(fun: Value2Result) = (lwlty._1, lwlty._2, fun)
        def and_fulfill(fun: Value2Result) = (lwlty._1, lwlty._2, fun)
    }

    class CsvValueHelper(lwltyv: LoggingWithLineTypeValue) {

        def at(pos: Position) = {
            verifyCsvLoggingWithVerificationFun(lwltyv._1, lwltyv._2, { value =>
                {
                    if (lwltyv._3.trim() != value.trim()) {
                        fail("CSV value not matched, expect: " + lwltyv._3 + ", but get " + value)
                    }
                }
            }, pos)
        }
    }

    class CsvFulfillHelper(lwltyf: LoggingWithLineTypeFulFill) {
        def at(pos: Position) = verifyCsvLoggingWithVerificationFun(lwltyf._1, lwltyf._2, lwltyf._3, pos)
    }

    implicit def string2LoggingHelper(name: String) = new LoggingReader(name)

    implicit def withLineType(str2Reader: LoggingReader) = new ReaderHelper(str2Reader)

    implicit def withCsvValue(lwlty: LoggingWithLineType) = new LineTypeHelper(lwlty)

    implicit def withPos(lwltv: LoggingWithLineTypeValue) = new CsvValueHelper(lwltv)

    implicit def withPos(lwltf: LoggingWithLineTypeFulFill) = new CsvFulfillHelper(lwltf)

    private[this] def verifyCsvLoggingWithVerificationFun(loggingReader: LoggingReader, lt: LineType, vf: Value2Result, pos: Position) = {

        val reader = loggingReader.open
        val stream = reader.toStream

        def fetchValueFromLine(line: List[String]): String = {
            pos match {
                case LinePosition(WHOLELINE) => line.mkString(reader.delimiter.toString).trim()
                case _ => line(pos.pos).trim()
            }
        }

        lt match {
            case FIRSTLINE => vf(fetchValueFromLine(stream.head))
            case LASTLINE => vf(fetchValueFromLine(stream.last))
            case ANYLINE => stream.foreach(line => vf(fetchValueFromLine(line)))
            case _ => fail("error line type")
        }

        reader.close

        new LineTypeHelper(loggingReader, lt)
    }

}
