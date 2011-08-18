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
package org.exnebula

import vcc.pbon._

import org.specs2.mutable.SpecificationWithJUnit

class MarshallObjectTest extends SpecificationWithJUnit {

  val marshaller = new Marshaller()

  marshaller.addMarshaller(classOf[Alpha])(a => {
    ObjectNode("Alpha", 1, List(StringDatum("name", a.name), IntDatum("id", a.id)))
  })

  marshaller.addUnMarshaller[Alpha]("Alpha", 1) {
    on =>
      Alpha(
        on.field("name").as(D.String),
        on.fieldOptionAs("id", D.Int).getOrElse(0)
      )
  }

  case class Alpha(name: String, id: Int)

  "When marshalling" should {
    "marshall an object" in {
      val a = Alpha("test", 10)
      a must_== a
    }
    "unmarshal something that was martialled" in {
      val a = Alpha("test", 10)
      val mo = marshaller.marshal(a)
      val ua = marshaller.unmarshal[Alpha](mo)
      a must_== ua
    }

    "unmarshal with default if we have another object" in {
      val a = Alpha("test", 0)
      val mo = ObjectNode("Alpha", 1, List(StringDatum("name", a.name)))
      val ua = marshaller.unmarshal[Alpha](mo)
      a must_== ua
    }
  }
}