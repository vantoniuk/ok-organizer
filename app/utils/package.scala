package object utils {
  def tuple[T1, T2](a1: Option[T1], a2: Option[T2]): Option[(T1, T2)] = {
    (a1, a2) match {
      case (Some(av1), Some(av2)) => Some((av1, av2))
      case _ => None
    }
  }

  implicit class OptionOps[T](in: Option[T]) {
    def and[T2](in2: Option[T2]): Option[(T, T2)] = tuple(in, in2)
  }
}
