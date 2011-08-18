
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

class Marshaller {

  private var _marshal = Map.empty[Class[AnyRef], AnyRef => ObjectNode]
  private var _unmarshal = Map.empty[(String, Int), ObjectNode => AnyRef]

  def addMarshaller[T <: AnyRef](c: Class[T])(marshal: T => ObjectNode) {
    this._marshal = this._marshal.updated(c.asInstanceOf[Class[AnyRef]], marshal.asInstanceOf[AnyRef => ObjectNode])
  }

  def addUnMarshaller[T](buildName: String, version: Int)(unMarshall: ObjectNode => T) {
    _unmarshal = _unmarshal.updated((buildName, version), (unMarshall.asInstanceOf[ObjectNode => AnyRef]))
  }

  def marshal(obj: AnyRef): ObjectNode = {
    _marshal(obj.getClass.asInstanceOf[Class[AnyRef]])(obj)
  }

  def unmarshal[T <: AnyRef](on: ObjectNode): T = {
    val key = (on.buildName, on.version)
    if (_unmarshal.isDefinedAt(key))
      _unmarshal(key)(on).asInstanceOf[T]
    else
      throw new Exception("Can't unmarshall " + key)
  }
}
