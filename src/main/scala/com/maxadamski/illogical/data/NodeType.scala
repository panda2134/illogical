package com.maxadamski.illogical


sealed abstract class NodeType {
    def compatibleWith(ty: NodeType): Boolean
}

case object AnyType extends NodeType {
    override def compatibleWith(ty: NodeType): Boolean = true

    override def toString: String = "Any"
}

case object UnknownType extends NodeType {
    override def compatibleWith(ty: NodeType): Boolean = false
    override def toString: String = "Unknown"
}

case class ConcreteType(typeName: String) extends NodeType {
    override def compatibleWith(ty: NodeType): Boolean = ty match {
        case AnyType => true
        case ConcreteType(otherTypeName) => otherTypeName == typeName
        case x => x.compatibleWith(this)
    }
    override def toString: String = typeName
}