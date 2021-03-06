package storage

trait Printable {
  def prettify: String

  implicit class ConsoleString(underlying: String) {
    def red: String = Console.RED + underlying + Console.RESET
    def yellow: String = Console.YELLOW + underlying + Console.RESET
    def blue: String = Console.BLUE + underlying + Console.RESET
  }
}
