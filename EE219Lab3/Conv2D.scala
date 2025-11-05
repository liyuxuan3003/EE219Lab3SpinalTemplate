package EE219Lab3

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import Tools.log2UpSafe
import scala.util.Random

case class Conv2D(cfg: Conv2DConfig = Conv2DConfig()) extends Component {
  val io = new Bundle {
    val done = out(Bool())
  }

  // Reset for im2col
  val resetnIm2col = Reg(Bool()) init (False)
  // Clocking area for im2col
  val areaIm2col = new ClockingArea(ClockDomain(clockDomain.clock, resetnIm2col)) {
    val im2col: Image2Col = Image2Col(cfg)
  }

  // Reset for systolic array
  val resetnSystol = Reg(Bool()) init (False)
  // Clocking area for systolic array
  val areaSystol = new ClockingArea(ClockDomain(clockDomain.clock, resetnSystol)) {
    val systol: SystolicArray = SystolicArray(cfg)
  }

  // Create a reference for im2col
  val im2col = areaIm2col.im2col

  // Create a reference for systolic array
  val systol = areaSystol.systol

  // Create memory for image, input, weight, output
  // Use sim public here to easily access from simulation
  // Use Vec instead of Mem here because systolic array need multiple input and output in one cycle
  val memImage = Vec(Reg(UInt(cfg.dataWidth bits)) init (0), cfg.sizeImage).simPublic()
  val memWeight = Vec(Reg(UInt(cfg.dataWidth bits)) init (0), cfg.sizeWeight).simPublic()
  val memInput = Vec(Reg(UInt(cfg.dataWidth bits)) init (0), cfg.sizeInput).simPublic()
  val memOutput = Vec(Reg(UInt(cfg.dataWidth bits)) init (0), cfg.sizeOutput).simPublic()

  // For im2col, mem read should be delay for one cycle
  val readSyncReg = Reg(UInt(cfg.dataWidth bits)) init (0)

  // Cycle counter for systolic array input generate
  val scycle = Reg(UInt(cfg.addrWidth bits)) init (0)

  // FSM
  val state = Reg(Conv2DState()) init (Conv2DState.idle)
  switch(state) {
    is(Conv2DState.idle) { state := Conv2DState.im2col }
    is(Conv2DState.im2col) { when(areaIm2col.im2col.io.done) { state := Conv2DState.systol } }
    is(Conv2DState.systol) { when(systol.io.done) { state := Conv2DState.done } }
    is(Conv2DState.done) { state := Conv2DState.done }
  }

  // Part for im2col
  resetnIm2col := (state === Conv2DState.im2col)
  // Connect im2col data read to register
  im2col.io.dataRd := readSyncReg
  when(state === Conv2DState.im2col) {
    // Read the requested data to register
    readSyncReg := memImage((im2col.io.addrRd - cfg.baseImage).resized)
    // Write the given data to memory if write enable
    when(im2col.io.memWrEnable) {
      memInput((im2col.io.addrWr - cfg.baseInput).resized) := im2col.io.dataWr
    }
  }

  // Part for systolic array
  resetnSystol := (state === Conv2DState.systol)
  // Pass rows, cols, length to the hardware port
  systol.io.rows := cfg.matrixM
  systol.io.cols := cfg.kernalK
  systol.io.length := cfg.matrixN
  // Generate input of w
  for (k <- 0 until cfg.kernalK) {
    val invaildMin = (scycle - 1) < k
    val invaildMax = (scycle - 1) > k + cfg.matrixN - 1
    val invaild = (invaildMin) || (invaildMax)
    val n = scycle - k - 1
    systol.io.w(k) := invaild ? U(0) | memWeight((k + n * cfg.kernalK).resized)
  }
  // Generate input of x
  for (m <- 0 until cfg.matrixM) {
    val invaildMin = (scycle - 1) < m
    val invaildMax = (scycle - 1) > m + cfg.matrixN - 1
    val invaild = (invaildMin) || (invaildMax)
    val n = scycle - m - 1
    systol.io.x(m) := invaild ? U(0) | memInput((m + n * cfg.matrixM).resized)
  }
  // Remember scycle will also add 1 on the cycle after systol reset
  // This behaviour is different from cnt inside systolic
  // So scycle - 1 should be the actual cycle you want
  when(state === Conv2DState.systol) {
    scycle := scycle + 1
    // Write the given data to memory if write enable
    when(systol.io.memWrEnable) {
      for (k <- 0 until cfg.kernalK) {
        memOutput((systol.io.memAddr + k - cfg.baseOutput).resized) := systol.io.y(k)
      }
    }
  }

  // Output done
  io.done := (state === Conv2DState.done)
}

object Conv2DState extends SpinalEnum {
  val idle, im2col, systol, done = newElement()
}

