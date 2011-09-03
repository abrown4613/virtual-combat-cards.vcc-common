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

import org.exnebula.protobuf.vcc.ObjectNotation
import com.google.protobuf.GeneratedMessage.GeneratedExtension

abstract class Datum {
  val name: String

  protected def serializePayload(db: ObjectNotation.Datum.Builder)

  def serialize: ObjectNotation.Datum = {
    val db = ObjectNotation.Datum.newBuilder()
    db.setName(name)
    serializePayload(db)
    db.build()
  }

  /**
   * Attempt to cast the Datum  to a give type.
   * @param lifter Class to lift the data type
   */
  def as[T](implicit lifter: Lifter[T]): T = lifter.getOrFail(this)
}

object Datum {

  class Extractor[T](extension: GeneratedExtension[ObjectNotation.Datum, T], builder: (String, T) => Datum) {
    def unapply(datum: ObjectNotation.Datum): Option[Datum] = {
      if (datum.hasExtension(extension)) Some(builder(datum.getName, datum.getExtension(extension)))
      else None
    }
  }

  class ListExtractor[T](extension: GeneratedExtension[ObjectNotation.Datum, java.util.List[T]], builder: (String, List[T]) => Datum) {
    def unapply(datum: ObjectNotation.Datum): Option[Datum] = {
      if (datum.getExtensionCount(extension) > 0) {
        val l:Seq[T] = for (i <- 0 until datum.getExtensionCount(extension)) yield {
          datum.getExtension(extension, i)
        }
        Some(builder(datum.getName, l.toList))
      }
      else None
    }
  }

  object AsIntDatum extends Extractor[ObjectNotation.IntDatum](ObjectNotation.intDatum, (name, datum) => IntDatum(name, datum.getValue))

  object AsStringDatum extends Extractor[ObjectNotation.StringDatum](ObjectNotation.stringDatum, (name, datum) => StringDatum(name, datum.getValue))

  object AsObjectDatum extends Extractor[ObjectNotation.ObjectDatum](ObjectNotation.objectDatum, (name, datum) => ObjectDatum(name, ObjectNode.deserialize(datum.getValue)))

  object AsObjectData extends ListExtractor[ObjectNotation.ObjectDatum](ObjectNotation.objectData, (name, data) => {
    ObjectData(name, data.map(node => ObjectNode.deserialize(node.getValue)))
  })

  def deserialize(datum: ObjectNotation.Datum): Datum = {
    datum match {
      case AsIntDatum(field) => field
      case AsStringDatum(field) => field
      case AsObjectDatum(field) => field
      case AsObjectData(field) => field
      case _ => null
    }
  }
}

case class IntDatum(name: String, value: Int) extends Datum {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    db.setExtension(ObjectNotation.intDatum, ObjectNotation.IntDatum.newBuilder().setValue(value).build())
  }
}

case class StringDatum(name: String, value: String) extends Datum {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    db.setExtension(ObjectNotation.stringDatum, ObjectNotation.StringDatum.newBuilder().setValue(value).build())
  }
}

case class ObjectDatum(name: String, value: ObjectNode) extends Datum {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    db.setExtension(ObjectNotation.objectDatum, ObjectNotation.ObjectDatum.newBuilder().setValue(value.serializer).build())
  }
}

case class ObjectData(name: String, value: List[ObjectNode]) extends Datum {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    for (node <- value) {
      db.addExtension(ObjectNotation.objectData, ObjectNotation.ObjectDatum.newBuilder().setValue(node.serializer).build())
    }
  }
}

object ObjectData {
  def apply(name: String, ns: ObjectNode*): ObjectData = new ObjectData(name, ns.toList)
}

case class ObjectNode(buildName: String, version: Int, fields: List[Datum]) {
  def serializer: ObjectNotation.ObjectNode.Builder = {
    val ob = ObjectNotation.ObjectNode.newBuilder()
    ob.setBuildClass(buildName)
    ob.setVersion(version)
    for (field <- fields) {
      ob.addData(field.serialize)
    }
    ob
  }

  /**
   *
   */
  def fieldOption(name: String): Option[Datum] = fields.find(_.name == name)

  /**
   * Extract a give field form the the list
   */
  def field(name: String) = fieldOption(name).get

  /**
   * Find field and lift to a given type
   */
  def fieldAs[T](name: String)(implicit lifter: Lifter[T]) = field(name).as(lifter)

  /**
   * Find field and lift to a given type
   */
  def fieldOptionAs[T](name: String)(implicit lifter: Lifter[T]) = fieldOption(name).map(_.as(lifter))
}

object ObjectNode {
  def deserialize(oNode: ObjectNotation.ObjectNode): ObjectNode = {
    val builder = oNode.getBuildClass
    val version = oNode.getVersion
    val fields: Seq[Datum] = for (x <- (0 until oNode.getDataCount)) yield {
      Datum.deserialize(oNode.getData(x))
    }
    ObjectNode(builder, version, fields.toList)
  }
}

