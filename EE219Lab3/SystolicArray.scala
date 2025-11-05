package EE219Lab3

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import Tools.log2UpSafe

case class SystolicArray(cfg: Conv2DConfig = Conv2DConfig()) extends Component {
  val io = new Bundle {
    val x = in(Vec(UInt(cfg.dataWidth bits), cfg.maxRows))
    val w = in(Vec(UInt(cfg.dataWidth bits), cfg.maxCols))
    val y = out(Vec(UInt(cfg.dataWidth bits), cfg.maxCols))
    val rows = in(UInt(log2UpSafe(cfg.maxRows + 1) bits))
    val cols = in(UInt(log2UpSafe(cfg.maxCols + 1) bits))
    val length = in(UInt(log2UpSafe(cfg.maxBufferSize + 1) bits))
    val memWrEnable = out(Bool())
    val memAddr = out(UInt(cfg.addrWidth bits))
    val done = out(Bool())
  }

  // TODO
}

object SystolicArrayState extends SpinalEnum {
  val idle, calculate, output, done = newElement()
}

object SystolicArraySim extends App {
  Config.sim.compile(SystolicArray()).doSim { dut =>
    dut.clockDomain.forkStimulus(period = 10, resetCycles = 9)
    dut.clockDomain.waitRisingEdge()

  }
}

object SystolicArrayVerilog extends App {
  Config.spinal.generateVerilog(SystolicArray())
}

object SystolicArrayVhdl extends App {
  Config.spinal.generateVhdl(SystolicArray())
}
