package models

package object note {
  object NodeType extends Enumeration {
    val RAW_TEXT_NODE = Value(0, "text")
    val MENU_NODE = Value(1, "menu")
    val PAGE_NODE = Value(2, "page")
    val PAGE_PART_NODE = Value(3, "page-container")
    val RECORD_NODE = Value(4, "record")
    val RECORD_ELEMENT_NODE = Value(5, "record-element")

    def byId(id: Int): NodeType = values.find(_.id == id).getOrElse(RAW_TEXT_NODE)
  }

  object NodePriority extends Enumeration {
    val NO_PRIORITY = Value(0, "no-priority")
    val LOWEST = Value(10, "lowest")
    val LOW = Value(50, "low")
    val NORMAL = Value(100, "normal")
    val HIGH = Value(500, "high")
    val HIGHEST = Value(1000, "highest")
    val BORODA = Value(10000, "boroda")

    def byId(id: Int): NodePriority = values.find(_.id == id).getOrElse(NO_PRIORITY)
  }

  type NodeType = NodeType.Value
  type NodePriority = NodePriority.Value
}
