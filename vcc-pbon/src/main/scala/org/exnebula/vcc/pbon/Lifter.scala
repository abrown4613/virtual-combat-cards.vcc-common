/*
 * Copyright (C) 2008-2011 - Thomas Santana <tms@exnebula.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.exnebula.vcc.pbon

import java.lang.ClassCastException

/**
 * Provides a method for defining type aware data extractors.
 * @param matcher A partial function that should take a Datum and return the expected type.
 */
class Lifter[T](matcher: PartialFunction[Datum, T]) {
  /**
   * Return a value of the correct type (if Datum matches)
   * or throw an ClassCastException if type is not valid.
   */
  def getOrFail(datum: Datum): T = {
    if (matcher.isDefinedAt(datum)) matcher(datum)
    else throw new ClassCastException("Field does not match lifter type")
  }

  /**
   * Check if Datum is of the appropriate type.
   */
  def canLift(datum: Datum): Boolean = matcher.isDefinedAt(datum)

}

/**
 * Base data lifter collection. User to indicate what type of matching is need for each type
 */
object D {
  implicit val intLifter = new Lifter[Int]({
    case IntDatum(_, v) => v
  })
  implicit val stringLifter = new Lifter[String]({
    case StringDatum(_, v) => v
  })

  implicit val objectNodeListLifter = new Lifter[List[ObjectNode]]({
    case ObjectData(_, v) => v
  })

  implicit val objectNodeLifter = new Lifter[ObjectNode]({
    case ObjectDatum(_, v) => v
  })
}
