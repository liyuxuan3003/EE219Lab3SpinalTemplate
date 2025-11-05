package EE219Lab3

import spinal.core._
import spinal.core.sim._
import spinal.lib._

object Tools {
  def log2UpSafe(x: Int): Int = Math.max(1, log2Up(x))
}
