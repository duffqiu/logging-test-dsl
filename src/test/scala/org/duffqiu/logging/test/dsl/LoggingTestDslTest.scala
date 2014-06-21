package org.duffqiu.logging.test.dsl

import scala.language.postfixOps

import org.duffqiu.logging.common.ANYLINE
import org.duffqiu.logging.common.FIRSTLINE
import org.duffqiu.logging.common.LASTLINE
import org.duffqiu.logging.test.dsl.LoggingTestDsl.string2LoggingHelper
import org.duffqiu.logging.test.dsl.LoggingTestDsl.withCsvValue
import org.duffqiu.logging.test.dsl.LoggingTestDsl.withLineType
import org.duffqiu.logging.test.dsl.LoggingTestDsl.withPos
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers

class LoggingTestDslTest extends FunSpec with Matchers with BeforeAndAfter with GivenWhenThen {
    describe("Logging Test DSL Function Test") {

        it("should read file successfully") {
            val reader = "test.csv" in "./" with_delimiter ';' reader

            val line = reader.readNext

            line match {
                case Some(data) => data(1) shouldBe "column2"
                case None => fail("can't read the data from file")
            }
        }

        it("should be able to use logging test dsl") {
            "test.csv" in "./" with_delimiter ';' in FIRSTLINE have "column3" at 2 and
                "column2" at 1 and "column1" at 0

            "test.csv" in "./" with_delimiter ';' in LASTLINE have "column3" at 2 and
                "column2" at 1 and "column1" at 0

            "test.csv" in "./" with_delimiter ';' in ANYLINE have "column3" at 2 and
                "column2" at 1 and "column1" at 0
        }

        it("should be able to use custom match function in logging test dsl") {
            "test.csv" in "./" with_delimiter ';' in FIRSTLINE fulfill { value => value should startWith("column") } at 2 and
                "column2" at 1 and_fulfill { value => value should startWith("column") } at 0

        }
    }
}
