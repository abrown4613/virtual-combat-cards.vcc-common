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

  def name[T](implicit m: scala.reflect.Manifest[T]) = m.toString

  case class grabber(name: String) {
    def get[T](on: ObjectNode)(implicit manif: Manifest[T]): Option[T] = {
      val opt = on.fields.find(_.name == name)
      if (opt.isDefined) {
        println("T is " + manif.toString)
        println("Not match " + (manif.erasure == opt.get.getClass))
        opt.get match {
          case s: T => Some(s.asInstanceOf[T])
          case x => None
        }
      } else {
        None
      }
    }


  }

  def lift[T](on: ObjectNode, matcher: PartialFunction[Datum, T]): Option[T] = {
    val opt = on.fields.find(matcher.isDefinedAt)
    opt.map(matcher.apply)
  }

  def lift2[T](on: ObjectNode, name: String, matcher: PartialFunction[Datum, T]): Option[T] = {
    val opt = on.fields.find(_.name == name)
    opt.collect(matcher)
  }

  val asInt: PartialFunction[Datum, Int] = {
    case IntDatum(_, v) => v
  }
  val asString: PartialFunction[Datum, String] = {
    case StringDatum(_, v) => v
  }

  class Lifter[T](matcher: PartialFunction[Datum, T]) {
    def getOrFail(datum: Datum): T = {
      if (matcher.isDefinedAt(datum)) matcher(datum)
      else throw new Exception("Field does not match lifter type")
    }

    def canLift(datum:Datum):Boolean = matcher.isDefinedAt(datum)

  }

  class DatumWrap(datum: Datum) {
    def as[T](lifter: Lifter[T]): T = lifter.getOrFail(datum)
  }

  object D {
    val Int = new Lifter[Int]({
      case IntDatum(_, v) => v
    })
    val String = new Lifter[String]({
      case StringDatum(_, v) => v
    })

    val ObjectNodeList = new Lifter[List[ObjectNode]]({
      case ObjectData(_, v) => v
    })

    val ObjectNode = new Lifter[ObjectNode]({
      case ObjectDatum(_, v) => v
    })
  }

  class ONWrapper(on: ObjectNode) {
    def fieldOption(name: String): Option[DatumWrap] = on.fields.find(_.name == name).map(new DatumWrap(_))

    def field(name: String) = fieldOption(name).get
  }


  abstract class SomeStuff[T](implicit protected val manifest: Manifest[T]) {
    val a: T

    def as[U](implicit manif: Manifest[U]): U = {
      if (this.manifest == manif) a.asInstanceOf[U]
      else throw new Exception("Cant cast to " + manif.toString)
    }
  }

  case class IntStuff(a: Int) extends SomeStuff[Int]

  case class StringStruff(a: String) extends SomeStuff[String]


  val marshaller = new Marshaller()
  marshaller.addMarshaller(classOf[Alpha])(a => {
    ObjectNode("Alpha", 1, List(StringDatum("name", a.name), IntDatum("id", a.id)))
  })
  marshaller.addUnMarshaller[Alpha]("Alpha", 1) {
    on =>
      val won = new ONWrapper(on)
      println("on: " + on.fields)
      val a = Alpha(
        won.field("name").as(D.String),
        won.field("id").as(D.Int)
      )
      println("Testing " + a)
      Alpha(
        //        on.fields.find(_.name == "name").get.asInstanceOf[StringDatum].value,
        //        on.fields.find(_.name == "id").get.asInstanceOf[IntDatum].value)
        lift2(on, "name", asString).getOrElse("?"),
        lift(on, {
          case IntDatum("name", v) => v
        }).getOrElse(0))

  }

  case class Alpha(name: String, id: Int)

  "When marshalling" should {
    "marshall an object" in {
      val a = Alpha("test", 10)
      a must_== a
    }
    "round trip" in {
      val a = Alpha("test", 10)
      val mo = marshaller.marshal(a)
      val ua = marshaller.unmarshal[Alpha](mo)
      a must_== ua
    }
    "not cast" in {
      val x: SomeStuff[_] = StringStruff("has")

      x.as[String] must_== "has"
      (x.as[Int]) must throwA(new Exception("Cant cast to Int"))
    }
    "not cast 2" in {
      val x: SomeStuff[_] = IntStuff(123)

      x.as[Int] must_== 123
      (x.as[String]) must throwA(new Exception("Cant cast to java.lang.String"))

    }
    "many manifests" in {
      val m = getManifest[List[Int]]
      var s = 0
      val count = 1000000
      (0 until count).foreach {
        x =>
          if(m == getManifest[Int]) s+=1
          if(m == getManifest[String]) s+=1
          if(m == getManifest[List[ObjectNode]]) s+=1
          if(m == getManifest[List[Int]]) s+=1
          x
      }
      //      m.toString must_== "scala.collection.immutable.List[int]"
      s must_== count
    }
    "many class manifests" in {
      val m = getClassManifest[List[Int]]
      var s = 0
      val count = 1000000
      (0 until count).foreach {
        x =>
          if(m == getClassManifest[Int]) s+=1
          if(m == getClassManifest[String]) s+=1
          if(m == getClassManifest[List[ObjectNode]]) s+=1
          if(m == getClassManifest[List[Int]]) s+=1
          x
      }
      //      m.toString must_== "scala.collection.immutable.List[int]"
      s must_== count * 2
    }

    "many matchs" in {
      val m = getManifest[List[Int]]
      var s = 0
      val count = 1000000
      val datum:Datum = StringDatum("a", "b")
      (0 until count).foreach {
        x =>
          if(D.String.canLift(datum)) {
            val str = D.String.getOrFail(datum)
            s+=1
          }
          if(D.Int.canLift(datum)) s+=1
          if(D.ObjectNodeList.canLift(datum)) s+=1
          if(D.ObjectNode.canLift(datum)) s+=1
          x
      }
      s must_== count
    }
  }

  def getManifest[T](implicit m: Manifest[T]) = {
    m
  }
  def getClassManifest[T](implicit m: ClassManifest[T]) = {
    m
  }

}