case class Conv2DConfig(dataWidth: Int = 32, addrWidth: Int = 32, imageC: Int = 1, imageW: Int = 4, imageH: Int = 4, filterSize: Int = 3, filterNum: Int = 2, maxRows: Int = 32, maxCols: Int = 32, maxBufferSize: Int = 72, baseImage: Int = 0x0000, baseWeight: Int = 0x1000, baseInput: Int = 0x2000, baseOutput: Int = 0x3000, addrInvalid: Int = 0xffff) {
  // Image padding
  val padding = (filterSize - 1) / 2

  // Image size
  val imagePadW = imageW + 2 * padding
  val imagePadH = imageH + 2 * padding

  // Kernal size
  val kernalR = filterSize
  val kernalS = filterSize
  val kernalK = filterNum

  // Input matrix size (from im2col)
  val matrixN = kernalR * kernalS * imageC
  val matrixM = imageW * imageH

  // Size of image
  val sizeImage = imageW * imageH * imageC

  // Size of input matrix
  // X: M * N
  val sizeInput = matrixM * matrixN

  // Size of weight matrix
  // W: N * K
  val sizeWeight = matrixN * kernalK

  // Size of output matrix
  // Y: M * K
  val sizeOutput = matrixM * kernalK
}

object Conv2DSim extends App {
  // For every configure in cfgArray
  Test.cfgArray.zipWithIndex.foreach { case (cfg, testIndex) =>
    Config.sim.compile(Conv2D(cfg)).doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10, resetCycles = 9)

      // Generate memory
      // Use random value to fill the memImage and memWeight
      val memImage = Array.tabulate(cfg.sizeImage)(i => Random.nextInt(256))
      val memWeight = Array.tabulate(cfg.sizeWeight)(i => Random.nextInt(256))
      val memInput = Array.fill(cfg.sizeInput)(0)
      val memOutput = Array.fill(cfg.sizeOutput)(0)

      // Write memImage to dut
      memImage.zipWithIndex.foreach { case (value, i) => dut.memImage(i) #= value }

      // Write memWeight to dut
      memWeight.zipWithIndex.foreach { case (value, i) => dut.memWeight(i) #= value }

      // Keep simulation until it is done
      while (dut.io.done.toBoolean != true) {
        dut.clockDomain.waitActiveEdge()
      }

      // Image store in H * W * C
      val image = memImage.grouped(cfg.imageC * cfg.imageW).toArray.map(row => row.grouped(cfg.imageC).toArray)

      // Matrix weight (size = N * k) store in N * K
      val matWeight = memWeight.grouped(cfg.kernalK).toArray

      // Indices for im2col
      val matInputIndices = for {
        h <- 0 until cfg.imageH
        w <- 0 until cfg.imageW
        c <- 0 until cfg.imageC
        s <- 0 until cfg.kernalS
        r <- 0 until cfg.kernalR
      } yield (h, w, c, s, r)

      // Im2col in software
      // Matrix input (size = M * N) store in N * M
      val matInput = Array.tabulate(cfg.matrixN, cfg.matrixM) { case (i, j) =>
        matInputIndices
          .map { case (h, w, c, s, r) =>
            val x = w + r
            val y = h + s
            val xInRange = ((x >= cfg.padding) && (x <= cfg.imagePadW - 1 - cfg.padding))
            val yInRange = ((y >= cfg.padding) && (y <= cfg.imagePadH - 1 - cfg.padding))
            if (xInRange && yInRange) image(y - cfg.padding)(x - cfg.padding)(c) else 0
          }
          .grouped(cfg.matrixN)
          .toArray
          .apply(j)(i)
      }

      // Im2col in hardware
      val matInputSim = Array.tabulate(cfg.matrixN, cfg.matrixM) { case (i, j) =>
        dut.memInput(i * cfg.matrixM + j).toLong.toInt
      }

      // Systol in software
      // Matrix multiplication Y = X * W, but X in col-based and W in row-based
      // --------------------------------
      // X (size = M * N) store in N * M (col-based)
      // W (size = N * K) store in N * K (row-based)
      // Y (size = M * K) store in M * K (row-based)
      // --------------------------------
      // Matrix output (size = M * K) store in M * K
      val matOutput = Array.tabulate(cfg.matrixM, cfg.kernalK) { case (i, j) =>
        ((matInput.map { col => col(i) }) zip (matWeight.map { row => row(j) })).map { case (a, b) => a * b }.sum
      }

      // Systol in hardware
      val matOutputSim = Array.tabulate(cfg.matrixM, cfg.kernalK) { case (i, j) =>
        dut.memOutput(i * cfg.kernalK + j).toLong.toInt
      }

      // Check the result and write the log
      Test.resultCheck(index = testIndex, cfg = cfg, image = image, matWeight = matWeight, matInput = matInput, matInputSim = matInputSim, matOutput = matOutput, matOutputSim = matOutputSim)
    }
  }
  // Summary the result and write the log
  Test.resultSummary()
}

object Conv2DVerilog extends App {
  Config.spinal.generateVerilog(Conv2D())
}

object Conv2DVhdl extends App {
  Config.spinal.generateVhdl(Conv2D())
}
