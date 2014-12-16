logging-test-dsl
================

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/duffqiu/logging-test-dsl?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/duffqiu/logging-test-dsl.svg?branch=develop)](https://travis-ci.org/duffqiu/logging-test-dsl)

It is a dsl for test logging file(scala)

- It is very simpel to use the DSL to test CSV logging file

	- example: Check the value at specific position
	
	      "test.csv" in "./" with_delimiter ';' in ANYLINE have "value3" at 2 and "value2" at 1 and "column1" at 0

	- example: Check with custom function
	
		  ("test.csv" in "./" with_delimiter ';' in FIRSTLINE fulfill { _ should fullyMatch regex ("""^([\s\S][^;]*);([\s\S][^;]*);([\s\S][^;]*)""".r) } at WHOLELINE and_fulfill { _ should include("column") } at WHOLELINE and "column2" at 1)

*** Release Notes ***

##0.0.1

   * First release

##0.0.2
   
   * update scalatest version to 2.2.0
   * change group id