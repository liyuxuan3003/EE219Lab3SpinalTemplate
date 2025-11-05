package EE219Lab3

import spinal.core._
import spinal.core.sim._
import spinal.lib._

case class ProcessElement(cfg: Conv2DConfig = Conv2DConfig()) extends Component {
  val io = new Bundle {
    val xi = in(UInt(cfg.dataWidth bits))
    val wi = in(UInt(cfg.dataWidth bit))
    val yi = in(UInt(cfg.dataWidth bits))
    val outputEnable = in(Bool())
    val outputBypass = in(Bool())
    val xo = out(UInt(cfg.dataWidth bits))
    val wo = out(UInt(cfg.dataWidth bits))
    val yo = out(UInt(cfg.dataWidth bits))
  }

  // TODO
}

object ProcessElementSim extends App {
  Config.sim.compile(ProcessElement()).doSim { dut =>
    dut.clockDomain.forkStimulus(period = 10, resetCycles = 9)
    dut.clockDomain.waitRisingEdge()

  }
}

object ProcessElementVerilog extends App {
  Config.spinal.generateVerilog(ProcessElement())
}

object ProcessElementVhdl extends App {
  Config.spinal.generateVhdl(ProcessElement())
}
