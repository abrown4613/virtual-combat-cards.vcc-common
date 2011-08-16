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

trait Field {
  val name: String

  protected def serializePayload(db: ObjectNotation.Datum.Builder)

  def serialize: ObjectNotation.Datum = {
    val db = ObjectNotation.Datum.newBuilder()
    db.setName(name)
    serializePayload(db)
    db.build()
  }
}

object Field {

  class Extractor[T](extension: GeneratedExtension[ObjectNotation.Datum, T], builder: (String, T) => Field) {
    def unapply(datum: ObjectNotation.Datum): Option[Field] = {
      if (datum.hasExtension(extension)) Some(builder(datum.getName, datum.getExtension(extension)))
      else None
    }
  }

  class ListExtractor[T](extension: GeneratedExtension[ObjectNotation.Datum, java.util.List[T]], builder: (String, List[T]) => Field) {
    def unapply(datum: ObjectNotation.Datum): Option[Field] = {
      if (datum.getExtensionCount(extension) > 0) {
        val l = for (i <- 0 until datum.getExtensionCount(extension)) yield {
          datum.getExtension(extension, i)
        }
        Some(builder(datum.getName, l.toList))
      }
      else None
    }
  }

  object ExtractIntField extends Extractor[ObjectNotation.IntDatum](ObjectNotation.intDatum, (name, datum) => IntField(name, datum.getValue))

  object ExtractStringField extends Extractor[ObjectNotation.StringDatum](ObjectNotation.stringDatum, (name, datum) => StringField(name, datum.getValue))

  object ExtractObjectField extends Extractor[ObjectNotation.ObjectDatum](ObjectNotation.objectDatum, (name, datum) => ObjectField(name, NodeObject.deserialize(datum.getValue)))

  object ExtractObjectListField extends ListExtractor[ObjectNotation.ObjectDatum](ObjectNotation.objectData, (name, data) => {
    ObjectListField(name, data.map(node => NodeObject.deserialize(node.getValue)))
  })

  def deserialize(datum: ObjectNotation.Datum): Field = {
    datum match {
      case ExtractIntField(field) => field
      case ExtractStringField(field) => field
      case ExtractObjectField(field) => field
      case ExtractObjectListField(field) => field
      case _ => null
    }
  }
}

case class IntField(name: String, value: Int) extends Field {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    db.setExtension(ObjectNotation.intDatum, ObjectNotation.IntDatum.newBuilder().setValue(value).build())
  }
}

case class StringField(name: String, value: String) extends Field {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    db.setExtension(ObjectNotation.stringDatum, ObjectNotation.StringDatum.newBuilder().setValue(value).build())
  }
}

case class NodeObject(buildName: String, version: Int, fields: List[Field]) {
  def serializer: ObjectNotation.ObjectNode.Builder = {
    val ob = ObjectNotation.ObjectNode.newBuilder()
    ob.setBuildClass(buildName)
    ob.setVersion(version)
    for (field <- fields) {
      ob.addData(field.serialize)
    }
    ob
  }
}

object NodeObject {
  def deserialize(oNode: ObjectNotation.ObjectNode): NodeObject = {
    val builder = oNode.getBuildClass
    val version = oNode.getVersion
    val fields: Seq[Field] = for (x <- (0 until oNode.getDataCount)) yield {
      Field.deserialize(oNode.getData(x))
    }
    NodeObject(builder, version, fields.toList)
  }
}

case class ObjectField(name: String, value: NodeObject) extends Field {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    db.setExtension(ObjectNotation.objectDatum, ObjectNotation.ObjectDatum.newBuilder().setValue(value.serializer).build())
  }
}

case class ObjectListField(name: String, value: List[NodeObject]) extends Field {
  protected def serializePayload(db: ObjectNotation.Datum.Builder) {
    for (node <- value) {
      db.addExtension(ObjectNotation.objectData, ObjectNotation.ObjectDatum.newBuilder().setValue(node.serializer).build())
    }
  }
}

object ObjectListField {
  def apply(name: String, ns: NodeObject*): ObjectListField = new ObjectListField(name, ns.toList)
}