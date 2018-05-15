package storage.lang

import storage._

trait PathMapper extends PathExtractor {
  def resolve(path: PathStr)(implicit ctx: EvaluatorContext): Path = {
    val elements = pathElements(path)
    if (elements.head contains "$") Path(nodeId = ctx.mappings(elements.head), elements = elements.tail)
    else Path(nodeId = ctx.mappings("$"), elements = elements)
  }
}
