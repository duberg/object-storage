package storage

import java.util.regex.Pattern

import storage.Path.Separator

trait PathExtractor {
  def pathElements(pathStr: PathStr): List[String] = pathStr.split(Pattern.quote(Separator)).toList
}
