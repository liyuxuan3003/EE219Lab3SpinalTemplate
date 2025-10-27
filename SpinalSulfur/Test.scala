package SpinalSulfur

import spinal.core._
import spinal.core.sim._
import spinal.lib._

case class Test() extends Component {
  val io = new Bundle {
    val a = in(Bits(4 bits))
    val b = in(Bits(4 bits))
    val i = in(Bool())
    val o = out(Bits(4 bits))
  }

  val test = Reg(Bits(4 bits)) init (0)
  test := Mux(io.i, io.a | io.b, io.a & io.b)
  io.o := test
}

object TestSim extends App {
  Config.sim.compile(Test()).doSim { dut =>
    dut.clockDomain.forkStimulus(period = 10, resetCycles = 10)

    for (a <- 0 to 15) {
      for (b <- 0 to 15) {
        dut.io.a #= a
        dut.io.b #= b
        dut.clockDomain.waitRisingEdge()
      }
    }
  }
}

object TestVerilog extends App {
  Config.spinal.generateVerilog(Test())
}

object TestVhdl extends App {
  Config.spinal.generateVhdl(Test())
}
