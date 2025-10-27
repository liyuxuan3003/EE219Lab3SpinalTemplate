package SpinalSulfur

import spinal.core._
import spinal.core.sim._
import spinal.lib._

case class TopLevel() extends Component {
  val io = new Bundle {
    // ...
  }

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
