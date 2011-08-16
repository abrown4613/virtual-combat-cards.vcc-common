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

import org.specs2.SpecificationWithJUnit

class ObjectNodeTest extends SpecificationWithJUnit {
  val on1 =ObjectNode("class", 1, List(IntDatum("count", 1)))
  val on2 =ObjectNode("class2", 1, List(StringDatum("hello", "world")))
  def is =
    "ObjectNode" ^
      "handle IntDatum" ^ handle(IntDatum("int", 123)) ^
      "handle StringDatum" ^ handle(StringDatum("int", "a name is a name")) ^
      "handle ObjectDatum" ^ handle(ObjectDatum("int", on1)) ^
      "handle ObjectData" ^ handle(ObjectData("int", on1, on2)) ^
      end

  def handle(datum: Datum) = {
    "serialize " + datum.getClass.getSimpleName ! {
      (datum.serialize must not beNull)
    } ^ "deserialize " + datum.getClass.getSimpleName ! {
      Datum.deserialize(datum.serialize) must_== datum
    } ^ endp
  }
}