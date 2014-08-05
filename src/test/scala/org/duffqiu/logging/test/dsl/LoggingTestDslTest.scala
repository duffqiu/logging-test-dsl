package org.duffqiu.logging.test.dsl

import scala.language.postfixOps
import org.duffqiu.logging.common.ANYLINE
import org.duffqiu.logging.common.FIRSTLINE
import org.duffqiu.logging.common.LASTLINE
import org.duffqiu.logging.test.dsl.LoggingTestDsl.string2LoggingHelper
import org.duffqiu.logging.test.dsl.LoggingTestDsl.withCsvValue
import org.duffqiu.logging.test.dsl.LoggingTestDsl.withLineType
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import org.scalatest.exceptions.TestFailedException
import org.duffqiu.logging.common.LinePosition.WHOLELINE

class LoggingTestDslTest extends FunSpec with Matchers with BeforeAndAfter with GivenWhenThen {
    describe("Logging Test DSL Function Test") {

        it("should be able to use logging test dsl") {
            "test.csv" in "./" with_delimiter ';' in FIRSTLINE have "column3" at 2 and
                "column2" at 1 and "column1" at 0 shouldOk

            "test.csv" in "./" with_delimiter ';' in LASTLINE have "column3" at 2 and
                "column2" at 1 and "column1" at 0 shouldOk

            "test.csv" in "./" with_delimiter ';' in ANYLINE have "column3" at 2 and
                "column2" at 1 and "column1" at 0 shouldOk
        }

        it("should be able to use custom match function in logging test dsl") {
            "test.csv" in "./" with_delimiter ';' in FIRSTLINE fulfill { value => value should startWith("column") } at 2 and
                "column2" at 1 and_fulfill { value => value should startWith("column") } at 0 shouldOk

        }

        it("should catch custom verification fun exception if the value is not matched") {
            intercept[TestFailedException] {
                "test.csv" in "./" with_delimiter ';' in FIRSTLINE fulfill { value => value should startWith("column") } at 2 and
                    "column2" at 1 and_fulfill { _ should startWith("xolumn") } at 0 shouldOk
            }
        }

        it("should catch value not matched") {
            intercept[TestFailedException] {
                "test.csv" in "./" with_delimiter ';' in FIRSTLINE fulfill { value => value should startWith("column") } at 2 and
                    "xolumn2" at 1 and_fulfill { value => value should startWith("column") } at 0 shouldOk
            }
        }

        it("should read the whole line to test") {

            ("test.csv" in "./" with_delimiter ';' in FIRSTLINE
                fulfill { _ should fullyMatch regex ("""^([\s\S][^;]*);([\s\S][^;]*);([\s\S][^;]*)""".r) } at WHOLELINE
                and_fulfill { _ should include("column") } at WHOLELINE and "column2" at 1 shouldOk)

        }

        it("should be quick(<3s) when read the the big file") {

            val startTime = System.currentTimeMillis()

            "test.csv" in "./" with_delimiter ';' in FIRSTLINE fulfill { value => value should startWith("column") } at 2 and
                "column2" at 1 and_fulfill { value => value should startWith("column") } at 0 shouldOk

            val duration = (System.currentTimeMillis() - startTime) / 1000

            assert(duration < 180)

        }
    }
}
