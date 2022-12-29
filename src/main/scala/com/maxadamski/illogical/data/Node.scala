package com.maxadamski.illogical

import scala.collection.immutable.Map

abstract class Node {
    /**
      * This should call {@link recursiveTyping} to typecheck the current node.
      *
      * @param context Semantic context of evaluation
      * @return typing, or None in case of type error
      */
    protected def typeCheck(context: Map[String, NodeType]): Option[NodeType]

    /**
      * Sets {@link Node.lastTyping} after executing the type check.
      *
      * @param context Semantic context of evaluation
      * @return typing, or None in case of type error
      */
    final def recursiveTyping(context: Map[String, NodeType]): Option[NodeType] = {
        lastTyping = typeCheck(context)
        return lastTyping
    }
    private var lastTyping: Option[NodeType] = None

    final def lastTypingValue(): Option[NodeType] = lastTyping
}
