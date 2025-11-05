package EE219Lab3

import spinal.core._
import spinal.core.sim._
import spinal.lib._

case class Image2Col(cfg: Conv2DConfig = Conv2DConfig()) extends Component {
  val io = new Bundle {
    val dataRd = in(UInt(cfg.dataWidth bits))
    val dataWr = out(UInt(cfg.dataWidth bits))
    val addrRd = out(UInt(cfg.addrWidth bits))
    val addrWr = out(UInt(cfg.addrWidth bits))
    val done = out(Bool())
    val memWrEnable = out(Bool())
  }

  // TODO
}

object Image2ColState extends SpinalEnum {
  val idle, write, last, done = newElement()
}

object Image2ColSim extends App {
  Config.sim.compile(Image2Col()).doSim { dut =>
    dut.clockDomain.forkStimulus(period = 10, resetCycles = 9)
    dut.clockDomain.waitRisingEdge()

  }
}

object Image2ColVerilog extends App {
  Config.spinal.generateVerilog(Image2Col())
}

object Image2ColVhdl extends App {
  Config.spinal.generateVhdl(Image2Col())
}
