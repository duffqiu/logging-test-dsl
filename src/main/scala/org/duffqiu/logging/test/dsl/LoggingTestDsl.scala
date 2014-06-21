package org.duffqiu.logging.test.dsl

import scala.language.implicitConversions
import scala.language.postfixOps
import com.github.tototoshi.csv.DefaultCSVFormat
import com.github.tototoshi.csv.CSVReader
import java.io.File
import org.duffqiu.logging.common.LineType
import org.duffqiu.logging.common.FIRSTLINE
import org.scalatest.Assertions
import java.io.FileNotFoundException
import org.duffqiu.logging.common.LASTLINE
import org.duffqiu.logging.common.ANYLINE

object LoggingTestDsl extends Assertions {
    type Value = String
    type Position = Int
    type Result = Unit
    type LoggingWithLineType = (LoggingReader, LineType)
    type LoggingWithLineTypeValue = (LoggingReader, LineType, Value)
    type Value2Result = (Value => Result)
    type LoggingWithLineTypeFulFill = (LoggingReader, LineType, Value2Result)

    class LoggingReader(val name: String, val path: String = "./", delimiter: Char = ',') { out =>

        implicit object MyFormat extends DefaultCSVFormat {
            override val delimiter = out.delimiter
        }

        def in(newPath: String) = new LoggingReader(name, newPath)
        def with_delimiter(newDelimiter: Char) = new LoggingReader(name, path, newDelimiter)

        def reader: CSVReader = CSVReader.open(new File(path + name))

    }

    class ReaderHelper(str2Reader: LoggingReader) {
        def in(lt: LineType) = (str2Reader, lt)

    }

    class LineTypeHelper(lwlty: LoggingWithLineType) {
        def have(value: Value) = (lwlty._1, lwlty._2, value)
        def and(value: Value) = (lwlty._1, lwlty._2, value)
        def fulfill(fun: Value2Result) = (lwlty._1, lwlty._2, fun)
        def and_fulfill(fun: Value2Result) = (lwlty._1, lwlty._2, fun)
        def end = Unit
    }

    class CsvValueHelper(lwltyv: LoggingWithLineTypeValue) {
        def at(pos: Position) = (lwltyv._1, lwltyv._2, lwltyv._3, pos)
    }

    class CsvFulfillHelper(lwltyf: LoggingWithLineTypeFulFill) {
        def at(pos: Position) = (lwltyf._1, lwltyf._2, lwltyf._3, pos)
    }

    implicit def string2LoggingHelper(name: String) = new LoggingReader(name)

    implicit def withLineType(str2Reader: LoggingReader) = new ReaderHelper(str2Reader)

    implicit def withCsvValue(lwlty: LoggingWithLineType) = new LineTypeHelper(lwlty)

    implicit def withPos(lwltv: LoggingWithLineTypeValue) = new CsvValueHelper(lwltv)

    implicit def withPos(lwltf: LoggingWithLineTypeFulFill) = new CsvFulfillHelper(lwltf)

    implicit def verifyCsvLogging(t: (LoggingReader, LineType, Value, Position)) = {

        t match {
            case (readerHelp, lt, value, pos) => {
                val reader = readerHelp.reader

                val stream = reader.toStream
                lt match {
                    case FIRSTLINE => verfiyCsvLine(stream.head, value, pos)
                    case LASTLINE => verfiyCsvLine(stream.last, value, pos)
                    case ANYLINE => stream.foreach(verfiyCsvLine(_, value, pos))
                    case _ => fail("error line type")
                }

                reader.close

            }
        }

        new LineTypeHelper(t._1, t._2)
    }

    implicit def verifyCsvLoggingWithFulfill(t: (LoggingReader, LineType, Value2Result, Position)) = {

        t match {
            case (readerHelp, lt, f, pos) => {
                val reader = readerHelp.reader

                val stream = reader.toStream
                lt match {
                    case FIRSTLINE => custCsvLine(stream.head, f, pos)
                    case LASTLINE => custCsvLine(stream.last, f, pos)
                    case ANYLINE => stream.foreach(custCsvLine(_, f, pos))
                    case _ => fail("error line type")
                }

                reader.close

            }
        }

        new LineTypeHelper(t._1, t._2)
    }

    def verfiyCsvLine(line: List[String], value: Value, pos: Position): Unit = {
        if (line(pos).trim() != value.trim()) {
            fail("CSV value not matched, expect: " + value + ", but get " + line(pos))
        }
    }

    def custCsvLine(line: List[String], f: Value2Result, pos: Position): Unit = {
        f(line(pos).trim())
    }

}
