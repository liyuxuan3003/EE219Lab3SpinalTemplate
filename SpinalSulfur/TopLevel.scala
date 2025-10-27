package SpinalSulfur

import spinal.core._
import spinal.core.sim._
import spinal.lib._

case class TopLevel() extends Component {
  val io = new Bundle {
    val m = master(magicleda.MagicLeda())
  }

  val top = Test()
  top.io.a := io.m.switch(1)(0 to 3)
  top.io.b := io.m.switch(1)(4 to 7)
  top.io.i := io.m.key(3)
  io.m.led(1)(0 to 3) := top.io.o
  io.m.led(1)(4 to 7) := 0
}

object TopLevelSim extends App {
  Config.sim.compile(TopLevel()).doSim { dut =>
    dut.clockDomain.forkStimulus(period = 10, resetCycles = 10)

  }
}

object TopLevelVerilog extends App {
  Config.spinal.generateVerilog(TopLevel())
}

object TopLevelVhdl extends App {
  Config.spinal.generateVhdl(TopLevel())
}
